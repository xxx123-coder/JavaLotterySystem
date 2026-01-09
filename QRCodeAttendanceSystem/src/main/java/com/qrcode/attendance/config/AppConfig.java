package com.qrcode.attendance.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AppConfig {
    private static final Properties properties = new Properties();

    static {
        loadConfig();
    }

    private static void loadConfig() {
        try {
            // 先加载默认配置
            properties.setProperty("app.name", "QR Code Attendance System");
            properties.setProperty("app.version", "1.0.0");
            properties.setProperty("web.server.port", "8080");

            // 尝试从文件加载
            File configFile = new File("src/main/resources/config/application.properties");
            if (configFile.exists()) {
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    properties.load(fis);
                    System.out.println("Configuration loaded from file");
                }
            } else {
                System.out.println("Config file not found, using defaults");
            }
        } catch (IOException e) {
            System.err.println("Error loading config: " + e.getMessage());
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static int getIntProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(getProperty(key));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}