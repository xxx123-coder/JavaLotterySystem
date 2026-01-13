package lottery.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.InvalidPathException;
import java.nio.file.StandardCopyOption;
import java.util.regex.Pattern;

/**
 * 路径管理器
 * 统一管理项目的所有文件路径，增强跨平台兼容性
 */
public class PathManager {

    // 私有构造函数，防止实例化
    private PathManager() {
        throw new IllegalStateException("工具类不可实例化");
    }

    // 目录和文件名称
    private static final String DATA_DIR = "data";
    private static final String BACKUP_DIR = "backup";
    private static final String LOG_DIR = "logs";
    private static final String USERS_FILE = "users.xlsx";
    private static final String TICKETS_FILE = "tickets.xlsx";
    private static final String RESULTS_FILE = "results.xlsx";
    private static final String CONFIG_FILE = "config.properties";

    // 特殊字符过滤正则
    private static final Pattern INVALID_CHARS = Pattern.compile("[\\\\/:*?\"<>|]");

    // 缓存
    private static volatile String projectRootCache = null;
    private static volatile String dataDirCache = null;
    private static volatile String backupDirCache = null;
    private static volatile String logDirCache = null;

    /**
     * 获取项目根目录（增强版）
     */
    public static String getProjectRoot() {
        if (projectRootCache != null) {
            return projectRootCache;
        }

        synchronized (PathManager.class) {
            if (projectRootCache != null) {
                return projectRootCache;
            }

            System.out.println("[PATH] 开始定位项目根目录...");

            // 策略1：使用当前工作目录
            String currentDir = System.getProperty("user.dir");
            System.out.println("[PATH] 当前工作目录: " + currentDir);

            // 检查常见项目标记
            if (isProjectRoot(currentDir)) {
                projectRootCache = normalizePath(currentDir);
                System.out.println("[PATH] 确认为项目根目录: " + projectRootCache);
                return projectRootCache;
            }

            // 策略2：向上查找
            File parentDir = new File(currentDir).getParentFile();
            while (parentDir != null) {
                if (isProjectRoot(parentDir.getAbsolutePath())) {
                    projectRootCache = normalizePath(parentDir.getAbsolutePath());
                    System.out.println("[PATH] 向上查找到项目根目录: " + projectRootCache);
                    return projectRootCache;
                }
                parentDir = parentDir.getParentFile();
            }

            // 策略3：使用类路径
            try {
                String classpath = PathManager.class.getProtectionDomain()
                        .getCodeSource().getLocation().getPath();
                classpath = decodePath(classpath);

                File classpathFile = new File(classpath);
                File potentialRoot = classpathFile.isDirectory() ? classpathFile : classpathFile.getParentFile();

                while (potentialRoot != null) {
                    if (isProjectRoot(potentialRoot.getAbsolutePath())) {
                        projectRootCache = normalizePath(potentialRoot.getAbsolutePath());
                        System.out.println("[PATH] 从类路径找到项目根目录: " + projectRootCache);
                        return projectRootCache;
                    }
                    potentialRoot = potentialRoot.getParentFile();
                }
            } catch (Exception e) {
                System.err.println("[PATH] 类路径查找失败: " + e.getMessage());
            }

            // 策略4：创建项目结构
            System.out.println("[PATH] 未找到现有项目结构，创建新项目目录...");
            projectRootCache = normalizePath(currentDir);
            initializeProjectStructure(projectRootCache);

            return projectRootCache;
        }
    }

    /**
     * 判断是否为项目根目录
     */
    private static boolean isProjectRoot(String dirPath) {
        if (dirPath == null || dirPath.trim().isEmpty()) {
            return false;
        }

        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory()) {
            return false;
        }

        // 检查项目标记文件/目录
        File dataDir = new File(dir, DATA_DIR);
        File srcDir = new File(dir, "src");
        File pomFile = new File(dir, "pom.xml");
        File gradleFile = new File(dir, "build.gradle");
        File configFile = new File(dir, CONFIG_FILE);

