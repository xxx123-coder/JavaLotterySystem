package com.qrcode.attendance.config;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 应用程序配置管理类
 * 负责加载和管理所有配置项
 */
public class AppConfig {

    private static final String CONFIG_FILE = "application.properties";
    private static final String DEFAULT_CONFIG_FILE = "application-default.properties";

    private final Properties properties;
    private final Map<String, Object> runtimeConfig;
    private final List<ConfigChangeListener> listeners;
    private final Map<String, String> configHistory;

    private static volatile AppConfig instance;

    /**
     * 配置变更监听器接口
     */
    @FunctionalInterface
    public interface ConfigChangeListener {
        void onConfigChanged(String key, String oldValue, String newValue);
    }

    private AppConfig() {
        this.properties = new Properties();
        this.runtimeConfig = new ConcurrentHashMap<>();
        this.listeners = new CopyOnWriteArrayList<>();
        this.configHistory = new LinkedHashMap<>();

        loadConfig();
    }

    public static AppConfig getInstance() {
        if (instance == null) {
            synchronized (AppConfig.class) {
                if (instance == null) {
                    instance = new AppConfig();
                }
            }
        }
        return instance;
    }

    /**
     * 从配置文件加载配置
     */
    private void loadConfig() {
        try {
            // 1. 加载默认配置
            InputStream defaultStream = getClass().getClassLoader()
                    .getResourceAsStream(DEFAULT_CONFIG_FILE);
            if (defaultStream != null) {
                properties.load(defaultStream);
                defaultStream.close();
            }

            // 2. 加载用户配置（覆盖默认配置）
            File configFile = new File(CONFIG_FILE);
            if (configFile.exists()) {
                try (InputStream is = new FileInputStream(configFile)) {
                    properties.load(is);
                    log("配置文件加载成功: " + CONFIG_FILE);
                }
            } else {
                // 如果配置文件不存在，使用默认值
                setDefaultValues();
                saveConfig();
            }

            // 3. 加载系统属性（最高优先级）
            properties.putAll(System.getProperties());

            // 4. 验证配置
            validateConfig();

        } catch (IOException e) {
            logError("加载配置文件失败: " + e.getMessage());
            setDefaultValues();
        }
    }

    /**
     * 设置默认配置值
     */
    private void setDefaultValues() {
        setPropertyIfAbsent("app.name", "QRCodeAttendance");
        setPropertyIfAbsent("app.version", "1.0.0");
        setPropertyIfAbsent("app.env", "development");
        setPropertyIfAbsent("app.debug", "true");
        setPropertyIfAbsent("app.locale", "zh_CN");
        setPropertyIfAbsent("app.timezone", "Asia/Shanghai");
        setPropertyIfAbsent("app.max.retry", "3");
        setPropertyIfAbsent("app.cache.enabled", "true");
        setPropertyIfAbsent("app.cache.size", "1000");
        setPropertyIfAbsent("app.cache.ttl", "3600");
        setPropertyIfAbsent("app.log.level", "INFO");
        setPropertyIfAbsent("app.log.path", "./logs");
        setPropertyIfAbsent("app.log.retain.days", "30");
    }

    private void setPropertyIfAbsent(String key, String value) {
        if (!properties.containsKey(key)) {
            properties.setProperty(key, value);
        }
    }

    /**
     * 获取配置项（支持默认值）
     */
    public String getProperty(String key) {
        return getProperty(key, null);
    }

    public String getProperty(String key, String defaultValue) {
        // 1. 检查运行时配置（最高优先级）
        Object runtimeValue = runtimeConfig.get(key);
        if (runtimeValue != null) {
            return runtimeValue.toString();
        }

        // 2. 检查配置文件
        String value = properties.getProperty(key);
        if (value != null) {
            return value;
        }

        // 3. 返回默认值
        return defaultValue;
    }

