package lottery.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * 配置管理器（兼容层，委托给FileUtils）
 */
public class AppConfig {
    private static boolean loaded = false;
    private static final String CONFIG_FILE = "config.properties";

    static {
        loadConfig();
    }

    private static void loadConfig() {
        try {
            // 委托给FileUtils加载配置
            FileUtils.loadConfig();
            loaded = true;
            System.out.println("[INFO] AppConfig初始化完成");
        } catch (Exception e) {
            System.err.println("[ERROR] 加载配置文件失败: " + e.getMessage());
            loaded = true;
        }
    }

    /**
     * 获取数据目录
     */
    public static String getDataDir() {
        return FileUtils.getDataDir();
    }

    /**
     * 获取服务器端口
     */
    public static int getServerPort() {
        return FileUtils.getConfigInt("server.port", 8080);
    }

    /**
     * 获取日志级别
     */
    public static String getLogLevel() {
        return FileUtils.getConfigValue("log.level", "INFO");
    }

    /**
     * 获取配置值
     */
    public static String getProperty(String key, String defaultValue) {
        return FileUtils.getConfigValue(key, defaultValue);
    }

    /**
     * 获取整数配置值
     */
    public static int getIntProperty(String key, int defaultValue) {
        return FileUtils.getConfigInt(key, defaultValue);
    }

    /**
     * 检查配置是否已加载
     */
    public static boolean isLoaded() {
        return loaded;
    }
}