        return dataDir.exists() || srcDir.exists() ||
                pomFile.exists() || gradleFile.exists() ||
                configFile.exists();
    }

    /**
     * 初始化项目结构
     */
    private static void initializeProjectStructure(String projectRoot) {
        try {
            System.out.println("[PATH] 初始化项目目录结构...");

            // 创建必要目录
            ensureDirectoryExists(getDataDir());
            ensureDirectoryExists(getBackupDir());
            ensureDirectoryExists(getLogDir());

            // 创建配置文件模板
            createDefaultConfig();

            System.out.println("[PATH] 项目目录结构初始化完成");

        } catch (Exception e) {
            System.err.println("[PATH] 初始化项目结构失败: " + e.getMessage());
        }
    }

    /**
     * 规范化路径（跨平台兼容）
     */
    public static String normalizePath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return "";
        }

        try {
            // 解码URL编码
            path = decodePath(path);

            // 规范化路径
            Path normalized = Paths.get(path).toAbsolutePath().normalize();

            // 转换为系统兼容的路径字符串
            String result = normalized.toString();

            // 确保使用正确的文件分隔符
            if (File.separator.equals("/")) {
                // Unix系统
                result = result.replace("\\", "/");
            } else {
                // Windows系统
                result = result.replace("/", "\\");
            }

            return result;

        } catch (InvalidPathException e) {
            System.err.println("[PATH] 路径规范化失败: " + path + " - " + e.getMessage());

            // 回退方案：手动清理无效字符
            String cleaned = cleanPath(path);
            return new File(cleaned).getAbsolutePath();
        }
    }

    /**
     * 解码路径（处理URL编码）
     */
    private static String decodePath(String path) {
        if (path.startsWith("file:/")) {
            try {
                path = java.net.URLDecoder.decode(path, "UTF-8");
                if (path.startsWith("file:")) {
                    path = path.substring(5);
                }
            } catch (Exception e) {
                // 忽略解码错误
            }
        }

        // 处理Windows文件URL
        if (path.startsWith("/") && path.contains(":")) {
            path = path.substring(1);
        }

        return path;
    }

    /**
     * 清理路径中的无效字符
     */
    public static String cleanPath(String path) {
        if (path == null) return "";

        // 移除控制字符和无效字符
        String cleaned = INVALID_CHARS.matcher(path).replaceAll("_");

        // 移除多余的点
        cleaned = cleaned.replaceAll("\\.{2,}", ".");

        // 移除首尾空白
        cleaned = cleaned.trim();

        return cleaned;
    }

    /**
     * 确保目录存在（增强版）
     */
    public static boolean ensureDirectoryExists(String dirPath) {
        if (dirPath == null || dirPath.trim().isEmpty()) {
            System.err.println("[PATH] 目录路径为空");
            return false;
        }

        // 清理路径
        dirPath = normalizePath(cleanPath(dirPath));

        File dir = new File(dirPath);

        if (dir.exists()) {
            if (dir.isDirectory()) {
                return true;
            } else {
                System.err.println("[PATH] 路径存在但不是目录: " + dirPath);

                // 尝试重命名冲突文件
                try {
                    File backup = new File(dirPath + ".bak");
                    if (dir.renameTo(backup)) {
                        System.out.println("[PATH] 已重命名冲突文件: " + backup.getName());
                    } else {
                        return false;
                    }
                } catch (Exception e) {
                    System.err.println("[PATH] 重命名冲突文件失败: " + e.getMessage());
                    return false;
                }
            }
        }

        // 创建目录（包括父目录）
        try {
            boolean created = dir.mkdirs();
            if (created) {
                System.out.println("[PATH] 创建目录成功: " + dirPath);
                return true;
            } else {
                // 尝试检查父目录权限
                File parent = dir.getParentFile();
                if (parent != null && !parent.canWrite()) {
                    System.err.println("[PATH] 没有写入权限: " + parent.getAbsolutePath());

                    // 尝试在用户目录下创建
                    String userHome = System.getProperty("user.home");
                    String fallbackDir = userHome + File.separator + ".lottery" + File.separator +
                            new File(dirPath).getName();

                    System.out.println("[PATH] 尝试备用目录: " + fallbackDir);
                    return ensureDirectoryExists(fallbackDir);
                }

                System.err.println("[PATH] 创建目录失败: " + dirPath);
                return false;
            }
        } catch (SecurityException e) {
            System.err.println("[PATH] 安全异常，无法创建目录: " + dirPath);
            return false;
        } catch (Exception e) {
            System.err.println("[PATH] 创建目录异常: " + dirPath + " - " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取数据目录路径
     */
    public static String getDataDir() {
        if (dataDirCache != null) {
            return dataDirCache;
        }

        synchronized (PathManager.class) {
            if (dataDirCache != null) {
                return dataDirCache;
            }

            String projectRoot = getProjectRoot();
            dataDirCache = normalizePath(projectRoot + File.separator + DATA_DIR);
            ensureDirectoryExists(dataDirCache);

            return dataDirCache;
        }
    }

    /**
     * 获取备份目录路径
     */
    public static String getBackupDir() {
        if (backupDirCache != null) {
            return backupDirCache;
        }

        synchronized (PathManager.class) {
            if (backupDirCache != null) {
                return backupDirCache;
            }

            String projectRoot = getProjectRoot();
            backupDirCache = normalizePath(projectRoot + File.separator + BACKUP_DIR);
            ensureDirectoryExists(backupDirCache);

            return backupDirCache;
        }
    }

    /**
     * 获取日志目录路径
     */
    public static String getLogDir() {
        if (logDirCache != null) {
            return logDirCache;
        }

        synchronized (PathManager.class) {
            if (logDirCache != null) {
                return logDirCache;
            }

            String projectRoot = getProjectRoot();
            logDirCache = normalizePath(projectRoot + File.separator + LOG_DIR);
            ensureDirectoryExists(logDirCache);

            return logDirCache;
        }
    }

    /**
     * 获取用户数据文件完整路径
     */
    public static String getUserFilePath() {
        String dataDir = getDataDir();
        return normalizePath(dataDir + File.separator + USERS_FILE);
    }

    /**
     * 获取彩票数据文件完整路径
     */
    public static String getTicketFilePath() {
        String dataDir = getDataDir();
        return normalizePath(dataDir + File.separator + TICKETS_FILE);
    }

    /**
     * 获取结果数据文件完整路径
     */
    public static String getResultFilePath() {
        String dataDir = getDataDir();
        return normalizePath(dataDir + File.separator + RESULTS_FILE);
    }

    /**
     * 获取配置文件路径
     */
    public static String getConfigFilePath() {
        String projectRoot = getProjectRoot();
        return normalizePath(projectRoot + File.separator + CONFIG_FILE);
    }

    /**
     * 创建默认配置文件
     */
    private static void createDefaultConfig() {
        File configFile = new File(getConfigFilePath());
        if (configFile.exists()) {
            return;
        }

        try {
            String defaultConfig =
                    "# 彩票系统配置\n" +
                            "# 服务器配置\n" +
                            "server.port=8080\n" +
                            "server.host=localhost\n" +
                            "\n" +
                            "# 数据配置\n" +
                            "excel.data.dir=data\n" +
                            "backup.enabled=true\n" +
                            "backup.interval.days=7\n" +
                            "\n" +
                            "# 抽奖配置\n" +
                            "lottery.price=2.0\n" +
                            "lottery.min.numbers=7\n" +
                            "lottery.max.numbers=36\n" +
                            "\n" +
                            "# 日志配置\n" +
                            "log.level=INFO\n" +
                            "log.max.size=10MB\n" +
                            "log.max.files=10\n";

            Files.write(configFile.toPath(), defaultConfig.getBytes("UTF-8"));
            System.out.println("[PATH] 创建默认配置文件: " + configFile.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("[PATH] 创建配置文件失败: " + e.getMessage());
        }
    }

    /**
     * 检查文件是否存在
     */
    public static boolean fileExists(String filePath) {
        if (filePath == null) {
            return false;
        }
        File file = new File(filePath);
        return file.exists() && file.isFile();
    }

    /**
     * 打印详细的路径信息
     */
    public static void printPathInfo() {
        System.out.println("\n[PATH] ========== 路径信息 ==========");
        System.out.println("[PATH] 操作系统: " + System.getProperty("os.name"));
        System.out.println("[PATH] 文件分隔符: '" + File.separator + "'");
        System.out.println("[PATH] 当前工作目录: " + System.getProperty("user.dir"));
        System.out.println("[PATH] 用户主目录: " + System.getProperty("user.home"));
        System.out.println("[PATH] 项目根目录: " + getProjectRoot());
        System.out.println("[PATH] 数据目录: " + getDataDir());
        System.out.println("[PATH] 备份目录: " + getBackupDir());
        System.out.println("[PATH] 日志目录: " + getLogDir());
        System.out.println("[PATH] 用户文件: " + getUserFilePath());
        System.out.println("[PATH] 彩票文件: " + getTicketFilePath());
        System.out.println("[PATH] 结果文件: " + getResultFilePath());
        System.out.println("[PATH] 配置文件: " + getConfigFilePath());

        // 检查文件是否存在
        System.out.println("[PATH] 用户文件存在: " + fileExists(getUserFilePath()));
        System.out.println("[PATH] 彩票文件存在: " + fileExists(getTicketFilePath()));
        System.out.println("[PATH] 结果文件存在: " + fileExists(getResultFilePath()));
        System.out.println("[PATH] 配置文件存在: " + fileExists(getConfigFilePath()));

        // 检查目录权限
        System.out.println("[PATH] 数据目录可写: " + new File(getDataDir()).canWrite());
        System.out.println("[PATH] 备份目录可写: " + new File(getBackupDir()).canWrite());
        System.out.println("[PATH] ============================\n");
    }
}