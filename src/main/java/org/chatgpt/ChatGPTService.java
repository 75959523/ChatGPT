package org.chatgpt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.chatgpt.entity.ImageRequestData;
import org.chatgpt.jdbc.DatabaseService;
import org.chatgpt.openai.OpenAIClient;
import org.chatgpt.redis.RedisService;
import org.chatgpt.threadpool.ChatGptThreadPoolExecutor;
import org.chatgpt.token.TokenCounter;
import org.chatgpt.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(ChatGPTService.class);

    @PostMapping("chat")
    public void chat(HttpServletRequest request, HttpServletResponse response) {
        String requestParam = BufferedReaderParam.execute(request);

        //请求OpenAI接口
        long beginTime = System.currentTimeMillis();
        Future<String> chatResponseFuture = ChatGptThreadPoolExecutor.getInstance().submit(() -> OpenAIClient.chat(requestParam));
        String result = getResultFromFuture(chatResponseFuture);
        if(!"error".equals(result)) {
            //费用统计
             String msg = TokenCounter.sumToken(requestParam, result, beginTime);

            //采集用户信息
            ChatGptThreadPoolExecutor.getInstance().execute(() -> getUserInfo.execute(request, requestParam, result, null, msg));

            sendChunksToClient(response, result, msg);
        }
        errorResponse(response);
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
        ChatGptThreadPoolExecutor.getInstance().execute(() -> getUserInfo.execute(request, requestParam, null, urlArr, null));

        return Arrays.toString(urlArr);
    }

    @GetMapping(value = "get", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getUserInfo(HttpServletRequest request) {
        logger.info("查询IP:" + GetUserInfo.getClientIpAddress(request));
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
    public String getUserInfoClear(HttpServletRequest request){
        logger.info("查询IP:" + GetUserInfo.getClientIpAddress(request));
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
    private void errorResponse(HttpServletResponse response) {
        response.setStatus(500);
    }

    private void sendChunksToClient(HttpServletResponse response, String result, String msg) {
        response.setHeader("Transfer-Encoding", "chunked");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter out = response.getWriter()) {
            // 处理返回结果，分块返回前端
            List<String> list = SplitChunks.execute(result);

            String s = list.get(list.size() - 2);
            String done = list.get(list.size() - 1);
            String str = s.substring(s.indexOf("data: ") + 6);
            JSONObject jsonObject = JSONObject.parseObject(str);
            jsonObject.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("delta")
                    .put("content", msg);
            jsonObject.getJSONArray("choices")
                    .getJSONObject(0)
                    .put("finish_reason", null);
            String s1 = jsonObject.toJSONString();
            s1 = "data: " + s1;
            list.set(list.size() - 2,s1);
            list.set(list.size() - 1,s);
            list.add(done);

            for (String chunk : list) {
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
