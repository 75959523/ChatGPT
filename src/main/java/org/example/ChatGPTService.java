package org.example;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.example.jdbc.DatabaseService;
import org.example.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
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

        String param = BufferedReaderParam.execute(request);
        ExecutorService executorService = ChatThreadPoolExecutor.newFixedThreadPool(5, 20, 5);

        Future<String> submit = executorService.submit(() -> {
            //请求OpenAI接口
            return OpenAIClient.execute(param);
        });

        String result;
        try {
            result = submit.get();
        } catch (Exception e) {
            e.printStackTrace();
            result = "";
        }

        String finalResult = result;
        executorService.execute(() -> {
            //采集用户信息
            getUserInfo.execute(request, param, finalResult);
        });

        response.setHeader("Transfer-Encoding", "chunked");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out;
        try {
            out = response.getWriter();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //处理返回结果 分块返回前端
        for (String chunk : SplitChunks.execute(result)) {
            out.write(chunk);
            out.write("\n\n");
            out.flush();
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
}
