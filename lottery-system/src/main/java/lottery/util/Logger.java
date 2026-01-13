package lottery.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简单日志工具类
 * 支持分级日志、文件输出、控制台输出
 */
public class Logger {
    private static final ConcurrentHashMap<String, Logger> instances = new ConcurrentHashMap<>();
    private static final Logger LOGGER_INSTANCE = new Logger("Logger");

    private final String name;
    private static LogLevel globalLevel = LogLevel.INFO;

    // 日志文件配置
    private static boolean fileLoggingEnabled = false;
    private static final String logFile = "lottery.log";
    private static final int maxFileSize = 10 * 1024 * 1024; // 10MB
    private static final int maxBackupFiles = 10;

    // 日期格式
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR, FATAL
    }

    private Logger(String name) {
        this.name = name;
    }

    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    public static Logger getLogger(String name) {
        return instances.computeIfAbsent(name, Logger::new);
    }

    /**
     * 设置全局日志级别
     */
    public static void setLevel(String level) {
        try {
            globalLevel = LogLevel.valueOf(level.toUpperCase());
            LOGGER_INSTANCE.info("日志级别设置为: " + level);  // 修复1：使用现有的info方法
        } catch (IllegalArgumentException e) {
            System.err.println("无效的日志级别: " + level + "，使用默认级别 INFO");
            globalLevel = LogLevel.INFO;
        }
    }

    /**
     * 设置文件日志是否启用
     */
    public static void setFileLoggingEnabled(boolean enabled) {  // 修复2：添加setter方法
        fileLoggingEnabled = enabled;
        if (enabled) {
            ensureLogDirectory();
        }
    }

    /**
     * 确保日志目录存在
     */
    private static void ensureLogDirectory() {
        File dir = new File(PathManager.getLogDir());
        if (!dir.exists()) {
            boolean created = dir.mkdirs();  // 修复4：检查mkdirs结果
            if (!created) {
                System.err.println("无法创建日志目录: " + dir.getAbsolutePath());
            }
        }
    }

    /**
     * 获取当前日志文件路径
     */
    private static String getCurrentLogFile() {
        String dateStr = fileDateFormat.format(new Date());
        return PathManager.getLogDir() + File.separator + dateStr + "_" + logFile;
    }

    /**
     * 检查并滚动日志文件
     */
    private static void checkAndRotateLogFile() {
        if (!fileLoggingEnabled) {
            return;
        }

        String logFilePath = getCurrentLogFile();
        File logFile = new File(logFilePath);

        if (logFile.exists() && logFile.length() > maxFileSize) {
            // 滚动日志文件
            for (int i = maxBackupFiles - 1; i > 0; i--) {
                File oldFile = new File(logFilePath + "." + i);
                File newFile = new File(logFilePath + "." + (i + 1));
                if (oldFile.exists()) {
                    boolean renamed = oldFile.renameTo(newFile);  // 修复5：检查renameTo结果
                    if (!renamed) {
                        System.err.println("无法重命名日志文件: " + oldFile.getName() + " -> " + newFile.getName());
                    }
                }
            }

            File firstBackup = new File(logFilePath + ".1");
            boolean renamed = logFile.renameTo(firstBackup);  // 修复6：检查renameTo结果
            if (!renamed) {
                System.err.println("无法滚动日志文件: " + logFile.getName() + " -> " + firstBackup.getName());
            }
        }
    }

    /**
     * 写入日志到文件
     */
    private static synchronized void writeToFile(String message) {
        if (!fileLoggingEnabled) {
            return;
        }

        ensureLogDirectory();  // 修复3：确保日志目录存在
        checkAndRotateLogFile();

        String logFilePath = getCurrentLogFile();
        try (FileWriter fw = new FileWriter(logFilePath, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(message);
        } catch (IOException e) {
            System.err.println("写入日志文件失败: " + e.getMessage());
        }
    }

    /**
     * 格式化日志消息
     */
    private String formatMessage(LogLevel level, String message) {
        String timestamp = dateFormat.format(new Date());
        String threadName = Thread.currentThread().getName();
        return String.format("%s [%s] %s %s: %s",
                timestamp, threadName, level.name(), name, message);
    }

    /**
     * 记录日志
     */
    private void log(LogLevel level, String message, Throwable throwable) {
        if (level.ordinal() < globalLevel.ordinal()) {
            return;
        }

        String formattedMessage = formatMessage(level, message);

        // 控制台输出
        if (level == LogLevel.ERROR || level == LogLevel.FATAL) {
            System.err.println(formattedMessage);
            if (throwable != null) {
                throwable.printStackTrace(System.err);
            }
        } else {
            System.out.println(formattedMessage);
        }

        // 文件输出
        writeToFile(formattedMessage);
        if (throwable != null) {
            String stackTrace = getStackTrace(throwable);
            writeToFile(stackTrace);
        }
    }

    /**
     * 获取堆栈跟踪字符串
     */
    private String getStackTrace(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.toString()).append("\n");
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append("    at ").append(element.toString()).append("\n");
        }
        return sb.toString();
    }

    // 公共日志方法
    @SuppressWarnings("unused")  // 修复7：抑制未使用警告
    public void trace(String message) {
        log(LogLevel.TRACE, message, null);
    }

    public void debug(String message) {
        log(LogLevel.DEBUG, message, null);
    }

    public void info(String message) {
        log(LogLevel.INFO, message, null);
    }

    public void warn(String message) {
        log(LogLevel.WARN, message, null);
    }

    public void error(String message) {
        log(LogLevel.ERROR, message, null);
    }

    public void error(String message, Throwable throwable) {
        log(LogLevel.ERROR, message, throwable);
    }

    @SuppressWarnings("unused")  // 修复8：抑制未使用警告
    public void fatal(String message) {
        log(LogLevel.FATAL, message, null);
    }

    @SuppressWarnings("unused")  // 修复9：抑制未使用警告
    public void fatal(String message, Throwable throwable) {
        log(LogLevel.FATAL, message, throwable);
    }
}