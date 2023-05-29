package org.chatgpt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
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
        try {
            String requestParam = BufferedReaderParam.execute(request);

            //Request the OpenAI interface
            long begin = System.currentTimeMillis();
            Future<String> chatResponseFuture = ChatGptThreadPoolExecutor.getInstance().submit(() -> OpenAIClient.chat(requestParam));
            String result = getResultFromFuture(chatResponseFuture);

            if (!"500".equals(result)) {
                String time = CalculateTimeElapsed.format(begin);

                //token computation
                String msg = TokenCounter.sumToken(requestParam, result, time);

                //Collection of user information
                submitUserInfoTask(request, requestParam, result, msg);

                //Processing returned results
                sendChunksToClient(response, result, msg);

            } else {
                errorResponse(response);
            }
        } catch (Exception e) {
            logger.error("Unexpected exception occurred", e);
            errorResponse(response);
        }
    }

    @PostMapping("image")
    @ResponseBody
    public String getImage(HttpServletRequest request) {
        String requestParam = GetRequestMethod.ImageRequestParameter(request);
        String requestBody = GetRequestMethod.ImagePrepareRequestBody(requestParam);

        //Request the OpenAI interface
        Future<String> imageFuture = ChatGptThreadPoolExecutor.getInstance().submit(() -> OpenAIClient.image(requestBody));
        String imageString = getResultFromFuture(imageFuture);
        String[] urlArr = ExtractUrlsFromJson.execute(imageString);

        //Collection of user information
        ChatGptThreadPoolExecutor.getInstance().execute(() -> getUserInfo.execute(request, requestParam, null, urlArr, null));

        return Arrays.toString(urlArr);
    }

    @GetMapping(value = "get", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getUserInfo(HttpServletRequest request) {
        logger.info("check ip: " + GetRequestMethod.clientIpAddress(request));
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
        logger.info("check ip: " + GetRequestMethod.clientIpAddress(request));
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

    private void submitUserInfoTask(HttpServletRequest request, String requestParam, String result, String msg) {
        ChatGptThreadPoolExecutor.getInstance().execute(() -> {
            try {
                getUserInfo.execute(request, requestParam, result, null, msg);
            } catch (Exception e) {
                logger.error("Exception occurred while getting user info", e);
            }
        });
    }

    private void sendChunksToClient(HttpServletResponse response, String result, String msg) {
        response.setHeader("Transfer-Encoding", "chunked");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter out = response.getWriter()) {
            List<String> list = SplitChunks.execute(result);
            List<String> strings = addMsgToResponse(list, msg);

            for (String chunk : strings) {
                out.write(chunk);
                out.write("\n\n");
                out.flush();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> addMsgToResponse(List<String> list, String msg) {
        String doneBefore = list.get(list.size() - 2);
        String done = list.get(list.size() - 1);
        String add = doneBefore.substring(doneBefore.indexOf("data: ") + 6);
        String re = addMsgToJsonObject(add, msg);
        re = "data: " + re;
        list.set(list.size() - 2, re);
        list.set(list.size() - 1, doneBefore);
        list.add(done);
        return list;
    }

    public String addMsgToJsonObject(String add, String msg) {
        JSONObject jsonObject = JSONObject.parseObject(add);
        jsonObject.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("delta")
                .put("content", msg);
        jsonObject.getJSONArray("choices")
                .getJSONObject(0)
                .put("finish_reason", null);
        return jsonObject.toJSONString();
    }

    private void errorResponse(HttpServletResponse response) {
        response.setStatus(500);
    }
}
