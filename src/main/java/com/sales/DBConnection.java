/*
 * Copyright (c) 2026 Swagat Samal
 * Licensed under CC BY-NC-ND 4.0
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */
package com.sales;

import java.io.*;
import java.sql.*;
import java.util.Properties;

public class DBConnection {
    private static String URL, USER, PASS;
    static {
        try (InputStream in = DBConnection.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (in == null) throw new RuntimeException(
                    "config.properties not found. Copy config.example.properties and fill in your password.");
            Properties p = new Properties();
            p.load(in);
            URL  = p.getProperty("db.url");
            USER = p.getProperty("db.user");
            PASS = p.getProperty("db.password");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config: " + e.getMessage());
        }
    }
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
