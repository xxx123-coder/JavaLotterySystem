package lottery.util;

/**
 * 路径管理器（兼容层，委托给FileUtils）
 * 这个类主要用于向后兼容，所有功能都委托给FileUtils类实现
 */
public class PathManager {
    /**
     * 获取数据目录路径
     * @return 数据目录的字符串路径
     */
    public static String getDataDir() {
        // 委托给FileUtils类的getDataDir方法
        return FileUtils.getDataDir();
    }

    /**
     * 获取日志目录路径
     * @return 日志目录的字符串路径
     */
    public static String getLogDir() {
        // 委托给FileUtils类的getLogDir方法
        return FileUtils.getLogDir();
    }

    /**
     * 获取用户Excel文件路径
     * @return 用户文件完整路径的字符串
     */
    public static String getUserFilePath() {
        // 委托给FileUtils类的getUserFilePath方法
        return FileUtils.getUserFilePath();
    }

    /**
     * 获取彩票Excel文件路径
     * @return 彩票文件完整路径的字符串
     */
    public static String getTicketFilePath() {
        // 委托给FileUtils类的getTicketFilePath方法
        return FileUtils.getTicketFilePath();
    }

    /**
     * 获取开奖结果Excel文件路径
     * @return 结果文件完整路径的字符串
     */
    public static String getResultFilePath() {
        // 委托给FileUtils类的getResultFilePath方法
        return FileUtils.getResultFilePath();
    }

    /**
     * 确保所有必要的目录都存在
     * 这个方法会创建数据目录和日志目录
     */
    public static void ensureDirectories() {
        // 委托给FileUtils类的ensureAllDirectories方法
        FileUtils.ensureAllDirectories();
    }
}