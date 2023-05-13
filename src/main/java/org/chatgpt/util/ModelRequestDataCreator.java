package org.chatgpt.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.chatgpt.entity.ModelRequestData;

public class ModelRequestDataCreator {

    public static String execute(String param){

        JSONObject jsonObject = JSONObject.parseObject(param);
        String model = String.valueOf(jsonObject.get("model"));
        String messages = String.valueOf(jsonObject.get("messages"));
        double top_p = Double.parseDouble(String.valueOf(jsonObject.get("top_p")));
        double temperature = Double.parseDouble(String.valueOf(jsonObject.get("temperature")));
        Boolean stream = Boolean.valueOf(String.valueOf(jsonObject.get("stream")));

        ModelRequestData requestData = new ModelRequestData();
        requestData.setModel(model);
        requestData.setMessages(messages);
        requestData.setTop_p(top_p);
        requestData.setTemperature(temperature);
        requestData.setStream(stream);

        String toJSONString = JSON.toJSONString(requestData).replace("\\", "");
        toJSONString = toJSONString.replace("\"messages\":\"","\"messages\":").replace("\",\"model\"",",\"model\"");
        return toJSONString;
    }
}
