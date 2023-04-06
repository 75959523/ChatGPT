package org.example.openai;

public class OpenAIClient extends OpenAIClientBase {

    public static String chat(String requestParam){

        return execute(requestParam, "POST", TARGET_URL_CHAT);
    }

    public static String model(){

        return execute("","GET", TARGET_URL_MODEL);
    }

    public static String image(String requestParam){

        return execute(requestParam,"POST", TARGET_URL_IMAGE);
    }
}
