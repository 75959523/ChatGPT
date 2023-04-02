package org.example.jdbc;

import org.example.entity.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public int addUserInfo(UserInfo userInfo) {
        int update = jdbcTemplate.update(
                "insert into user_info(question,address,header,create_time) values (?, ? ,? ,? )",
                userInfo.getQuestion(),
                userInfo.getAddress(),
                userInfo.getHeader(),
                userInfo.getCreateTime()
        );
        return update;
    }
}
