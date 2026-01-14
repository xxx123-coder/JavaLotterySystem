package lottery.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简单日志工具类（简化版，仅控制台输出）
 */
public class Logger {
    private static final ConcurrentHashMap<String, Logger> instances = new ConcurrentHashMap<>();
    private static final Logger LOGGER_INSTANCE = new Logger("Logger");

    private final String name;
    private static LogLevel globalLevel = LogLevel.INFO;

    // 日期格式
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR, FATAL
    }

    private Logger(String name) {
        this.name = name;
    }

    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getSimpleName());
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
            LOGGER_INSTANCE.info("日志级别设置为: " + level);
        } catch (IllegalArgumentException e) {
            System.err.println("无效的日志级别: " + level + "，使用默认级别 INFO");
            globalLevel = LogLevel.INFO;
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
    }

    // 公共日志方法
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

    public void fatal(String message) {
        log(LogLevel.FATAL, message, null);
    }

    public void fatal(String message, Throwable throwable) {
        log(LogLevel.FATAL, message, throwable);
    }
}