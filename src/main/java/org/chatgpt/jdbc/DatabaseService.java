package org.chatgpt.jdbc;

import org.chatgpt.entity.IpInfo;
import org.chatgpt.entity.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class DatabaseService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public int addUserInfo(UserInfo userInfo) {

        String sql = "insert into user_info(question,address,header,create_time,uuid,answer,model) values (?, ? ,? ,?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql, new String[] {"id"});
                    ps.setString(1, userInfo.getQuestion());
                    ps.setString(2, userInfo.getAddress());
                    ps.setString(3, userInfo.getHeader());
                    ps.setString(4, userInfo.getCreateTime());
                    ps.setString(5, userInfo.getUuid());
                    ps.setString(6, userInfo.getAnswer());
                    ps.setString(7, userInfo.getModel());
                    return ps;
                },
                keyHolder
        );
        return Objects.requireNonNull(keyHolder.getKey()).intValue();
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
                " u.model," +
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
                " u.answer" +
//                ", " +
//                " u.address " +
                " FROM " +
                " user_info u " +
                " LEFT JOIN ip_info i ON ( u.uuid = i.uuid )  " +
                " ORDER BY " +
                " u.id DESC");

        return list;
    }
}
