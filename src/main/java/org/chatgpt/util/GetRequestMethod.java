package org.chatgpt.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.chatgpt.entity.ImageRequestData;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetRequestMethod {

    public static String model(String request) {
        return request.substring(request.lastIndexOf("model") + 8, request.lastIndexOf("stream") - 3);
    }

    public static List<Map<String, String>> messageList(String request) {
        String requestMsg = request.substring(request.indexOf("messages") + 10, request.lastIndexOf("model") - 2);
        return stringToList(requestMsg);
    }

    private static List<Map<String,String>> stringToList(String requestMsg){
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

    public static String question(String request) {
        return request.substring(request.lastIndexOf("content") + 10, request.lastIndexOf("model") - 5);
    }

    public static String clientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    public static String ImageRequestParameter(HttpServletRequest request) {
        String requestParam = BufferedReaderParam.execute(request);
        return requestParam.substring(requestParam.lastIndexOf("content") + 10, requestParam.lastIndexOf("}]") - 1);
    }

    public static String ImagePrepareRequestBody(String requestParam) {
        return JSON.toJSONString(new ImageRequestData(requestParam, 2, "1024x1024")).replace("\\", "");
    }
}
