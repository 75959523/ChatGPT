package org.chatgpt.openai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class OpenAIClientBase {

    private static final Logger logger = LoggerFactory.getLogger(OpenAIClientBase.class);
    public static final String API_KEY = "";
    public static final String TARGET_URL_CHAT = "https://api.openai.com/v1/chat/completions";
    public static final String TARGET_URL_MODEL = "https://api.openai.com/v1/models";
    public static final String TARGET_URL_IMAGE = "https://api.openai.com/v1/images/generations";
    public static final String TARGET_URL_BILLING = "https://api.openai.com/dashboard/billing/credit_grants";

    public static String execute(String requestParam, String requestType, String targetUrl) {

        logger.info("请求参数:" + requestParam);
        StringBuilder content = new StringBuilder();
        try {
            URL url = new URL(targetUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod(requestType);
            setRequestHeaders(connection);
            connection.setDoOutput(true);

            if (!requestParam.isEmpty()) {
                try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
                    outputStream.write(requestParam.getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();
                }
            }

            try (BufferedReader inputReader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String inputLine;
                while ((inputLine = inputReader.readLine()) != null) {
                    content.append(inputLine);
                }
            }

        } catch (Exception e) {
            logger.error("请求OpenAI异常：", e);
            return "error";
        }
        return content.toString();
    }

    private static void setRequestHeaders(HttpURLConnection connection) {
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
    }
}