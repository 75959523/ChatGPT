package org.example.jdbc;

import org.example.entity.IpInfo;
import org.example.entity.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DatabaseService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public int addUserInfo(UserInfo userInfo) {
        int update = jdbcTemplate.update(
                "insert into user_info(question,address,header,create_time,uuid,answer) values (?, ? ,? ,?, ?, ?)",
                userInfo.getQuestion(),
                userInfo.getAddress(),
                userInfo.getHeader(),
                userInfo.getCreateTime(),
                userInfo.getUuid(),
                userInfo.getAnswer()
        );
        return update;
    }

    public int addIpInfo(IpInfo ipInfo, String location) {
        int update = jdbcTemplate.update(
                "insert into ip_info(" +
                        "country," +
                        "countryCode," +
                        "regionName," +
                        "city," +
                        "lat," +
                        "lon," +
                        "isp," +
                        "org," +
                        "as1," +
                        "query," +
                        "uuid ," +
                        "location) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )",

                ipInfo.getCountry(),
                ipInfo.getCountryCode(),
                ipInfo.getRegionName(),
                ipInfo.getCity(),
                ipInfo.getLat(),
                ipInfo.getLon(),
                ipInfo.getIsp(),
                ipInfo.getOrg(),
                ipInfo.getAs(),
                ipInfo.getQuery(),
                ipInfo.getUuid(),
                location
        );
        return update;
    }

    public List<Map<String, Object>> getUserInfo(){

        List<Map<String, Object>> list = jdbcTemplate.queryForList("" +
                " SELECT " +
                " u.question, " +
                " u.answer, " +
                " u.address, " +
                " i.location, " +
                " i.country, " +
                " i.countryCode, " +
                " i.regionName, " +
                " i.city, " +
                " i.lat, " +
                " i.lon, " +
                " i.isp, " +
                " i.org, " +
                " i.as1, " +
                " u.create_time  " +
                " FROM " +
                " user_info u " +
                " LEFT JOIN ip_info i ON ( u.uuid = i.uuid )  " +
                " ORDER BY " +
                " u.id DESC");

        return list;
    }

    public List<Map<String, Object>> getUserInfoClear(){

        List<Map<String, Object>> list = jdbcTemplate.queryForList("" +
                " SELECT " +
                " u.question, " +
                " u.answer, " +
                " u.address " +
                " FROM " +
                " user_info u " +
                " LEFT JOIN ip_info i ON ( u.uuid = i.uuid )  " +
                " ORDER BY " +
                " u.id DESC");

        return list;
    }
}