    @SuppressWarnings("unused")
    public int getIntProperty(String key, int defaultValue) {
        try {
            String value = getProperty(key);
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @SuppressWarnings("unused")
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    @SuppressWarnings("unused")
    public long getLongProperty(String key, long defaultValue) {
        try {
            String value = getProperty(key);
            return value != null ? Long.parseLong(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @SuppressWarnings("unused")
    public List<String> getListProperty(String key, List<String> defaultValue) {
        String value = getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return Arrays.asList(value.split(","));
    }

    /**
     * 设置配置项（动态更新）
     */
    public void setProperty(String key, String value) {
        String oldValue = getProperty(key);

        // 添加到运行时配置
        runtimeConfig.put(key, value);

        // 记录历史
        String timestamp = new java.util.Date().toString();
        configHistory.put(timestamp + ":" + key, oldValue + " -> " + value);

        // 通知监听器
        notifyListeners(key, oldValue, value);

        log("配置已更新: " + key + " = " + value);
    }

    /**
     * 持久化配置到文件
     */
    public void saveConfig() {
        try (OutputStream os = new FileOutputStream(CONFIG_FILE)) {
            properties.store(os, "Application Configuration - Updated: " + new Date());
            log("配置已保存到文件: " + CONFIG_FILE);
        } catch (IOException e) {
            logError("保存配置文件失败: " + e.getMessage());
        }
    }

    /**
     * 添加配置变更监听器
     */
    @SuppressWarnings("unused")
    public void addConfigChangeListener(ConfigChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * 移除配置变更监听器
     */
    @SuppressWarnings("unused")
    public void removeConfigChangeListener(ConfigChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * 通知所有监听器配置变更
     */
    private void notifyListeners(String key, String oldValue, String newValue) {
        for (ConfigChangeListener listener : listeners) {
            try {
                listener.onConfigChanged(key, oldValue, newValue);
            } catch (Exception e) {
                logError("配置变更监听器执行失败: " + e.getMessage());
            }
        }
    }

    /**
     * 验证配置项合法性
     */
    private void validateConfig() {
        Map<String, Validator> validators = new HashMap<>();

        // 定义验证规则
        validators.put("app.version", value -> value.matches("^\\d+\\.\\d+\\.\\d+$"));
        validators.put("app.env", value -> Arrays.asList("development", "test", "production").contains(value));
        validators.put("app.max.retry", value -> {
            try {
                int retry = Integer.parseInt(value);
                return retry >= 0 && retry <= 10;
            } catch (NumberFormatException e) {
                return false;
            }
        });
        validators.put("app.cache.size", value -> {
            try {
                int size = Integer.parseInt(value);
                return size >= 0 && size <= 100000;
            } catch (NumberFormatException e) {
                return false;
            }
        });

        // 执行验证
        for (Map.Entry<String, Validator> entry : validators.entrySet()) {
            String key = entry.getKey();
            String value = getProperty(key);
            if (value != null && !entry.getValue().validate(value)) {
                logWarning("配置项验证失败: " + key + " = " + value);
            }
        }
    }

    /**
     * 验证器接口
     */
    @FunctionalInterface
    private interface Validator {
        boolean validate(String value);
    }

    /**
     * 打印所有配置项
     */
    public void printAllConfig() {
        System.out.println("========== 应用程序配置 ==========");
        System.out.println("配置文件: " + CONFIG_FILE);
        System.out.println("加载时间: " + new Date());
        System.out.println("----------------------------------");

        // 按前缀分组打印
        Map<String, List<String>> grouped = new TreeMap<>();

        for (String key : properties.stringPropertyNames()) {
            String prefix = key.contains(".") ? key.substring(0, key.indexOf('.')) : "other";
            grouped.computeIfAbsent(prefix, k -> new ArrayList<>())
                    .add(String.format("  %-30s = %s", key, getProperty(key)));
        }

        for (Map.Entry<String, List<String>> entry : grouped.entrySet()) {
            System.out.println("[" + entry.getKey().toUpperCase() + "]");
            for (String line : entry.getValue()) {
                System.out.println(line);
            }
            System.out.println();
        }

        // 打印运行时配置
        if (!runtimeConfig.isEmpty()) {
            System.out.println("[RUNTIME CONFIG]");
            for (Map.Entry<String, Object> entry : runtimeConfig.entrySet()) {
                System.out.printf("  %-30s = %s%n", entry.getKey(), entry.getValue());
            }
            System.out.println();
        }

        System.out.println("==================================");
    }

    /**
     * 导出配置到指定文件
     */
    @SuppressWarnings("unused")
    public void exportConfig(String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("# 导出时间: " + new Date());
            writer.println("# 应用程序配置");
            writer.println();

            for (String key : properties.stringPropertyNames()) {
                writer.println(key + "=" + properties.getProperty(key));
            }

            writer.println();
            writer.println("# 运行时配置");
            for (Map.Entry<String, Object> entry : runtimeConfig.entrySet()) {
                writer.println(entry.getKey() + "=" + entry.getValue());
            }

            writer.println();
            writer.println("# 配置变更历史");
            for (Map.Entry<String, String> entry : configHistory.entrySet()) {
                writer.println("# " + entry.getKey() + " : " + entry.getValue());
            }

            log("配置已导出到文件: " + filePath);
        } catch (IOException e) {
            logError("导出配置失败: " + e.getMessage());
        }
    }

    /**
     * 重新加载配置
     */
    @SuppressWarnings("unused")
    public void reload() {
        properties.clear();
        loadConfig();
        log("配置已重新加载");
    }

    private void log(String message) {
        System.out.println("[AppConfig] " + message);
    }

    private void logWarning(String message) {
        System.out.println("[AppConfig WARN] " + message);
    }

    private void logError(String message) {
        System.err.println("[AppConfig ERROR] " + message);
    }
}