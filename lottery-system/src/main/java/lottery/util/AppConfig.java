package lottery.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * 配置管理器（兼容层，委托给FileUtils）
 * 这个类提供了一个静态的配置管理接口，主要用于向后兼容，实际功能委托给FileUtils类
 */
public class AppConfig {
    // 静态变量，用于标记配置是否已加载完成
    private static boolean loaded = false;
    // 配置文件的名称常量
    private static final String CONFIG_FILE = "config.properties";

    // 静态初始化块，在类加载时执行，用于加载配置文件
    static {
        loadConfig();
    }

    /**
     * 私有静态方法，用于加载配置文件
     * 这个方法委托给FileUtils.loadConfig()来实际执行配置加载
     */
    private static void loadConfig() {
        try {
            // 调用FileUtils类的loadConfig方法加载配置
            FileUtils.loadConfig();
            // 加载成功后，将loaded标志设置为true
            loaded = true;
            // 输出初始化完成信息到控制台
            System.out.println("[INFO] AppConfig初始化完成");
        } catch (Exception e) {
            // 如果加载失败，输出错误信息到控制台
            System.err.println("[ERROR] 加载配置文件失败: " + e.getMessage());
            // 即使加载失败，也将loaded标志设置为true，避免重复尝试加载
            loaded = true;
        }
    }

    /**
     * 获取数据目录的路径
     * @return 数据目录的字符串路径
     */
    public static String getDataDir() {
        // 委托给FileUtils类的getDataDir方法
        return FileUtils.getDataDir();
    }

    /**
     * 获取服务器端口号
     * @return 服务器端口号的整数值，如果配置不存在则返回默认值8080
     */
    public static int getServerPort() {
        // 委托给FileUtils类的getConfigInt方法获取配置值
        return FileUtils.getConfigInt("server.port", 8080);
    }

    /**
     * 获取日志级别
     * @return 日志级别的字符串值，如果配置不存在则返回默认值"INFO"
     */
    public static String getLogLevel() {
        // 委托给FileUtils类的getConfigValue方法获取配置值
        return FileUtils.getConfigValue("log.level", "INFO");
    }

    /**
     * 获取配置属性值
     * @param key 配置项的键名
     * @param defaultValue 如果配置项不存在时返回的默认值
     * @return 配置项的字符串值，如果不存在则返回默认值
     */
    public static String getProperty(String key, String defaultValue) {
        // 委托给FileUtils类的getConfigValue方法获取配置值
        return FileUtils.getConfigValue(key, defaultValue);
    }

    /**
     * 获取整数类型的配置属性值
     * @param key 配置项的键名
     * @param defaultValue 如果配置项不存在或解析失败时返回的默认值
     * @return 配置项的整数值，如果不存在或解析失败则返回默认值
     */
    public static int getIntProperty(String key, int defaultValue) {
        // 委托给FileUtils类的getConfigInt方法获取配置值
        return FileUtils.getConfigInt(key, defaultValue);
    }

    /**
     * 检查配置是否已加载完成
     * @return 配置加载状态的布尔值，true表示已加载
     */
    public static boolean isLoaded() {
        return loaded;
    }
}