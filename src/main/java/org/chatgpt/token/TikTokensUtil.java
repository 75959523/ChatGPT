package org.chatgpt.token;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.ModelType;
import org.apache.commons.lang3.StringUtils;
import org.chatgpt.entity.Model;

import java.util.*;

public class TikTokensUtil {

    private static final Map<String, Encoding> modelMap = new HashMap<>();

    private static final EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();

    static {
        for (ModelType modelType : ModelType.values()) {
            modelMap.put(modelType.getName(), registry.getEncodingForModel(modelType));
        }
        modelMap.put(Model.GPT_3_5_TURBO_0301.getValue(), registry.getEncodingForModel(ModelType.GPT_3_5_TURBO));
        modelMap.put(Model.GPT_4_32K.getValue(), registry.getEncodingForModel(ModelType.GPT_4));
        modelMap.put(Model.GPT_4_32K_0314.getValue(), registry.getEncodingForModel(ModelType.GPT_4));
        modelMap.put(Model.GPT_4_0314.getValue(), registry.getEncodingForModel(ModelType.GPT_4));
    }
    /**
     * https://github.com/openai/openai-cookbook/blob/main/examples/How_to_count_tokens_with_tiktoken.ipynb
     */
    public static int tokens(String modelName, List<Map<String,String>> messages) {
        Encoding encoding = getEncoding(modelName);
        int tokensPerMessage = 0;
        int tokensPerName = 0;

        if (modelName.equals("gpt-3.5-turbo") || modelName.equals("gpt-3.5-turbo-0301")) {
            tokensPerMessage = 4;
            tokensPerName = -1;
        }

        if (modelName.equals("gpt-4") || modelName.equals("gpt-4-0314")) {
            tokensPerMessage = 3;
            tokensPerName = 1;
        }
        int sum = 0;
        for (Map<String,String> msg : messages) {
            sum += tokensPerMessage;
            sum += tokens(encoding, msg.get("content"));
            sum += tokens(encoding, msg.get("role"));
            if (StringUtils.isNotBlank(msg.get("name"))) {
                sum += tokens(encoding, msg.get("name"));
                sum += tokensPerName;
            }
        }
        sum += 3;
        return sum;
    }

    public static Encoding getEncoding(String modelName) {
        return modelMap.get(modelName);
    }

    public static int tokens(Encoding enc, String text) {
        return encode(enc, text).size();
    }

    public static List<Integer> encode(Encoding enc, String text) {
        return StringUtils.isBlank(text) ? new ArrayList<>() : enc.encode(text);
    }

    public static int tokens(String modelName, String text) {
        return encode(modelName, text).size();
    }

    public static List<Integer> encode(String modelName, String text) {
        if (StringUtils.isBlank(text)) {
            return new ArrayList<>();
        }
        Encoding enc = getEncoding(modelName);
        if (Objects.isNull(enc)) {
            return new ArrayList<>();
        }
        List<Integer> encoded = enc.encode(text);
        return encoded;
    }
}
