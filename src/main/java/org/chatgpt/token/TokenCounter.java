package org.chatgpt.token;

import org.chatgpt.util.FormatCost;
import org.chatgpt.util.GetRequestMethod;
import org.chatgpt.util.GetResponseMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;


public class TokenCounter {

    private static final Logger logger = LoggerFactory.getLogger(TokenCounter.class);

    public static String sumToken(String request, String result, String time){

        String model = GetRequestMethod.model(request);
        List<Map<String, String>> messageList = GetRequestMethod.messageList(request);

        String responseText = GetResponseMethod.execute(result);
        logger.info("request content: " + GetRequestMethod.question(request));
        logger.info("response content: " + responseText);

        int prompt = TikTokensUtil.tokens(model, messageList);
        int completion = TikTokensUtil.tokens(model, responseText);
        String msg = buildStatisticsMessage(time, model, prompt, completion);

        logger.info(msg.replace("\n\n", ""));
        return msg;
    }

    private static String buildStatisticsMessage(String time, String model, int prompt, int completion) {
        int token = prompt + completion;

        return "\n\n" +
                "Time-consuming to request OpenAI: " + time + " s" +
                ", model: " + model +
                ", prompt: " + prompt +
                ", completion: " + completion +
                ", token = " + token +
                ", request cost: " + requestCost(prompt, model) +
                ", response cost: " + responseCost(completion, model);
    }

    public static String requestCost(int tokenNum, String model) {
        double cost = 0;
        if(model.equals("gpt-3.5-turbo")) {
            cost = tokenNum * 0.001 * 0.002;
        } else if (model.equals("gpt-4")) {
            cost = tokenNum * 0.001 * 0.03;
        }
        return FormatCost.format(cost);
    }

    public static String responseCost(int tokenNum, String model) {
        double cost = 0;
        if(model.equals("gpt-3.5-turbo")) {
            cost = tokenNum * 0.001 * 0.002;
        } else if (model.equals("gpt-4")) {
            cost = tokenNum * 0.001 * 0.06;
        }
        return FormatCost.format(cost);
    }
}