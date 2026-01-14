package lottery.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简单日志工具类（简化版，仅控制台输出）
 * 这个类提供了一个轻量级的日志记录功能，支持不同级别的日志输出
 */
public class Logger {
    // 使用线程安全的ConcurrentHashMap存储日志器实例，键为日志器名称
    private static final ConcurrentHashMap<String, Logger> instances = new ConcurrentHashMap<>();
    // 用于记录本类日志的日志器实例
    private static final Logger LOGGER_INSTANCE = new Logger("Logger");

    // 日志器名称，通常为类名或模块名
    private final String name;
    // 全局日志级别，控制哪些级别的日志会被输出
    private static LogLevel globalLevel = LogLevel.INFO;

    // 日期格式化器，用于生成日志时间戳
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * 日志级别枚举，定义了6个日志级别
     * 级别从低到高：TRACE < DEBUG < INFO < WARN < ERROR < FATAL
     */
    public enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR, FATAL
    }

    /**
     * 私有构造函数，防止外部直接实例化
     * @param name 日志器的名称
     */
    private Logger(String name) {
        this.name = name;
    }

    /**
     * 根据类对象获取对应的日志器实例
     * @param clazz 类对象
     * @return 日志器实例
     */
    public static Logger getLogger(Class<?> clazz) {
        // 使用类的简单名作为日志器名称
        return getLogger(clazz.getSimpleName());
    }

    /**
     * 根据名称获取日志器实例
     * @param name 日志器名称
     * @return 日志器实例
     */
    public static Logger getLogger(String name) {
        // 使用computeIfAbsent确保相同名称的日志器只创建一次
        return instances.computeIfAbsent(name, Logger::new);
    }

    /**
     * 设置全局日志级别
     * @param level 要设置的日志级别字符串
     */
    public static void setLevel(String level) {
        try {
            // 将字符串转换为大写的LogLevel枚举值
            globalLevel = LogLevel.valueOf(level.toUpperCase());
            // 记录设置日志级别的事件
            LOGGER_INSTANCE.info("日志级别设置为: " + level);
        } catch (IllegalArgumentException e) {
            // 如果传入的日志级别无效，输出错误信息并使用默认级别
            System.err.println("无效的日志级别: " + level + "，使用默认级别 INFO");
            globalLevel = LogLevel.INFO;
        }
    }

    /**
     * 格式化日志消息
     * @param level 日志级别
     * @param message 日志消息
     * @return 格式化后的完整日志消息
     */
    private String formatMessage(LogLevel level, String message) {
        // 获取当前时间戳
        String timestamp = dateFormat.format(new Date());
        // 获取当前线程名称
        String threadName = Thread.currentThread().getName();
        // 格式化日志消息：时间戳 [线程名] 日志级别 日志器名称: 日志消息
        return String.format("%s [%s] %s %s: %s",
                timestamp, threadName, level.name(), name, message);
    }

    /**
     * 记录日志的核心方法
     * @param level 日志级别
     * @param message 日志消息
     * @param throwable 异常对象，可为null
     */
    private void log(LogLevel level, String message, Throwable throwable) {
        // 如果当前日志级别低于全局级别，则不输出该日志
        if (level.ordinal() < globalLevel.ordinal()) {
            return;
        }

        // 格式化日志消息
        String formattedMessage = formatMessage(level, message);

        // 根据日志级别选择输出到标准输出还是标准错误
        if (level == LogLevel.ERROR || level == LogLevel.FATAL) {
            // 错误和致命错误输出到标准错误
            System.err.println(formattedMessage);
            // 如果有异常对象，打印异常堆栈信息
            if (throwable != null) {
                throwable.printStackTrace(System.err);
            }
        } else {
            // 其他级别的日志输出到标准输出
            System.out.println(formattedMessage);
        }
    }

    // 以下是一组公共日志方法，对应不同的日志级别
    // 每个方法都调用私有的log方法进行实际记录

    /**
     * 记录TRACE级别的日志
     * @param message 日志消息
     */
    public void trace(String message) {
        log(LogLevel.TRACE, message, null);
    }

    /**
     * 记录DEBUG级别的日志
     * @param message 日志消息
     */
    public void debug(String message) {
        log(LogLevel.DEBUG, message, null);
    }

    /**
     * 记录INFO级别的日志
     * @param message 日志消息
     */
    public void info(String message) {
        log(LogLevel.INFO, message, null);
    }

    /**
     * 记录WARN级别的日志
     * @param message 日志消息
     */
    public void warn(String message) {
        log(LogLevel.WARN, message, null);
    }

    /**
     * 记录ERROR级别的日志
     * @param message 日志消息
     */
    public void error(String message) {
        log(LogLevel.ERROR, message, null);
    }

    /**
     * 记录ERROR级别的日志（带异常）
     * @param message 日志消息
     * @param throwable 异常对象
     */
    public void error(String message, Throwable throwable) {
        log(LogLevel.ERROR, message, throwable);
    }

    /**
     * 记录FATAL级别的日志
     * @param message 日志消息
     */
    public void fatal(String message) {
        log(LogLevel.FATAL, message, null);
    }

    /**
     * 记录FATAL级别的日志（带异常）
     * @param message 日志消息
     * @param throwable 异常对象
     */
    public void fatal(String message, Throwable throwable) {
        log(LogLevel.FATAL, message, throwable);
    }
}