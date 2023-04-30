package org.chatgpt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.chatgpt.entity.ImageRequestData;
import org.chatgpt.jdbc.DatabaseService;
import org.chatgpt.openai.OpenAIClient;
import org.chatgpt.redis.RedisService;
import org.chatgpt.threadpool.ChatGptThreadPoolExecutor;
import org.chatgpt.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.Future;

@Component
@RequestMapping("api")
public class ChatGPTService {

    @Autowired
    GetUserInfo getUserInfo;

    @Autowired
    DatabaseService databaseService;

    @Autowired
    private RedisService redisService;

    @PostMapping("chat")
    public void chat(HttpServletRequest request, HttpServletResponse response) {
        String requestParam = BufferedReaderParam.execute(request);

        // 请求OpenAI接口
        Future<String> chatResponseFuture = ChatGptThreadPoolExecutor.getInstance().submit(() -> OpenAIClient.chat(requestParam));
        String result = getResultFromFuture(chatResponseFuture);

        // 采集用户信息
        ChatGptThreadPoolExecutor.getInstance().execute(() -> getUserInfo.execute(request, requestParam, result, null));

        configureResponse(response);
        sendChunksToClient(response, result);
    }

    @PostMapping("image")
    @ResponseBody
    public String getImage(HttpServletRequest request) {
        String requestParam = getRequestParameter(request);
        String requestBody = prepareRequestBody(requestParam);

        //请求OpenAI接口
        Future<String> imageFuture = ChatGptThreadPoolExecutor.getInstance().submit(() -> OpenAIClient.image(requestBody));
        String imageString = getResultFromFuture(imageFuture);
        String[] urlArr = extractUrlsFromJson(imageString);

        //采集用户信息
        ChatGptThreadPoolExecutor.getInstance().execute(() -> getUserInfo.execute(request, requestParam, null, urlArr));

        return Arrays.toString(urlArr);
    }

    @GetMapping(value = "get", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getUserInfo() {
        Object cachedData = redisService.get("user_info_key");
        if (cachedData != null) {
            return (String) cachedData;
        }
        String jsonString = JSON.toJSONString(databaseService.getUserInfo(), SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue);
        redisService.set("user_info_key", jsonString);
        return jsonString;
    }

    @GetMapping(value = "getClear", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getUserInfoClear(){
        Object cachedData = redisService.get("user_info_clear_key");
        if (cachedData != null) {
            return (String) cachedData;
        }
        String jsonString = JSON.toJSONString(databaseService.getUserInfoClear(), SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue);
        redisService.set("user_info_clear_key", jsonString);
        return jsonString;
    }

    @GetMapping(value = "model", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getModel(){
        return JSON.toJSONString(OpenAIClient.model(), SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue);
    }

    @GetMapping(value = "billing", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getBilling(){
        return JSON.toJSONString(OpenAIClient.billing(), SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue);
    }

    private String getResultFromFuture(Future<String> responseFuture) {
        try {
            return responseFuture.get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void configureResponse(HttpServletResponse response) {
        response.setHeader("Transfer-Encoding", "chunked");
        response.setCharacterEncoding("UTF-8");
    }

    private void sendChunksToClient(HttpServletResponse response, String result) {
        try (PrintWriter out = response.getWriter()) {
            // 处理返回结果，分块返回前端
            for (String chunk : SplitChunks.execute(result)) {
                out.write(chunk);
                out.write("\n\n");
                out.flush();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getRequestParameter(HttpServletRequest request) {
        String requestParam = BufferedReaderParam.execute(request);
        return requestParam.substring(requestParam.lastIndexOf("content") + 10, requestParam.lastIndexOf("}]") - 1);
    }

    private String prepareRequestBody(String requestParam) {
        return JSON.toJSONString(new ImageRequestData(requestParam, 2, "1024x1024")).replace("\\", "");
    }

    private String[] extractUrlsFromJson(String imageString) {
        JSONObject jsonObject = JSONObject.parseObject(imageString);
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) jsonObject.get("data");
        return dataList.stream().map(data -> data.get("url").toString()).toArray(String[]::new);
    }
}
