package com.qrcode.attendance.main;

public class ApplicationLauncher {
    public static void main(String[] args) {
        System.out.println("Starting QR Code Attendance System...");

        try {
            // 测试配置加载
            System.out.println("Loading configuration...");

            // 这里添加启动逻辑
            System.out.println("System started successfully!");

        } catch (Exception e) {
            System.err.println("Failed to start system: " + e.getMessage());
            e.printStackTrace();
        }
    }
}