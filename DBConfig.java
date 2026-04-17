package com.safehaven.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConfig.java
 * Manages the MySQL database connection for the SafeHaven application.
 * All servlets and service classes obtain a connection through this class.
 */
public class DBConfig {

    private static final String DRIVER   = "com.mysql.cj.jdbc.Driver";
    private static final String URL      = "jdbc:mysql://localhost:3306/safehaven_db?useSSL=false&serverTimezone=UTC";
    private static final String USER     = "root";
    private static final String PASSWORD = "";   // set your MySQL password here

    // Private constructor — this class should never be instantiated
    private DBConfig() {}

    /**
     * Returns a live connection to the safehaven_db database.
     * Always close the connection in a finally block or try-with-resources.
     *
     * @return Connection object
     * @throws SQLException if the connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName(DRIVER);
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found. Add it to your build path.", e);
        }
    }
}
