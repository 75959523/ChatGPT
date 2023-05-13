package org.chatgpt.token;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;
import com.knuddels.jtokkit.api.ModelType;
import org.apache.commons.lang3.StringUtils;
import org.chatgpt.entity.Model;

import java.util.*;

public class TikTokensUtil {

    /**
     * 模型名称对应Encoding
     */
    private static final Map<String, Encoding> modelMap = new HashMap<>();

    /**
     * registry实例
     */
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
     * 通过模型名称计算messages获取编码数组
     * 参考官方的处理逻辑：
     * <a href=https://github.com/openai/openai-cookbook/blob/main/examples/How_to_count_tokens_with_tiktoken.ipynb>https://github.com/openai/openai-cookbook/blob/main/examples/How_to_count_tokens_with_tiktoken.ipynb</a>
     *
     * @param modelName 模型名称
     * @param messages  消息体
     * @return tokens数量
     */
    public static int tokens(String modelName, List<Map<String,String>> messages) {
        Encoding encoding = getEncoding(modelName);
        int tokensPerMessage = 0;
        int tokensPerName = 0;
        //3.5统一处理
        if (modelName.equals("gpt-3.5-turbo") || modelName.equals("gpt-3.5-turbo-0301")) {
            tokensPerMessage = 4;
            tokensPerName = -1;
        }
        //4.0统一处理
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

    /**
     * 通过模型名称, 计算指定字符串的tokens
     */
    public static int tokens(String modelName, String text) {
        return encode(modelName, text).size();
    }

    /**
     * 获取encode的编码数组，通过模型名称
     */
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

    /**
     * 获取一个Encoding对象，通过模型名称
     */
    public static Encoding getEncoding(String modelName) {
        return modelMap.get(modelName);
    }

    /**
     * 通过Encoding计算text信息的tokens
     */
    public static int tokens(Encoding enc, String text) {
        return encode(enc, text).size();
    }

    /**
     * 通过Encoding和text获取编码数组
     */
    public static List<Integer> encode(Encoding enc, String text) {
        return StringUtils.isBlank(text) ? new ArrayList<>() : enc.encode(text);
    }

    /**
     * 计算指定字符串的tokens，通过EncodingType
     */
    public static int tokens(EncodingType encodingType, String text) {
        return encode(encodingType, text).size();
    }

    /**
     * 获取encode的编码数组
     */
    public static List<Integer> encode(EncodingType encodingType, String text) {
        if (StringUtils.isBlank(text)) {
            return new ArrayList<>();
        }
        Encoding enc = getEncoding(encodingType);
        List<Integer> encoded = enc.encode(text);
        return encoded;
    }

    /**
     * 获取一个Encoding对象，通过Encoding类型
     */
    public static Encoding getEncoding(EncodingType encodingType) {
        Encoding enc = registry.getEncoding(encodingType);
        return enc;
    }

    public static void main(String[] args) {
        String text = "123";
        List<Integer> encode = TikTokensUtil.encode(EncodingType.CL100K_BASE, text);
        long tokens = TikTokensUtil.tokens(EncodingType.CL100K_BASE, text);
        System.out.println(encode);
        System.out.println(tokens);
    }
}
