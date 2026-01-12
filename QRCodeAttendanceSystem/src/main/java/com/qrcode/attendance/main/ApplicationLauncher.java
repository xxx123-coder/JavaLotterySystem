package com.qrcode.attendance.main;

import com.qrcode.attendance.config.AppConfig;
import com.qrcode.attendance.gui.MainWindow;
import com.qrcode.attendance.server.EmbeddedWebServer;
import com.qrcode.attendance.util.LoggerUtil;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 应用程序启动类 - 主程序入口
 * 负责启动整个QR签到系统
 */
public class ApplicationLauncher {

    private static final Logger LOGGER = LoggerUtil.getLogger(ApplicationLauncher.class);
    // 修正拼写错误：java.io.tmpdir 是正确的
    private static final String LOCK_FILE = System.getProperty("java.io.tmpdir") + "/qrcode_attendance.lock";
    private static FileLock lock;
    private static RandomAccessFile lockFile;

    public static void main(String[] args) {
        try {
            // 1. 确保程序只能启动一个实例
            if (!acquireLock()) {
                JOptionPane.showMessageDialog(null,
                        "应用程序已在运行中！",
                        "系统提示",
                        JOptionPane.WARNING_MESSAGE);
                System.exit(0);
            }

            // 2. 设置外观（使用系统外观）
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                LOGGER.warning("无法设置系统外观: " + e.getMessage());
            }

            // 3. 初始化应用程序配置
            initializeApplicationConfig();

            // 4. 在事件分发线程中创建GUI
            SwingUtilities.invokeLater(() -> {
                try {
                    // 5. 创建并显示主窗口
                    MainWindow mainWindow = new MainWindow();
                    mainWindow.setVisible(true);

                    // 设置主窗口到应用程序控制器
                    AttendanceApplication app = AttendanceApplication.getInstance();
                    app.setMainWindow(mainWindow);

                    // 6. 处理系统托盘图标
                    if (SystemTray.isSupported()) {
                        SystemTrayManager trayManager = new SystemTrayManager(mainWindow);
                        trayManager.createTrayIcon();
                    }

                    // 7. 启动内嵌Web服务器
                    startEmbeddedWebServer();

                } catch (Exception e) {
                    handleUncaughtException(e);
                }
            });

            // 8. 设置全局未捕获异常处理器
            setGlobalExceptionHandler();

            // 9. 添加关闭钩子
            addShutdownHook();

        } catch (Exception e) {
            handleUncaughtException(e);
        }
    }

    /**
     * 获取程序实例锁（确保单实例运行）
     */
    private static boolean acquireLock() {
        try {
            lockFile = new RandomAccessFile(LOCK_FILE, "rw");
            lock = lockFile.getChannel().tryLock();
            return lock != null;
        } catch (Exception e) {
            LOGGER.severe("无法获取实例锁: " + e.getMessage());
            return false;
        }
    }

    /**
     * 初始化应用程序配置
     */
    private static void initializeApplicationConfig() {
        try {
            // 初始化应用配置单例
            AppConfig config = AppConfig.getInstance(); // 此行正常

            // 初始化数据目录结构
            DataInitializer dataInitializer = new DataInitializer();
            dataInitializer.initializeDataStructure();

            // 初始化应用程序主控制器
            AttendanceApplication.getInstance().initialize();

            LOGGER.info("应用程序配置初始化完成");
        } catch (Exception e) {
            LOGGER.severe("初始化应用程序配置失败: " + e.getMessage());
            throw new RuntimeException("初始化失败", e);
        }
    }

    /**
     * 启动内嵌Web服务器
     */
    private static void startEmbeddedWebServer() {
        try {
            EmbeddedWebServer server = EmbeddedWebServer.getInstance();
            server.start();

            // 设置Web服务器到应用程序控制器
            AttendanceApplication.getInstance().setWebServer(server);

            LOGGER.info("内嵌Web服务器已启动，端口: " + server.getPort());
        } catch (Exception e) {
            LOGGER.severe("启动Web服务器失败: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Web服务器启动失败，部分功能可能无法使用。\n错误: " + e.getMessage(),
                    "服务器错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 设置全局异常处理器
     */
    private static void setGlobalExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            handleUncaughtException(throwable);
        });
    }

    /**
     * 处理未捕获的异常
     */
    private static void handleUncaughtException(Throwable throwable) {
        LOGGER.log(Level.SEVERE, "未捕获的异常: ", throwable);

        // 显示错误对话框
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null,
                    "应用程序发生错误: " + throwable.getMessage() +
                            "\n详细信息请查看日志文件。",
                    "系统错误",
                    JOptionPane.ERROR_MESSAGE);
        });

        // 如果异常严重，退出程序
        if (throwable instanceof VirtualMachineError) {
            System.exit(1);
        }
    }

    /**
     * 添加关闭钩子
     */
    private static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                // 释放实例锁
                if (lock != null) {
                    lock.release();
                }
                if (lockFile != null) {
                    lockFile.close();
                }

                // 删除锁文件
                File file = new File(LOCK_FILE);
                if (file.exists()) {
                    if (!file.delete()) {
                        LOGGER.warning("无法删除锁文件: " + LOCK_FILE);
                    }
                }

                // 停止应用程序
                AttendanceApplication.getInstance().stop();

                LOGGER.info("应用程序已正常关闭");
            } catch (Exception e) {
                LOGGER.severe("关闭钩子执行失败: " + e.getMessage());
            }
        }));
    }
}