package com.safehaven.config;

import java.sql.Connection;
import java.sql.SQLException;

public class TestConnection {
    public static void main(String[] args) {
        try {
            Connection conn = DBConfig.getConnection();
            if (conn != null) {
                System.out.println("Connection successful!");
                conn.close();
            }
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
    }
}