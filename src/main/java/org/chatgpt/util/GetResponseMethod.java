package org.chatgpt.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class GetResponseMethod {

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
}
