package lottery.util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 路径管理器
 * 统一管理项目的所有文件路径
 */
public class PathManager {

    // 私有构造函数，防止实例化
    private PathManager() {
        throw new IllegalStateException("工具类不可实例化");
    }

    // 数据目录名称
    private static final String DATA_DIR = "data";

    // 数据文件名
    private static final String USERS_FILE = "users.xlsx";
    private static final String TICKETS_FILE = "tickets.xlsx";
    private static final String RESULTS_FILE = "results.xlsx";

    // 配置文件名
    private static final String CONFIG_FILE = "config.properties";

    // 项目根目录缓存（避免重复计算）
    private static String projectRootCache = null;
    private static String dataDirCache = null;

    /**
     * 获取项目根目录（优化版本）
     */
    public static synchronized String getProjectRoot() {
        if (projectRootCache != null) {
            return projectRootCache;
        }

        // 策略1：使用当前工作目录（最可靠）
        String currentDir = System.getProperty("user.dir");
        System.out.println("[PATH] 当前工作目录: " + currentDir);

        // 检查当前目录是否有data文件夹或pom.xml
        File currentDirFile = new File(currentDir);
        File dataDir = new File(currentDirFile, DATA_DIR);
        File pomFile = new File(currentDirFile, "pom.xml");
        File srcDir = new File(currentDirFile, "src");

        // 如果当前目录有data目录，或者有pom.xml，或者有src目录，就认为是项目根目录
        if (dataDir.exists() || pomFile.exists() || srcDir.exists()) {
            projectRootCache = currentDirFile.getAbsolutePath();
            System.out.println("[PATH] 确认为项目根目录: " + projectRootCache);
            return projectRootCache;
        }

        // 策略2：向上查找项目根目录
        File parentDir = currentDirFile.getParentFile();
        while (parentDir != null) {
            File testDataDir = new File(parentDir, DATA_DIR);
            File testPomFile = new File(parentDir, "pom.xml");
            File testSrcDir = new File(parentDir, "src");

            if (testDataDir.exists() || testPomFile.exists() || testSrcDir.exists()) {
                projectRootCache = parentDir.getAbsolutePath();
                System.out.println("[PATH] 向上查找到项目根目录: " + projectRootCache);
                return projectRootCache;
            }
            parentDir = parentDir.getParentFile();
        }

        // 策略3：作为最后手段，使用当前目录
        projectRootCache = currentDirFile.getAbsolutePath();
        System.out.println("[PATH] 使用当前目录作为项目根目录: " + projectRootCache);
        return projectRootCache;
    }

    /**
     * 获取数据目录路径（确保目录存在）
     */
    public static synchronized String getDataDir() {
        if (dataDirCache != null) {
            return dataDirCache;
        }

        String projectRoot = getProjectRoot();
        Path dataPath = Paths.get(projectRoot, DATA_DIR);
        dataDirCache = dataPath.toString();

        // 确保目录存在
        ensureDirectoryExists(dataDirCache);

        return dataDirCache;
    }

    /**
     * 获取用户数据文件完整路径
     */
    public static String getUserFilePath() {
        String dataDir = getDataDir();
        return Paths.get(dataDir, USERS_FILE).toString();
    }

    /**
     * 获取彩票数据文件完整路径
     */
    public static String getTicketFilePath() {
        String dataDir = getDataDir();
        return Paths.get(dataDir, TICKETS_FILE).toString();
    }

    /**
     * 获取结果数据文件完整路径
     */
    public static String getResultFilePath() {
        String dataDir = getDataDir();
        return Paths.get(dataDir, RESULTS_FILE).toString();
    }

    /**
     * 获取配置文件路径
     */
    public static String getConfigFilePath() {
        String projectRoot = getProjectRoot();
        return Paths.get(projectRoot, CONFIG_FILE).toString();
    }

    /**
     * 确保目录存在（修复版）
     */
    public static boolean ensureDirectoryExists(String dirPath) {
        if (dirPath == null || dirPath.trim().isEmpty()) {
            return false;
        }

        File dir = new File(dirPath);
        if (dir.exists()) {
            if (dir.isDirectory()) {
                return true;
            } else {
                System.err.println("[ERROR] 路径存在但不是目录: " + dirPath);
                return false;
            }
        }

        // 尝试创建目录（包括所有父目录）
        boolean created = dir.mkdirs();
        if (created) {
            System.out.println("[INFO] 创建目录: " + dirPath);
            return true;
        } else {
            // 如果创建失败，尝试不同策略
            System.err.println("[WARN] 创建目录失败: " + dirPath);

            // 尝试创建父目录
            File parent = dir.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
                created = dir.mkdirs();
            }

            if (created) {
                System.out.println("[INFO] 最终创建目录成功: " + dirPath);
                return true;
            } else {
                System.err.println("[ERROR] 无法创建目录，请检查权限: " + dirPath);
                return false;
            }
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
     * 获取相对路径（相对于项目根目录）
     */
    public static String getRelativePath(String absolutePath) {
        if (absolutePath == null) {
            return "";
        }

        String projectRoot = getProjectRoot();
        try {
            Path projectPath = Paths.get(projectRoot).toAbsolutePath();
            Path filePath = Paths.get(absolutePath).toAbsolutePath();
            return projectPath.relativize(filePath).toString();
        } catch (Exception e) {
            return absolutePath;
        }
    }

    /**
     * 重置缓存（用于测试或重新加载配置）
     */
    public static synchronized void resetCache() {
        projectRootCache = null;
        dataDirCache = null;
        System.out.println("[INFO] 路径缓存已重置");
    }

    /**
     * 打印路径调试信息
     */
    public static void printPathInfo() {
        System.out.println("\n[PATH] ========== 路径信息 ==========");
        System.out.println("[PATH] 项目根目录: " + getProjectRoot());
        System.out.println("[PATH] 数据目录: " + getDataDir());
        System.out.println("[PATH] 用户文件: " + getUserFilePath());
        System.out.println("[PATH] 彩票文件: " + getTicketFilePath());
        System.out.println("[PATH] 结果文件: " + getResultFilePath());
        System.out.println("[PATH] 配置文件: " + getConfigFilePath());

        // 检查文件是否存在
        System.out.println("[PATH] 用户文件存在: " + fileExists(getUserFilePath()));
        System.out.println("[PATH] 彩票文件存在: " + fileExists(getTicketFilePath()));
        System.out.println("[PATH] 结果文件存在: " + fileExists(getResultFilePath()));
        System.out.println("[PATH] 配置文件存在: " + fileExists(getConfigFilePath()));
        System.out.println("[PATH] ============================\n");
    }
}