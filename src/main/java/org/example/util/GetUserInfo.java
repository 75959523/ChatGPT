package org.example.util;

import org.example.entity.UserInfo;
import org.example.jdbc.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

@Component
public class GetUserInfo {

    @Autowired
    DatabaseService databaseService;

    public int execute(HttpServletRequest request, String param){

        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        param = param.substring(param.lastIndexOf("content") + 10 ,param.length() - 3);
        String header = request.getHeader("User-Agent");
        Locale locale = new Locale("zh", "CN");
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Shanghai");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale);
        sdf.setTimeZone(timeZone);

        UserInfo userInfo = new UserInfo();
        userInfo.setQuestion(param);
        userInfo.setAddress(ipAddress);
        userInfo.setHeader(header);
        userInfo.setCreateTime(sdf.format(new Date()));

        return databaseService.addUserInfo(userInfo);
    }
}
