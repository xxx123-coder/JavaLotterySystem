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
     * 获取项目根路径（修复路径问题）
     * 采用多种策略确保在不同环境下都能正确获取路径
     */
    public static String getProjectRoot() {
        try {
            // 策略1: 使用系统属性 user.dir（当前工作目录）
            String userDir = System.getProperty("user.dir");
            System.out.println("[DEBUG] 当前工作目录: " + userDir);

            // 策略2: 尝试通过类加载器获取类路径
            String classpathPath = "";
            try {
                java.net.URL url = FileUtils.class.getProtectionDomain().getCodeSource().getLocation();
                classpathPath = url.getPath();

                // 处理URL编码
                if (classpathPath.contains("%")) {
                    classpathPath = java.net.URLDecoder.decode(classpathPath, "UTF-8");
                }

                System.out.println("[DEBUG] 类路径: " + classpathPath);

                // 如果是jar文件，获取jar所在目录
                if (classpathPath.contains(".jar")) {
                    File jarFile = new File(classpathPath);
                    if (jarFile.isFile()) {
                        String jarDir = jarFile.getParent();
                        System.out.println("[DEBUG] JAR所在目录: " + jarDir);
                        return jarDir;
                    }
                }

                // 如果是类文件，获取类文件所在目录
                File classFile = new File(classpathPath);
                if (classFile.exists()) {
                    // 如果是classes目录，向上回溯到项目根目录
                    if (classpathPath.contains("classes")) {
                        // 找到classes目录的父目录
                        String path = classpathPath;
                        while (path.contains("classes")) {
                            File current = new File(path);
                            if (current.isDirectory() && current.getName().equals("classes")) {
                                return current.getParent();
                            }
                            path = current.getParent();
                        }
                    }
                    return classFile.getAbsolutePath();
                }
            } catch (Exception e) {
                System.err.println("[DEBUG] 获取类路径失败: " + e.getMessage());
            }

            // 策略3: 尝试通过查找data目录来确定项目根目录
            // 从当前工作目录向上查找，直到找到data目录或到达根目录
            File currentDir = new File(userDir);
            while (currentDir != null) {
                File dataDir = new File(currentDir, DATA_DIR);
                if (dataDir.exists()) {
                    System.out.println("[DEBUG] 找到data目录: " + dataDir.getAbsolutePath());
                    return currentDir.getAbsolutePath();
                }
                File parent = currentDir.getParentFile();
                if (parent == null || parent.equals(currentDir)) {
                    break;
                }
                currentDir = parent;
            }

            // 策略4: 查找配置文件
            File currentConfigDir = new File(userDir);
            while (currentConfigDir != null) {
                File configFile = new File(currentConfigDir, CONFIG_FILE);
                if (configFile.exists()) {
                    System.out.println("[DEBUG] 找到配置文件: " + configFile.getAbsolutePath());
                    return currentConfigDir.getAbsolutePath();
                }
                File parent = currentConfigDir.getParentFile();
                if (parent == null || parent.equals(currentConfigDir)) {
                    break;
                }
                currentConfigDir = parent;
            }

            // 如果所有策略都失败，使用当前工作目录
            System.out.println("[DEBUG] 使用当前工作目录作为项目根目录: " + userDir);
            return userDir;

        } catch (Exception e) {
            System.err.println("[ERROR] 获取项目根路径失败: " + e.getMessage());
            // 返回当前工作目录作为后备
            return System.getProperty("user.dir");
        }
    }

    /**
     * 获取数据文件路径
     */
    public static String getDataPath() {
        String projectRoot = getProjectRoot();
        Path dataPath = Paths.get(projectRoot, DATA_DIR);
        String dataPathStr = dataPath.toString();
        System.out.println("[DEBUG] 数据目录路径: " + dataPathStr);
        return dataPathStr;
    }

    /**
     * 获取数据文件的完整路径
     */
    public static String getDataFilePath(String fileName) {
        String dataPath = getDataPath();
        // 确保数据目录存在
        try {
            createDirectory(dataPath);
            System.out.println("[DEBUG] 确保数据目录存在: " + dataPath);
        } catch (IOException e) {
            System.err.println("[ERROR] 创建数据目录失败: " + e.getMessage());
        }
        String fullPath = Paths.get(dataPath, fileName).toString();
        System.out.println("[DEBUG] 数据文件完整路径: " + fullPath);
        return fullPath;
    }

    /**
     * 获取绝对路径
     */
    public static String getAbsolutePath(String relativePath) {
        if (relativePath == null) {
            return getProjectRoot();
        }

        // 如果已经是绝对路径，直接返回
        File file = new File(relativePath);
        if (file.isAbsolute()) {
            return relativePath;
        }

        // 否则，相对于项目根路径
        String absolutePath = Paths.get(getProjectRoot(), relativePath).toString();
        System.out.println("[DEBUG] 相对路径转换: " + relativePath + " -> " + absolutePath);
        return absolutePath;
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
            // 策略1: 从项目根目录加载配置文件
            String configPath = getAbsolutePath(CONFIG_FILE);
            File configFile = new File(configPath);
            System.out.println("[DEBUG] 尝试加载配置文件: " + configPath);

            if (configFile.exists()) {
                try (InputStream input = new FileInputStream(configFile)) {
                    configCache.load(input);
                    System.out.println("[INFO] 从文件系统加载配置文件: " + configPath);
                    return configCache;
                }
            }

            // 策略2: 从classpath加载
            System.out.println("[DEBUG] 尝试从classpath加载配置文件");
            try (InputStream input = FileUtils.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
                if (input != null) {
                    configCache.load(input);
                    System.out.println("[INFO] 从classpath加载配置文件成功");
                    return configCache;
                }
            }

            // 策略3: 创建默认配置文件并加载
            System.out.println("[INFO] 配置文件不存在，创建默认配置");
            loadDefaultConfig();

            // 保存默认配置到文件
            try {
                java.util.Properties properties = new java.util.Properties();
                properties.putAll(configCache);
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(configPath)) {
                    properties.store(fos, "彩票系统默认配置");
                    System.out.println("[INFO] 已创建默认配置文件: " + configPath);
                }
            } catch (Exception e) {
                System.err.println("[WARN] 创建配置文件失败: " + e.getMessage());
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
        configCache.setProperty("prize.special", "1000000.0");
        configCache.setProperty("prize.first", "50000.0");
        configCache.setProperty("prize.second", "1000.0");
        configCache.setProperty("prize.third", "100.0");
        configCache.setProperty("log.level", "INFO");
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

    /**
     * 简化版获取项目根路径（无调试输出）
     */
    public static String getProjectRootSimple() {
        try {
            // 首先尝试当前工作目录
            String userDir = System.getProperty("user.dir");

            // 检查当前工作目录下是否有data目录
            File dataDir = new File(userDir, DATA_DIR);
            if (dataDir.exists()) {
                return userDir;
            }

            // 尝试通过类加载器获取
            java.net.URL url = FileUtils.class.getProtectionDomain().getCodeSource().getLocation();
            String path = url.getPath();

            // 处理URL编码
            if (path.contains("%")) {
                path = java.net.URLDecoder.decode(path, "UTF-8");
            }

            // 移除file:协议前缀
            if (path.startsWith("file:")) {
                path = path.substring(5);
            }

            // 处理Windows路径开头的斜杠
            if (path.startsWith("/") && path.indexOf(":") > 0) {
                path = path.substring(1);
            }

            File file = new File(path);
            if (file.isFile()) {
                // 如果是jar文件，返回jar所在目录
                return file.getParent();
            } else {
                // 如果是目录，查找项目根目录
                // 向上查找直到找到包含src或pom.xml的目录
                File current = file;
                while (current != null) {
                    if (new File(current, "pom.xml").exists() ||
                            new File(current, "src").exists() ||
                            new File(current, DATA_DIR).exists()) {
                        return current.getAbsolutePath();
                    }
                    current = current.getParentFile();
                }
            }

            return userDir;
        } catch (Exception e) {
            return System.getProperty("user.dir");
        }
    }
}