package org.chatgpt.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.chatgpt.entity.IpInfo;
import org.chatgpt.entity.UserInfo;
import org.chatgpt.feign.base.IpServiceFeignClient;
import org.chatgpt.jdbc.DatabaseService;
import org.chatgpt.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.IntStream;

@Component
public class GetUserInfo {

    @Autowired
    DatabaseService databaseService;

    @Autowired
    IpServiceFeignClient ipServiceFeignClient;

    @Autowired
    RedisService redisService;

    private final SimpleDateFormat sdf;

    public GetUserInfo() {
        Locale locale = new Locale("zh", "CN");
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Shanghai");
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale);
        sdf.setTimeZone(timeZone);
    }

    private static final Logger logger = LoggerFactory.getLogger(GetUserInfo.class);

    public void execute(HttpServletRequest request, String requestParam, String result, String[] urlArr, String msg) {
        String header = request.getHeader("User-Agent");
        String ipAddress = getClientIpAddress(request);

        // 根据请求ip获取位置相关信息
        RestTemplate restTemplate = new RestTemplate();
        Optional<IpInfo> response;
        try {
            response = Optional.ofNullable(restTemplate.getForObject("http://ip-api.com/json/" + ipAddress, IpInfo.class));
        } catch (Exception e) {
            logger.error("根据请求ip获取位置相关信息失败");
            e.printStackTrace();
            response = Optional.empty();
        }

        UserInfo userInfo = new UserInfo();
        response.ifPresent(info -> {
            if (info.getStatus().equals("success")) {
                String uuid = UUID.randomUUID().toString();
                info.setQuery(ipAddress);
                info.setUuid(uuid);

                // 根据经纬度坐标精确定位
                NominatimApiClient nominatimApiClient = new NominatimApiClient();
                String location = "";
                try {
                    location = nominatimApiClient.reverseGeocode(
                            Double.parseDouble(info.getLat()),
                            Double.parseDouble(info.getLon()),
                            18);
                } catch (Exception e) {
                    logger.error("根据经纬度坐标精确定位失败");
                    e.printStackTrace();
                }

                databaseService.addIpInfo(info, location);
                userInfo.setUuid(uuid);
            }
        });

        userInfo.setAddress(ipAddress);
        userInfo.setHeader(header);
        userInfo.setCreateTime(sdf.format(new Date()));

        if(result != null){
            userInfo.setQuestion(requestParam.substring(requestParam.lastIndexOf("content") + 10, requestParam.lastIndexOf("model") - 5));
            userInfo.setAnswer(GetResponseText.execute(result));
            userInfo.setModel(requestParam.substring(requestParam.lastIndexOf("model") + 8, requestParam.lastIndexOf("stream") - 3));
            userInfo.setMsg(msg.replace("\n\n" , ""));
            databaseService.addUserInfo(userInfo);

        }
        if(urlArr != null){
            userInfo.setQuestion(requestParam);
            int userInfoId = databaseService.addUserInfo(userInfo);
            IntStream.range(0, urlArr.length)
                    .forEach(index -> {
                        try {
                            SaveImageFromUrl.execute(urlArr[index], userInfoId + "_" + (index + 1));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        }
        updateUserInfoCache();
    }

    public static String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    private void updateUserInfoCache() {

        String jsonString = JSON.toJSONString(databaseService.getUserInfo(), SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue);
        String jsonStringClear = JSON.toJSONString(databaseService.getUserInfoClear(), SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue);

        redisService.set("user_info_key", jsonString);
        redisService.set("user_info_clear_key", jsonStringClear);
    }
}
