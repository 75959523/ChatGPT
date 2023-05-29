package org.chatgpt.util;

import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Map;

public class ExtractUrlsFromJson {

    public static String[] execute(String imageString) {
        JSONObject jsonObject = JSONObject.parseObject(imageString);
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) jsonObject.get("data");
        return dataList.stream().map(data -> data.get("url").toString()).toArray(String[]::new);
    }
}
