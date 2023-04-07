package org.example;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.example.entity.ImageRequestData;
import org.example.entity.UserInfo;
import org.example.jdbc.DatabaseService;
import org.example.openai.OpenAIClient;
import org.example.util.*;
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

    @PostMapping("chat")
    public void chat(HttpServletRequest request, HttpServletResponse response) {
        String requestParam = BufferedReaderParam.execute(request);

        Future<String> submit = ChatGptThreadPoolExecutor.getInstance().submit(() -> {
            // 请求OpenAI接口
            return OpenAIClient.chat(requestParam);
        });

        ChatGptThreadPoolExecutor.getInstance().execute(() -> {
            try {
                String finalResult = submit.get();
                // 采集用户信息
                getUserInfo.execute(request, requestParam, finalResult);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        response.setHeader("Transfer-Encoding", "chunked");
        response.setCharacterEncoding("UTF-8");

        try {
            String result = submit.get();
            try (PrintWriter out = response.getWriter()) {
                // 处理返回结果，分块返回前端
                for (String chunk : SplitChunks.execute(result)) {
                    out.write(chunk);
                    out.write("\n\n");
                    out.flush();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping(value = "get", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getUserInfo(){
        return JSON.toJSONString(databaseService.getUserInfo(), SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue);
    }

    @GetMapping(value = "getClear", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getUserInfoClear(){
        return JSON.toJSONString(databaseService.getUserInfoClear(), SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue);
    }

    @GetMapping(value = "model", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getModel(){
        return JSON.toJSONString(OpenAIClient.model(), SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue);
    }

    @PostMapping(value = "image", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getImage(HttpServletRequest request){
        String requestParam = BufferedReaderParam.execute(request);

        requestParam = requestParam.substring(requestParam.lastIndexOf("content") + 10, requestParam.lastIndexOf("}]") - 1);

        ImageRequestData imageRequestData = new ImageRequestData();
        imageRequestData.setPrompt(requestParam);
        imageRequestData.setN(2);
        imageRequestData.setSize("1024x1024");

        String requestBody = JSON.toJSONString(imageRequestData).replace("\\", "");
        String image = OpenAIClient.image(requestBody);
        JSONObject jsonObject = JSONObject.parseObject(image);
        List<Map<String,Object>> stringList = (List<Map<String,Object>>) jsonObject.get("data");

        String[] arr = new String[stringList.size()];
        for(int i=0;i<stringList.size();i++){
            arr[i] = stringList.get(i).get("url").toString();
        }

        UserInfo userInfo = new UserInfo();
        userInfo.setQuestion(requestParam);
        userInfo.setAddress(GetUserInfo.getClientIpAddress(request));
        userInfo.setCreateTime(GetUserInfo.sdf.format(new Date()));

        ChatGptThreadPoolExecutor.getInstance().execute(() -> {
            try {
                int userInfoId = databaseService.addUserInfo(userInfo);
                for (int i=0;i<arr.length;i++) {
                    SaveImageFromUrl.execute(arr[i], userInfoId + "_" + (i + 1));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return Arrays.toString(arr);
    }
}
