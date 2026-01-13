package lottery.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * 文件处理工具类
 * 提供文件操作、路径处理、配置管理等功能
 */
public class FileUtils {
    // 私有构造函数，防止实例化
    private FileUtils() {
        throw new IllegalStateException("工具类不可实例化");
    }

    // 配置文件路径
    private static final String CONFIG_FILE = "config.properties";
    private static final String DATA_DIR = "data";

    // 配置文件缓存
    private static Properties configCache = null;

    /**
     * 检查文件是否存在
     */
    public static boolean fileExists(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }
        File file = new File(filePath);
        return file.exists() && file.isFile();
    }

    /**
     * 检查目录是否存在
     */
    public static boolean directoryExists(String dirPath) {
        if (dirPath == null || dirPath.trim().isEmpty()) {
            return false;
        }
        File dir = new File(dirPath);
        return dir.exists() && dir.isDirectory();
    }

    /**
     * 创建目录（包括父目录）
     */
    public static void createDirectory(String dirPath) throws IOException {
        if (dirPath == null || dirPath.trim().isEmpty()) {
            throw new IllegalArgumentException("目录路径不能为空");
        }
        Path path = Paths.get(dirPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

    /**
     * 确保文件所在目录存在
     */
    public static void ensureFileDirectory(String filePath) throws IOException {
        if (filePath == null || filePath.trim().isEmpty()) {
            return;
        }
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            createDirectory(parent.getAbsolutePath());
        }
    }

    /**
     * 获取项目根路径（基于classpath） - 修复路径问题
     */
    public static String getProjectRoot() {
        try {
            // 使用更可靠的方式获取路径
            java.net.URL url = FileUtils.class.getProtectionDomain().getCodeSource().getLocation();
            String path = java.net.URLDecoder.decode(url.getPath(), "UTF-8");

            // 处理Windows文件协议
            if (path.startsWith("file:/")) {
                if (path.startsWith("file://")) {
                    path = path.substring("file://".length());
                } else {
                    path = path.substring("file:/".length());
                }
            }

            // 移除开头的斜杠（Windows路径）
            if (path.startsWith("/") && path.contains(":")) {
                path = path.substring(1);
            }

            // 如果是jar文件，获取jar所在目录
            if (path.contains(".jar!")) {
                int jarIndex = path.lastIndexOf(".jar!");
                if (jarIndex > 0) {
                    path = path.substring(0, jarIndex + 4);
                }
                // 获取jar文件所在目录
                File jarFile = new File(path);
                return jarFile.getParent();
            }

            File file = new File(path);
            if (file.isFile()) {
                // 如果是文件，返回父目录
                return file.getParent();
            } else {
                // 如果是目录，返回目录本身
                return path;
            }
        } catch (Exception e) {
            // 如果上述方法失败，使用当前工作目录
            return System.getProperty("user.dir");
        }
    }

    /**
     * 获取数据文件路径
     */
    public static String getDataPath() {
        String projectRoot = getProjectRoot();
        Path dataPath = Paths.get(projectRoot, DATA_DIR);
        return dataPath.toString();
    }

    /**
     * 获取数据文件的完整路径
     */
    public static String getDataFilePath(String fileName) {
        String dataPath = getDataPath();
        // 确保数据目录存在
        try {
            createDirectory(dataPath);
        } catch (IOException e) {
            System.err.println("创建数据目录失败: " + e.getMessage());
        }
        return Paths.get(dataPath, fileName).toString();
    }

    /**
     * 获取绝对路径
     */
    public static String getAbsolutePath(String relativePath) {
        if (relativePath == null) {
            return getProjectRoot();
        }
        // 如果已经是绝对路径，直接返回
        if (Paths.get(relativePath).isAbsolute()) {
            return relativePath;
        }
        // 否则，相对于项目根路径
        return Paths.get(getProjectRoot(), relativePath).toString();
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
            // 尝试从classpath加载
            try (InputStream input = FileUtils.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
                if (input != null) {
                    configCache.load(input);
                    System.out.println("[INFO] 从classpath加载配置文件成功");
                } else {
                    // 如果classpath中没有，尝试从项目根目录加载
                    String configPath = getAbsolutePath(CONFIG_FILE);
                    File configFile = new File(configPath);
                    if (configFile.exists()) {
                        try (FileInputStream fis = new FileInputStream(configFile)) {
                            configCache.load(fis);
                            System.out.println("[INFO] 从文件系统加载配置文件: " + configPath);
                        }
                    } else {
                        System.out.println("[INFO] 配置文件不存在，使用默认配置");
                        loadDefaultConfig();
                    }
                }
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
        // 设置默认配置
        configCache.setProperty("server.port", "8080");
        configCache.setProperty("excel.data.dir", "data");
        configCache.setProperty("lottery.number.count", "7");
        configCache.setProperty("lottery.number.min", "1");
        configCache.setProperty("lottery.number.max", "36");
        configCache.setProperty("ticket.price", "2.0");
    }

    /**
     * 获取配置值
     */
    public static String getConfigValue(String key) {
        return getConfigValue(key, null);
    }

    /**
     * 获取配置值
     */
    public static String getConfigValue(String key, String defaultValue) {
        Properties config = loadConfig();
        return config.getProperty(key, defaultValue);
    }

    /**
     * 获取整数配置值
     */
    public static int getConfigInt(String key, int defaultValue) {
        String value = getConfigValue(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            System.err.println("配置值格式错误，键: " + key + ", 值: " + value);
            return defaultValue;
        }
    }

    /**
     * 获取浮点数配置值
     */
    public static double getConfigDouble(String key, double defaultValue) {
        String value = getConfigValue(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            System.err.println("配置值格式错误，键: " + key + ", 值: " + value);
            return defaultValue;
        }
    }

    /**
     * 获取布尔值配置值
     */
    public static boolean getConfigBoolean(String key, boolean defaultValue) {
        String value = getConfigValue(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value) || "1".equals(value) || "true".equalsIgnoreCase(value);
    }

    /**
     * 清理配置缓存，强制重新加载
     */
    public static synchronized void clearConfigCache() {
        configCache = null;
    }

    /**
     * 获取文件扩展名
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1);
        }
        return "";
    }

    /**
     * 检查文件是否为Excel文件
     */
    public static boolean isExcelFile(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        return "xlsx".equals(extension) || "xls".equals(extension);
    }

    /**
     * 安全删除文件
     */
    public static boolean safeDelete(String filePath) {
        if (!fileExists(filePath)) {
            return true;
        }
        try {
            File file = new File(filePath);
            return file.delete();
        } catch (SecurityException e) {
            System.err.println("删除文件失败（权限不足）: " + filePath);
            return false;
        }
    }
}