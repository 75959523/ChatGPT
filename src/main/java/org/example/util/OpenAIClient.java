package org.example.util;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class OpenAIClient {

    private static final String API_KEY = "";
    private static final String TARGET_URL = "https://api.openai.com/v1/chat/completions";

    private static final Logger logger = LoggerFactory.getLogger(OpenAIClient.class);

    public static String execute(String param) {

        logger.info("请求参数：" + param);
        StringBuilder content = null;

        try {
            URL url = new URL(TARGET_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setDoOutput(true);

            DataOutputStream outputStream = null;
            BufferedReader inputReader = null;
            try {
                long begin = System.currentTimeMillis();
                outputStream = new DataOutputStream(connection.getOutputStream());
                param = JSON.toJSONString(ModelRequestDataCreator.execute(param)).replace("\\", "");
                param = param.replace("\"messages\":\"","\"messages\":").replace("\",\"model\"",",\"model\"");
                /*使用 writeBytes 方法时，您可能会以不正确的编码格式（例如，不是 UTF-8）将 JSON 字符串转换为字节数组。这可能导致在服务器端解析 JSON 时出现问题。*/
                outputStream.write(param.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                long end = System.currentTimeMillis();
                logger.info("请求OpenAI耗时：" + (end - begin) + " ms");


//                long begin1 = System.currentTimeMillis();
//                int responseCode = connection.getResponseCode();
//                long end1 = System.currentTimeMillis();
//                logger.info("获取响应码耗时：" + (end1 - begin1) + " ms");

                long begin2 = System.currentTimeMillis();
//                if (responseCode == HttpURLConnection.HTTP_OK) {
//                    inputReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//                } else {
//                    inputReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
//                }

                inputReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                String inputLine;
                content = new StringBuilder();
                while ((inputLine = inputReader.readLine()) != null) {
                    content.append(inputLine);
                }
                long end2 = System.currentTimeMillis();
                logger.info("获取响应结果耗时：" + (end2 - begin2) + " ms");
            } finally {
                outputStream.close();
                inputReader.close();
                connection.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return content.toString();
    }
}