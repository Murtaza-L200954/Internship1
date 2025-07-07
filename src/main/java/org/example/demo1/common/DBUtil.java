package org.example.demo1.common;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DBUtil {
    private static final HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/RetailShopDB?serverTimezone=UTC");
        config.setUsername("root");

        String dbPassword = System.getenv("DB_PASSWORD");
        if (dbPassword == null) {
            throw new RuntimeException("Environment variable DB_PASSWORD is not set.");
        }

        config.setPassword(dbPassword);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(30000);
        config.setConnectionTimeout(20000);
        config.setLeakDetectionThreshold(15000);

        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection(); // returns a pooled connection
    }
}
