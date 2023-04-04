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
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

@Component
public class GetUserInfo {

    @Autowired
    DatabaseService databaseService;

    @Autowired
    IpServiceFeignClient ipServiceFeignClient;

    private static final Logger logger = LoggerFactory.getLogger(GetUserInfo.class);

    public int execute(HttpServletRequest request, String param, String result){

        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }

        //根据请求ip获取位置相关信息
        RestTemplate restTemplate = new RestTemplate();
        IpInfo response = null;
        try {
            response = restTemplate.getForObject("http://ip-api.com/json/" + ipAddress, IpInfo.class);
        } catch (Exception e) {
            logger.error("根据请求ip获取位置相关信息失败");
            e.printStackTrace();
        }

        UserInfo userInfo = new UserInfo();
        if(response != null && response.getStatus().equals("success")){
            String uuid = UUID.randomUUID().toString();
            response.setQuery(ipAddress);
            response.setUuid(uuid);

            //根据经纬度坐标精确定位
            NominatimApiClient nominatimApiClient = new NominatimApiClient();
            String location = "";
            try {
                location = nominatimApiClient.reverseGeocode(
                        Double.parseDouble(response.getLat()),
                        Double.parseDouble(response.getLon()),
                        18);
            } catch (Exception e) {
                logger.error("根据经纬度坐标精确定位失败");
                e.printStackTrace();
            }

            databaseService.addIpInfo(response, location);
            userInfo.setUuid(uuid);
        }

//        ResponseEntity<IpInfo> ipInfo = ipServiceFeignClient.getIPInfo(ipAddress);
//        if(ipInfo.getStatusCode().value() == 200){
//            IpInfo info = ipInfo.getBody();
//            String uuid = UUID.randomUUID().toString();
//            info.setQuery(ipAddress);
//            info.setUuid(uuid);
//            databaseService.addIpInfo(info);
//
//            userInfo.setUuid(uuid);
//        }

        param = param.substring(param.lastIndexOf("content") + 10 ,param.length() - 3);
        Locale locale = new Locale("zh", "CN");
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Shanghai");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale);
        sdf.setTimeZone(timeZone);

        userInfo.setQuestion(param);
        userInfo.setAddress(ipAddress);
        userInfo.setHeader(request.getHeader("User-Agent"));
        userInfo.setCreateTime(sdf.format(new Date()));
        userInfo.setAnswer(GetAnswer.extractContent(result));

        return databaseService.addUserInfo(userInfo);
    }
}
