package org.example.util;

import org.example.entity.ModelRequestData;

public class ModelRequestDataCreator {

    public static ModelRequestData execute(String param){

        ModelRequestData requestData = new ModelRequestData();
        requestData.setMessages(param);
        requestData.setModel("gpt-3.5-turbo");
        requestData.setStream(true);
        requestData.setTemperature(1);
        requestData.setTop_p(1);

        return requestData;
    }
}
