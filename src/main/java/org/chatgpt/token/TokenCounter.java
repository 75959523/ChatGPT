package org.chatgpt.token;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.chatgpt.util.GetResponseText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TokenCounter {

    private static final Logger logger = LoggerFactory.getLogger(TokenCounter.class);

    private static final DecimalFormat df = new DecimalFormat("#.##########");

    public static String sumToken(String request, String result, long beginTime){

        DecimalFormat df = new DecimalFormat("0.00");
        String time = df.format((double)(System.currentTimeMillis() - beginTime) / 1000);

        String model = getModel(request);
        List<Map<String, String>> messageList = getMessageList(request);

        String responseText = GetResponseText.execute(result);
        logger.info("响应内容:" + responseText);

        int prompt = TikTokensUtil.tokens(model, messageList);
        int completion = TikTokensUtil.tokens(model, responseText);
        int token = prompt + completion;

        String msg = "\n\n" +
                "请求OpenAI耗时: " + time + " s" +
                ", model: " + model +
                ", prompt: " + prompt +
                ", completion: " + completion +
                ", token = " + token +
                ", 请求费用: " + requestPrice(prompt, model) +
                ", 响应费用: " + responsePrice(completion, model);
        logger.info(msg.replace("\n\n", ""));
        return msg;
    }

    private static String getModel(String request) {
        return request.substring(request.lastIndexOf("model") + 8, request.lastIndexOf("stream") - 3);
    }

    private static List<Map<String, String>> getMessageList(String request) {
        String requestMsg = request.substring(request.indexOf("messages") + 10, request.lastIndexOf("model") - 2);
        return stringToList(requestMsg);
    }

    public static List<Map<String,String>> stringToList(String requestMsg){
        JSONArray jsonArray = JSON.parseArray(requestMsg);
        List<Map> messageArray = JSONObject.parseArray(jsonArray.toJSONString(), Map.class);
        List<Map<String, String>> messageList = new ArrayList<>();
        for (Map map : messageArray) {
            Map<String, String> convertedMap = new HashMap<>();
            for (Object key : map.keySet()) {
                convertedMap.put((String) key, (String) map.get(key));
            }
            messageList.add(convertedMap);
        }
        return messageList;
    }

    public static String requestPrice(int tokenNum, String model) {
        double price = 0;
        if(model.equals("gpt-3.5-turbo")) {
            price = tokenNum * 0.001 * 0.002;
        } else if (model.equals("gpt-4")) {
            price = tokenNum * 0.001 * 0.03;
        }
        return df.format(price) + "$";
    }

    public static String responsePrice(int tokenNum, String model) {
        double price = 0;
        if(model.equals("gpt-3.5-turbo")) {
            price = tokenNum * 0.001 * 0.002;
        } else if (model.equals("gpt-4")) {
            price = tokenNum * 0.001 * 0.06;
        }
        return df.format(price) + "$";
    }
}