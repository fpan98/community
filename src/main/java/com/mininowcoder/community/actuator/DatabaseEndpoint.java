package com.mininowcoder.community.actuator;

import com.mininowcoder.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by FeiPan on 2022/5/1.
 */

@Component
@Endpoint(id = "database") // 自定义测试数据库是否正常连接的端点，访问路径为localhost:8080/community/actuator/database
public class DatabaseEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseEndpoint.class);

    @Autowired
    private DataSource dataSource;

    @ReadOperation  // 表示这是一个get请求
    public String checkConnection() {
        try (
                Connection conn = dataSource.getConnection();
        ) {
            return CommunityUtil.getJSONString(0, "获取数据库连接成功");
        } catch (SQLException e) {
            logger.error("获取数据库连接失败：" + e.getMessage());
            return CommunityUtil.getJSONString(1, "获取数据库连接失败");
        }
    }
}
