package lottery.util;

import java.io.*;
import java.util.Properties;

/**
 * 文件处理工具类（整合了配置管理和路径管理）
 * 这个类负责文件操作、配置文件加载和路径管理功能
 */
public class FileUtils {
    // 配置文件路径常量，使用绝对路径指向桌面上的配置文件
    private static final String CONFIG_FILE = "C:\\Users\\Administrator\\Desktop\\JAVA课设\\lottery-system\\doc\\config.properties";

    // 配置缓存，使用Properties对象存储加载的配置，避免重复加载
    private static Properties configCache = null;

    // 路径常量 - 修改为完整桌面路径
    // 数据目录常量，指向桌面上的文档目录
    private static final String DATA_DIR = "C:\\Users\\Administrator\\Desktop\\JAVA课设\\lottery-system\\doc";
    // 日志目录常量，在数据目录下创建logs子目录
    private static final String LOGS_DIR = DATA_DIR + File.separator + "logs";

    /**
     * 检查指定路径的文件是否存在
     * @param filePath 要检查的文件路径
     * @return 文件是否存在，如果路径为空或只包含空格则返回false
     */
    public static boolean fileExists(String filePath) {
        // 如果文件路径为空或仅包含空格，直接返回false
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }
        // 创建File对象并检查其是否存在
        return new File(filePath).exists();
    }

    /**
     * 创建指定路径的目录（包括所有必要的父目录）
     * @param dirPath 要创建的目录路径
     * @return 创建是否成功，如果路径为空或只包含空格则返回false
     */
    public static boolean createDirectory(String dirPath) {
        // 如果目录路径为空或仅包含空格，直接返回false
        if (dirPath == null || dirPath.trim().isEmpty()) {
            return false;
        }
        // 创建File对象并调用mkdirs方法创建所有必要的目录
        return new File(dirPath).mkdirs();
    }

    /**
     * 获取项目根目录的路径
     * @return 项目根目录的字符串路径
     */
    public static String getProjectRoot() {
        // 使用系统属性user.dir获取当前工作目录（通常是项目根目录）
        return System.getProperty("user.dir");
    }

    /**
     * 获取文档目录的路径
     * @return 文档目录的字符串路径
     */
    public static String getDocDir() {
        // 返回数据目录常量
        return DATA_DIR;
    }

    /**
     * 加载配置文件到内存缓存中
     * 这个方法使用同步锁确保线程安全，避免并发加载问题
     * @return 包含配置属性的Properties对象
     */
    public static synchronized Properties loadConfig() {
        // 如果配置缓存已存在，直接返回缓存的内容
        if (configCache != null) {
            return configCache;
        }

        // 创建新的Properties对象作为配置缓存
        configCache = new Properties();

        try {
            // 根据配置文件路径创建File对象
            File configFile = new File(CONFIG_FILE);

            // 确保配置文件所在的目录存在，如果不存在则创建
            configFile.getParentFile().mkdirs();

            // 检查配置文件是否存在
            if (configFile.exists()) {
                // 使用try-with-resources语句确保输入流正确关闭
                try (InputStream input = new FileInputStream(configFile)) {
                    // 从输入流加载配置到Properties对象
                    configCache.load(input);
                    // 输出加载成功信息到控制台
                    System.out.println("[INFO] 加载配置文件: " + configFile.getAbsolutePath());
                }
            } else {
                // 配置文件不存在，使用内置默认配置
                System.out.println("[WARN] 配置文件不存在，使用内置默认配置");
                // 加载默认配置到缓存
                loadDefaultConfig();

                // 将默认配置保存到文件，方便后续使用
                saveDefaultConfig();
            }
        } catch (IOException e) {
            // 捕获IO异常，输出错误信息到控制台
            System.err.println("[ERROR] 加载配置文件失败: " + e.getMessage());
            // 加载默认配置作为备选方案
            loadDefaultConfig();
        }

        // 返回配置缓存
        return configCache;
    }

    /**
     * 加载默认配置到缓存中
     * 这个方法设置系统运行所需的最小配置项
     */
    private static void loadDefaultConfig() {
        // 创建新的Properties对象作为配置缓存
        configCache = new Properties();
        // 设置最小必要配置项
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
     * 将默认配置保存到配置文件
     * 这个方法用于首次运行系统时创建配置文件
     */
    private static void saveDefaultConfig() {
        try {
            // 确保配置文件所在的目录存在
            new File(CONFIG_FILE).getParentFile().mkdirs();

            // 使用try-with-resources语句确保输出流正确关闭
            try (FileOutputStream output = new FileOutputStream(CONFIG_FILE)) {
                // 将配置缓存的内容写入到配置文件
                configCache.store(output, "Lottery System Configuration");
                // 输出创建成功信息到控制台
                System.out.println("[INFO] 创建默认配置文件: " + CONFIG_FILE);
            }
        } catch (IOException e) {
            // 捕获IO异常，输出错误信息到控制台
            System.err.println("[ERROR] 无法创建配置文件: " + e.getMessage());
        }
    }

    /**
     * 获取配置值，如果配置项不存在则返回默认值
     * @param key 配置项的键名
     * @param defaultValue 如果配置项不存在时返回的默认值
     * @return 配置项的字符串值，如果不存在则返回默认值
     */
    public static String getConfigValue(String key, String defaultValue) {
        // 如果配置缓存为空，先加载配置
        if (configCache == null) {
            loadConfig();
        }
        // 从配置缓存中获取值，如果不存在则返回默认值
        return configCache.getProperty(key, defaultValue);
    }

    /**
     * 获取整数类型的配置值
     * @param key 配置项的键名
     * @param defaultValue 如果配置项不存在或解析失败时返回的默认值
     * @return 配置项的整数值，如果不存在或解析失败则返回默认值
     */
    public static int getConfigInt(String key, int defaultValue) {
        // 先获取配置值的字符串形式
        String value = getConfigValue(key, null);
        // 如果值为空，直接返回默认值
        if (value == null) {
            return defaultValue;
        }
        try {
            // 尝试将字符串转换为整数
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            // 如果转换失败，返回默认值
            return defaultValue;
        }
    }

    /**
     * 获取布尔类型的配置值
     * @param key 配置项的键名
     * @param defaultValue 如果配置项不存在时返回的默认值
     * @return 配置项的布尔值，如果不存在则返回默认值
     */
    public static boolean getConfigBoolean(String key, boolean defaultValue) {
        // 先获取配置值的字符串形式
        String value = getConfigValue(key, null);
        // 如果值为空，直接返回默认值
        if (value == null) {
            return defaultValue;
        }
        // 将字符串转换为布尔值
        return Boolean.parseBoolean(value.trim());
    }

    // === 路径管理方法 ===

    /**
     * 获取数据目录路径
     * @return 数据目录的字符串路径
     */
    public static String getDataDir() {
        return DATA_DIR;
    }

    /**
     * 获取日志目录路径
     * @return 日志目录的字符串路径
     */
    public static String getLogDir() {
        return LOGS_DIR;
    }

    /**
     * 获取用户Excel文件的完整路径
     * @return 用户文件完整路径的字符串
     */
    public static String getUserFilePath() {
        // 组合数据目录和配置的文件名
        return DATA_DIR + File.separator + getConfigValue("excel.users.file", "users.xlsx");
    }

    /**
     * 获取彩票Excel文件的完整路径
     * @return 彩票文件完整路径的字符串
     */
    public static String getTicketFilePath() {
        // 组合数据目录和配置的文件名
        return DATA_DIR + File.separator + getConfigValue("excel.tickets.file", "tickets.xlsx");
    }

    /**
     * 获取开奖结果Excel文件的完整路径
     * @return 结果文件完整路径的字符串
     */
    public static String getResultFilePath() {
        // 组合数据目录和配置的文件名
        return DATA_DIR + File.separator + getConfigValue("excel.results.file", "results.xlsx");
    }

    /**
     * 获取中奖记录Excel文件的完整路径
     * @return 中奖记录文件完整路径的字符串
     */
    public static String getWinningFilePath() {
        // 组合数据目录和配置的文件名
        return DATA_DIR + File.separator + getConfigValue("excel.winnings.file", "winnings.xlsx");
    }

    /**
     * 确保数据目录存在
     * @return 目录创建是否成功
     */
    public static boolean ensureDataDir() {
        // 调用createDirectory方法创建数据目录
        return createDirectory(DATA_DIR);
    }

    /**
     * 确保所有必要的目录都存在
     * 这个方法会依次创建数据目录和日志目录
     */
    public static void ensureAllDirectories() {
        // 创建数据目录
        createDirectory(DATA_DIR);
        // 创建日志目录
        createDirectory(LOGS_DIR);
    }
}