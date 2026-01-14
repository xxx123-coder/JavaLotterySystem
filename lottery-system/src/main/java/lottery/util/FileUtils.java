package lottery.util;

import java.io.*;
import java.util.Properties;

/**
 * 文件处理工具类（整合了配置管理和路径管理）
 */
public class FileUtils {
    // 配置文件路径
    private static final String CONFIG_FILE = "C:\\Users\\Administrator\\Desktop\\JAVA课设\\lottery-system\\doc\\config.properties";

    // 配置文件缓存
    private static Properties configCache = null;

    // 路径常量 - 修改为完整桌面路径
    private static final String DATA_DIR = "C:\\Users\\Administrator\\Desktop\\JAVA课设\\lottery-system\\doc";
    private static final String LOGS_DIR = DATA_DIR + File.separator + "logs";

    /**
     * 检查文件是否存在
     */
    public static boolean fileExists(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }
        return new File(filePath).exists();
    }

    /**
     * 创建目录（包括父目录）
     */
    public static boolean createDirectory(String dirPath) {
        if (dirPath == null || dirPath.trim().isEmpty()) {
            return false;
        }
        return new File(dirPath).mkdirs();
    }

    /**
     * 获取项目根路径
     */
    public static String getProjectRoot() {
        return System.getProperty("user.dir");
    }

    /**
     * 获取文档目录路径
     */
    public static String getDocDir() {
        return DATA_DIR;
    }

    /**
     * 加载配置文件
     */
    public static synchronized Properties loadConfig() {
        if (configCache != null) {
            return configCache;
        }

        configCache = new Properties();

        try {
            // 从文档目录加载配置文件
            File configFile = new File(CONFIG_FILE);

            // 确保目录存在
            configFile.getParentFile().mkdirs();

            if (configFile.exists()) {
                try (InputStream input = new FileInputStream(configFile)) {
                    configCache.load(input);
                    System.out.println("[INFO] 加载配置文件: " + configFile.getAbsolutePath());
                }
            } else {
                // 配置文件不存在，使用内置默认配置
                System.out.println("[WARN] 配置文件不存在，使用内置默认配置");
                loadDefaultConfig();

                // 保存默认配置到文件
                saveDefaultConfig();
            }
        } catch (IOException e) {
            System.err.println("[ERROR] 加载配置文件失败: " + e.getMessage());
            loadDefaultConfig();
        }

        return configCache;
    }

    /**
     * 加载默认配置
     */
    private static void loadDefaultConfig() {
        configCache = new Properties();
        // 设置最小必要配置
        configCache.setProperty("server.port", "8080");
        configCache.setProperty("data.dir", DATA_DIR);
        configCache.setProperty("excel.users.file", "users.xlsx");
        configCache.setProperty("excel.tickets.file", "tickets.xlsx");
        configCache.setProperty("excel.results.file", "results.xlsx");
        configCache.setProperty("excel.winnings.file", "winnings.xlsx");
        configCache.setProperty("log.level", "INFO");
        configCache.setProperty("lottery.numbers.count", "7");
        configCache.setProperty("lottery.max.number", "36");
        configCache.setProperty("max.users", "1000");
    }

    /**
     * 保存默认配置到文件
     */
    private static void saveDefaultConfig() {
        try {
            // 确保目录存在
            new File(CONFIG_FILE).getParentFile().mkdirs();

            try (FileOutputStream output = new FileOutputStream(CONFIG_FILE)) {
                configCache.store(output, "Lottery System Configuration");
                System.out.println("[INFO] 创建默认配置文件: " + CONFIG_FILE);
            }
        } catch (IOException e) {
            System.err.println("[ERROR] 无法创建配置文件: " + e.getMessage());
        }
    }

    /**
     * 获取配置值
     */
    public static String getConfigValue(String key, String defaultValue) {
        if (configCache == null) {
            loadConfig();
        }
        return configCache.getProperty(key, defaultValue);
    }

    /**
     * 获取整数配置值
     */
    public static int getConfigInt(String key, int defaultValue) {
        String value = getConfigValue(key, null);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 获取布尔值配置值
     */
    public static boolean getConfigBoolean(String key, boolean defaultValue) {
        String value = getConfigValue(key, null);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.trim());
    }

    // === 路径管理方法 ===

    /**
     * 获取数据目录
     */
    public static String getDataDir() {
        return DATA_DIR;
    }

    /**
     * 获取日志目录
     */
    public static String getLogDir() {
        return LOGS_DIR;
    }

    /**
     * 获取用户文件路径
     */
    public static String getUserFilePath() {
        return DATA_DIR + File.separator + getConfigValue("excel.users.file", "users.xlsx");
    }

    /**
     * 获取彩票文件路径
     */
    public static String getTicketFilePath() {
        return DATA_DIR + File.separator + getConfigValue("excel.tickets.file", "tickets.xlsx");
    }

    /**
     * 获取结果文件路径
     */
    public static String getResultFilePath() {
        return DATA_DIR + File.separator + getConfigValue("excel.results.file", "results.xlsx");
    }

    /**
     * 获取中奖记录文件路径
     */
    public static String getWinningFilePath() {
        return DATA_DIR + File.separator + getConfigValue("excel.winnings.file", "winnings.xlsx");
    }

    /**
     * 确保数据目录存在
     */
    public static boolean ensureDataDir() {
        return createDirectory(DATA_DIR);
    }

    /**
     * 确保所有必要的目录都存在
     */
    public static void ensureAllDirectories() {
        createDirectory(DATA_DIR);
        createDirectory(LOGS_DIR);
    }
}