package org.example;

import org.example.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Component
@RequestMapping("api")
public class ChatGPTService {

    @Autowired
    GetUserInfo getUserInfo;

    @PostMapping(value = "chat")
    public void chat(HttpServletRequest request, HttpServletResponse response) {

        ExecutorService executorService = ChatThreadPoolExecutor.newFixedThreadPool(5, 20, 5);

        Future<List<String>> submit = executorService.submit(() -> {
            //获取请求参数
            String param = BufferedReaderParam.execute(request);
            //用户信息入库
            getUserInfo.execute(request, param);
            //请求OpenAI接口
            String execute = OpenAIClient.execute(param);
            //处理返回结果 分块返回前端
            return SplitChunks.execute(execute);
        });

        response.setHeader("Transfer-Encoding", "chunked");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out;
        List<String> list;
        try {
            out = response.getWriter();
            list = submit.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        for (String chunk : list) {
            out.write(chunk);
            out.write("\n\n");
            out.flush();
        }
    }
}
