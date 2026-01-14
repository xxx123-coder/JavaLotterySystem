package lottery.util;

/**
 * 路径管理器（兼容层，委托给FileUtils）
 */
public class PathManager {
    public static String getDataDir() {
        return FileUtils.getDataDir();
    }

    public static String getLogDir() {
        return FileUtils.getLogDir();
    }

    public static String getUserFilePath() {
        return FileUtils.getUserFilePath();
    }

    public static String getTicketFilePath() {
        return FileUtils.getTicketFilePath();
    }

    public static String getResultFilePath() {
        return FileUtils.getResultFilePath();
    }

    public static void ensureDirectories() {
        FileUtils.ensureAllDirectories();
    }
}