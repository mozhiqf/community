package com.forum.community.actuator;

import com.forum.community.util.CommunityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
@Endpoint(id = "database")
@Slf4j
public class DatabaseEndpoints {

    @Autowired
    private DataSource dataSource;

    //通过GET请求来访问
    @ReadOperation
    public String checkConnection() {
        try (Connection connection = dataSource.getConnection()) {
            return CommunityUtil.getJSONString(0, "获取连接成功！");
        } catch (SQLException e) {
            log.error("获取连接失败！" + e.getMessage());
            return CommunityUtil.getJSONString(1, "获取连接失败！");
        }
    }

}
