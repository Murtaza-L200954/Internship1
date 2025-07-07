package org.example.demo1.common;

import java.sql.Connection;

public class DBTest {
    public static void main(String[] args) {
        try (Connection conn = DBUtil.getConnection()) {
            System.out.println("✅ Pooled DB connection successful!");
        } catch (Exception e) {
            System.out.println("❌ Failed to connect");
            e.printStackTrace();
        }
    }
}
