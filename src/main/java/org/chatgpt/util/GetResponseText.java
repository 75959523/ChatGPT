package org.chatgpt.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetResponseText {

    public static String execute(String result) {
        StringBuilder responseText = new StringBuilder();
        for (String data : dataSplitter(result)) {
            if (!data.equals(" [DONE]")) {
                responseText.append(extractResponseText(data));
            }
        }
        return responseText.toString();
    }

    public static String[] dataSplitter(String input){
        String[] dataArray = input.split("data:");

        // Remove the first empty element if present
        if (dataArray.length > 0 && dataArray[0].isEmpty()) {
            String[] tempArray = new String[dataArray.length - 1];
            System.arraycopy(dataArray, 1, tempArray, 0, dataArray.length - 1);
            dataArray = tempArray;
        }
        return dataArray;
    }

    public static String extractResponseText(String data) {
        JSONObject jsonObject = JSONObject.parseObject(data);
        JSONArray choices = jsonObject.getJSONArray("choices");
        StringBuilder responseTextBuilder = new StringBuilder();

        for (int i = 0; i < choices.size(); i++) {
            JSONObject choice = choices.getJSONObject(i);
            JSONObject delta = choice.getJSONObject("delta");
            if(delta.containsKey("content")) {
                responseTextBuilder.append(delta.getString("content"));
            }
        }
        return responseTextBuilder.toString();
    }

    public static String extractContent(String input) {
        List<String> contents = new ArrayList<String>();
        Pattern pattern = Pattern.compile("\"content\":\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            contents.add(matcher.group(1));
        }
        return String.join("", contents);
    }
}
