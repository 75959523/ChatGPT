package org.example.util;

import org.example.entity.IpInfo;
import org.example.entity.UserInfo;
import org.example.feign.base.IpServiceFeignClient;
import org.example.jdbc.DatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class GetUserInfo {

    @Autowired
    DatabaseService databaseService;

    @Autowired
    IpServiceFeignClient ipServiceFeignClient;

    private final SimpleDateFormat sdf;

    public GetUserInfo() {
        Locale locale = new Locale("zh", "CN");
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Shanghai");
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale);
        sdf.setTimeZone(timeZone);
    }

    private static final Logger logger = LoggerFactory.getLogger(GetUserInfo.class);

    public int execute(HttpServletRequest request, String param, String result) {
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

        //param = param.substring(param.lastIndexOf("content") + 10, param.length() - 3);
        param = param.substring(param.lastIndexOf("content") + 10, param.lastIndexOf("model") - 5);
        userInfo.setQuestion(param);
        userInfo.setAddress(ipAddress);
        userInfo.setHeader(header);
        userInfo.setCreateTime(sdf.format(new Date()));
        userInfo.setAnswer(GetAnswer.extractContent(result));

        return databaseService.addUserInfo(userInfo);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
}
