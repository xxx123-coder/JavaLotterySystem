package com.qrcode.attendance.config;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 配置管理器
 * 提供配置的统一管理接口
 */
public class ConfigManager {

    // 配置源类型
    public enum ConfigSource {
        FILE,           // 文件配置源
        DATABASE,       // 数据库配置源
        MEMORY,         // 内存配置源
        ENVIRONMENT,    // 环境变量配置源
        COMMAND_LINE    // 命令行参数配置源
    }

    // 配置优先级（数字越小优先级越高）
    private static final Map<ConfigSource, Integer> SOURCE_PRIORITY = new EnumMap<>(ConfigSource.class);
    static {
        SOURCE_PRIORITY.put(ConfigSource.COMMAND_LINE, 1);
        SOURCE_PRIORITY.put(ConfigSource.ENVIRONMENT, 2);
        SOURCE_PRIORITY.put(ConfigSource.MEMORY, 3);
        SOURCE_PRIORITY.put(ConfigSource.FILE, 4);
        SOURCE_PRIORITY.put(ConfigSource.DATABASE, 5);
    }

    // 配置项类
    public static class ConfigItem {
        private final String key;
        private String value;
        private ConfigSource source;
        private long timestamp;
        private long version;
        private String description;
        private boolean sensitive;  // 是否为敏感信息（如密码）
        private boolean readOnly;   // 是否为只读配置
        private final Map<String, Object> metadata;

        public ConfigItem(String key, String value, ConfigSource source) {
            this.key = key;
            this.value = value;
            this.source = source;
            this.timestamp = System.currentTimeMillis();
            this.version = 1;
            this.metadata = new HashMap<>();
        }

        // Getter和Setter方法
        public String getKey() { return key; }

        public String getValue() { return value; }
        @SuppressWarnings("unused")
        public void setValue(String value) { this.value = value; }

        public ConfigSource getSource() { return source; }
        @SuppressWarnings("unused")
        public void setSource(ConfigSource source) { this.source = source; }

        public long getTimestamp() { return timestamp; }
        @SuppressWarnings("unused")
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

        public long getVersion() { return version; }
        public void setVersion(long version) { this.version = version; }

        public String getDescription() { return description; }
        @SuppressWarnings("unused")
        public void setDescription(String description) { this.description = description; }

        public boolean isSensitive() { return sensitive; }
        @SuppressWarnings("unused")
        public void setSensitive(boolean sensitive) { this.sensitive = sensitive; }

        public boolean isReadOnly() { return readOnly; }
        @SuppressWarnings("unused")
        public void setReadOnly(boolean readOnly) { this.readOnly = readOnly; }

        @SuppressWarnings("unused")
        public Map<String, Object> getMetadata() { return metadata; }
        @SuppressWarnings("unused")
        public void addMetadata(String key, Object value) { this.metadata.put(key, value); }

        @Override
        public String toString() {
            return String.format("ConfigItem{key='%s', value='%s', source=%s, version=%d}",
                    key, sensitive ? "***" : value, source, version);
        }
    }

    // 配置变更监听器
    @FunctionalInterface
    public interface ConfigUpdateListener {
        void onConfigUpdated(String key, ConfigItem oldItem, ConfigItem newItem);

        default void onConfigRemoved(String key, ConfigItem removedItem) {
            // 默认实现
        }

        default void onConfigAdded(String key, ConfigItem newItem) {
            // 默认实现
        }
    }

    // 配置历史记录
    public static class ConfigHistory {
        private final String key;
        private final List<ConfigItem> history;
        private final int maxHistorySize;

        public ConfigHistory(String key, int maxHistorySize) {
            this.key = key;
            this.history = new ArrayList<>();
            this.maxHistorySize = maxHistorySize;
        }

        public void addVersion(ConfigItem item) {
            history.add(item);
            // 限制历史记录大小
            if (history.size() > maxHistorySize) {
                history.remove(0);
            }
        }

        public ConfigItem getLatest() {
            return history.isEmpty() ? null : history.get(history.size() - 1);
        }

        public List<ConfigItem> getHistory() {
            return new ArrayList<>(history);
        }

        @SuppressWarnings("unused")
        public ConfigItem getVersion(long version) {
            for (ConfigItem item : history) {
                if (item.getVersion() == version) {
                    return item;
                }
            }
            return null;
        }

        public boolean rollback(long version) {
            for (int i = history.size() - 1; i >= 0; i--) {
                ConfigItem item = history.get(i);
                if (item.getVersion() == version) {
                    // 将旧版本添加到历史记录作为新版本
                    ConfigItem rollbackItem = new ConfigItem(
                            item.getKey(), item.getValue(), ConfigSource.MEMORY
                    );
                    rollbackItem.setVersion(item.getVersion() + 1);
                    history.add(rollbackItem);
                    return true;
                }
            }
            return false;
        }
    }

    // 单例实例
    private static volatile ConfigManager instance;

    // 配置存储
    private final Map<String, ConfigItem> configStore;
    private final Map<String, ConfigHistory> configHistory;
    private final List<ConfigUpdateListener> listeners;
    private final Map<ConfigSource, Properties> sourceProperties;

    // 热更新相关
    private final ScheduledExecutorService scheduler;
    private final Map<String, FileWatchTask> fileWatchTasks;
    private boolean hotReloadEnabled;
    private long hotReloadInterval;

    // 子配置管理器
    private AppConfig appConfig;
    private ExcelConfig excelConfig;
    private WebConfig webConfig;

    private ConfigManager() {
        this.configStore = new ConcurrentHashMap<>();
        this.configHistory = new ConcurrentHashMap<>();
        this.listeners = new CopyOnWriteArrayList<>();
        this.sourceProperties = new EnumMap<>(ConfigSource.class);

        // 初始化各个配置源
        for (ConfigSource source : ConfigSource.values()) {
            sourceProperties.put(source, new Properties());
        }

        // 热更新配置
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.fileWatchTasks = new ConcurrentHashMap<>();
        this.hotReloadEnabled = false;
        this.hotReloadInterval = 5000; // 5秒

        // 初始化子配置
        initSubConfigs();

        // 加载配置
        loadAllConfigs();
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            synchronized (ConfigManager.class) {
                if (instance == null) {
                    instance = new ConfigManager();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化子配置管理器
     */
    private void initSubConfigs() {
        this.appConfig = AppConfig.getInstance();
        this.excelConfig = new ExcelConfig();
        this.webConfig = new WebConfig();
    }

    /**
     * 加载所有配置源
     */
    private void loadAllConfigs() {
        // 1. 加载文件配置
        loadFileConfigs();

        // 2. 加载环境变量配置
        loadEnvironmentConfigs();

        // 3. 加载数据库配置（如果有）
        loadDatabaseConfigs();

        // 4. 合并配置
        mergeConfigs();

        // 5. 启用热更新
        if (hotReloadEnabled) {
            enableHotReload();
        }
    }

    /**
     * 加载文件配置
     */
    private void loadFileConfigs() {
        // 加载application.properties
        Properties fileProps = new Properties();
        try (InputStream is = new FileInputStream("application.properties")) {
            fileProps.load(is);
            for (String key : fileProps.stringPropertyNames()) {
                ConfigItem item = new ConfigItem(key, fileProps.getProperty(key), ConfigSource.FILE);
                sourceProperties.get(ConfigSource.FILE).setProperty(key, fileProps.getProperty(key));
                addConfigItem(item, false);
            }
            log("文件配置加载完成");
        } catch (IOException e) {
            logWarning("application.properties文件未找到，使用默认配置");
        }

        // 加载其他配置文件
        loadConfigFile("config.properties");
        loadConfigFile("database.properties");
        loadConfigFile("security.properties");
    }

    private void loadConfigFile(String fileName) {
        File configFile = new File(fileName);
        if (configFile.exists()) {
            Properties props = new Properties();
            try (InputStream is = new FileInputStream(configFile)) {
                props.load(is);
                for (String key : props.stringPropertyNames()) {
                    ConfigItem item = new ConfigItem(key, props.getProperty(key), ConfigSource.FILE);
                    sourceProperties.get(ConfigSource.FILE).setProperty(key, props.getProperty(key));
                    addConfigItem(item, false);
                }
                log("配置文件加载: " + fileName);
            } catch (IOException e) {
                logError("加载配置文件失败: " + fileName + " - " + e.getMessage());
            }
        }
    }

    /**
     * 加载环境变量配置
     */
    private void loadEnvironmentConfigs() {
        Map<String, String> env = System.getenv();
        for (Map.Entry<String, String> entry : env.entrySet()) {
            if (entry.getKey().startsWith("APP_")) {
                String key = entry.getKey().substring(4).toLowerCase().replace('_', '.');
                ConfigItem item = new ConfigItem(key, entry.getValue(), ConfigSource.ENVIRONMENT);
                sourceProperties.get(ConfigSource.ENVIRONMENT).setProperty(key, entry.getValue());
                addConfigItem(item, false);
            }
        }
        log("环境变量配置加载完成");
    }

    /**
     * 加载数据库配置（模拟）
     */
    private void loadDatabaseConfigs() {
        // 这里可以连接数据库加载配置
        // 为了简化，我们只模拟一些配置
        Properties dbProps = new Properties();
        dbProps.setProperty("db.connection.pool.size", "10");
        dbProps.setProperty("db.connection.timeout", "30");
        dbProps.setProperty("db.query.timeout", "60");

        for (String key : dbProps.stringPropertyNames()) {
            ConfigItem item = new ConfigItem(key, dbProps.getProperty(key), ConfigSource.DATABASE);
            sourceProperties.get(ConfigSource.DATABASE).setProperty(key, dbProps.getProperty(key));
            addConfigItem(item, false);
        }
        log("数据库配置加载完成（模拟）");
    }

    /**
     * 合并配置（根据优先级）
     */
    private void mergeConfigs() {
        // 按优先级顺序合并配置
        List<ConfigSource> sources = new ArrayList<>(SOURCE_PRIORITY.keySet());
        sources.sort(Comparator.comparingInt(SOURCE_PRIORITY::get));

        for (ConfigSource source : sources) {
            Properties props = sourceProperties.get(source);
            for (String key : props.stringPropertyNames()) {
                ConfigItem item = new ConfigItem(key, props.getProperty(key), source);

                // 检查是否已存在该配置
                ConfigItem existing = configStore.get(key);
                if (existing == null ||
                        SOURCE_PRIORITY.get(source) < SOURCE_PRIORITY.get(existing.getSource())) {
                    // 新配置优先级更高，更新
                    addConfigItem(item, true);
                }
            }
        }

        log("配置合并完成，总配置项: " + configStore.size());
    }

    /**
     * 添加配置项
     */
    private void addConfigItem(ConfigItem item, boolean notify) {
        String key = item.getKey();

        // 获取历史记录
        ConfigHistory history = configHistory.computeIfAbsent(
                key, k -> new ConfigHistory(k, 10)
        );

        // 设置版本号
        ConfigItem latest = history.getLatest();
        if (latest != null) {
            item.setVersion(latest.getVersion() + 1);
        }

        // 添加到历史记录
        history.addVersion(item);

        // 更新存储
        ConfigItem oldItem = configStore.put(key, item);

        // 通知监听器
        if (notify) {
            if (oldItem == null) {
                notifyConfigAdded(key, item);
            } else {
                notifyConfigUpdated(key, oldItem, item);
            }
        }
    }

    /**
     * 获取配置项
     */
    public String getConfig(String key) {
        ConfigItem item = configStore.get(key);
        return item != null ? item.getValue() : null;
    }

    @SuppressWarnings("unused")
    public String getConfig(String key, String defaultValue) {
        String value = getConfig(key);
        return value != null ? value : defaultValue;
    }

    @SuppressWarnings("unused")
    public int getIntConfig(String key, int defaultValue) {
        try {
            String value = getConfig(key);
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @SuppressWarnings("unused")
    public boolean getBooleanConfig(String key, boolean defaultValue) {
        String value = getConfig(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    @SuppressWarnings("unused")
    public ConfigItem getConfigItem(String key) {
        return configStore.get(key);
    }

    /**
     * 设置配置项
     */
    @SuppressWarnings("unused")
    public boolean setConfig(String key, String value) {
        return setConfig(key, value, ConfigSource.MEMORY);
    }

    public boolean setConfig(String key, String value, ConfigSource source) {
        ConfigItem existing = configStore.get(key);

        if (existing != null && existing.isReadOnly()) {
            logWarning("配置项 " + key + " 是只读的，无法修改");
            return false;
        }

        ConfigItem newItem = new ConfigItem(key, value, source);
        addConfigItem(newItem, true);

        // 更新对应配置源
        sourceProperties.get(source).setProperty(key, value);

        log("配置已更新: " + key + " = " + (newItem.isSensitive() ? "***" : value));
        return true;
    }

    /**
     * 删除配置项
     */
    @SuppressWarnings("unused")
    public boolean removeConfig(String key) {
        ConfigItem removed = configStore.remove(key);
        if (removed != null) {
            // 从所有配置源中移除
            for (Properties props : sourceProperties.values()) {
                props.remove(key);
            }

            notifyConfigRemoved(key, removed);
            log("配置已删除: " + key);
            return true;
        }
        return false;
    }

    /**
     * 获取所有配置项
     */
    @SuppressWarnings("unused")
    public Map<String, String> getAllConfigs() {
        Map<String, String> allConfigs = new TreeMap<>();
        for (Map.Entry<String, ConfigItem> entry : configStore.entrySet()) {
            ConfigItem item = entry.getValue();
            allConfigs.put(entry.getKey(),
                    item.isSensitive() ? "***" : item.getValue());
        }
        return allConfigs;
    }

    /**
     * 获取配置历史
     */
    @SuppressWarnings("unused")
    public List<ConfigItem> getConfigHistory(String key) {
        ConfigHistory history = configHistory.get(key);
        return history != null ? history.getHistory() : Collections.emptyList();
    }

    /**
     * 回滚配置到指定版本
     */
    @SuppressWarnings("unused")
    public boolean rollbackConfig(String key, long version) {
        ConfigHistory history = configHistory.get(key);
        if (history != null) {
            boolean success = history.rollback(version);
            if (success) {
                ConfigItem rollbackItem = history.getLatest();
                configStore.put(key, rollbackItem);
                notifyConfigUpdated(key, null, rollbackItem);
                log("配置已回滚: " + key + " 到版本 " + version);
                return true;
            }
        }
        return false;
    }

    /**
     * 监听器管理
     */
    @SuppressWarnings("unused")
    public void addConfigUpdateListener(ConfigUpdateListener listener) {
        listeners.add(listener);
    }

    @SuppressWarnings("unused")
    public void removeConfigUpdateListener(ConfigUpdateListener listener) {
        listeners.remove(listener);
    }

    private void notifyConfigUpdated(String key, ConfigItem oldItem, ConfigItem newItem) {
        for (ConfigUpdateListener listener : listeners) {
            try {
                listener.onConfigUpdated(key, oldItem, newItem);
            } catch (Exception e) {
                logError("通知配置更新监听器失败: " + e.getMessage());
            }
        }
    }

    private void notifyConfigAdded(String key, ConfigItem newItem) {
        for (ConfigUpdateListener listener : listeners) {
            try {
                listener.onConfigAdded(key, newItem);
            } catch (Exception e) {
                logError("通知配置添加监听器失败: " + e.getMessage());
            }
        }
    }

    private void notifyConfigRemoved(String key, ConfigItem removedItem) {
        for (ConfigUpdateListener listener : listeners) {
            try {
                listener.onConfigRemoved(key, removedItem);
            } catch (Exception e) {
                logError("通知配置删除监听器失败: " + e.getMessage());
            }
        }
    }

    /**
     * 热更新机制
     */
    public void enableHotReload() {
        if (!hotReloadEnabled) {
            hotReloadEnabled = true;

            // 定期检查配置文件变化
            scheduler.scheduleAtFixedRate(this::checkFileChanges,
                    hotReloadInterval, hotReloadInterval, TimeUnit.MILLISECONDS);

            log("热更新已启用，检查间隔: " + hotReloadInterval + "ms");
        }
    }

    public void disableHotReload() {
        if (hotReloadEnabled) {
            hotReloadEnabled = false;
            log("热更新已禁用");
        }
    }

    @SuppressWarnings("unused")
    public void setHotReloadInterval(long interval) {
        this.hotReloadInterval = interval;
        if (hotReloadEnabled) {
            disableHotReload();
            enableHotReload();
        }
    }

    @SuppressWarnings("unused")
    public void watchFile(String filePath) {
        fileWatchTasks.computeIfAbsent(filePath, path -> {
            FileWatchTask task = new FileWatchTask(path);
            scheduler.scheduleAtFixedRate(task, 0, hotReloadInterval, TimeUnit.MILLISECONDS);
            return task;
        });
        log("开始监控文件: " + filePath);
    }

    @SuppressWarnings("unused")
    public void unwatchFile(String filePath) {
        FileWatchTask task = fileWatchTasks.remove(filePath);
        if (task != null) {
            task.stop();
            log("停止监控文件: " + filePath);
        }
    }

    private void checkFileChanges() {
        fileWatchTasks.values().forEach(task -> {
            if (task.hasChanged()) {
                log("检测到文件变化: " + task.getFilePath());
                reloadFileConfig(task.getFilePath());
            }
        });
    }

    private void reloadFileConfig(String filePath) {
        Properties props = new Properties();
        try (InputStream is = new FileInputStream(filePath)) {
            props.load(is);

            // 更新配置
            for (String key : props.stringPropertyNames()) {
                setConfig(key, props.getProperty(key), ConfigSource.FILE);
            }

            log("配置文件已重新加载: " + filePath);
        } catch (IOException e) {
            logError("重新加载配置文件失败: " + filePath + " - " + e.getMessage());
        }
    }

    /**
     * 文件监控任务
     */
    private static class FileWatchTask implements Runnable {
        private final String filePath;
        private long lastModified;
        private volatile boolean running;

        public FileWatchTask(String filePath) {
            this.filePath = filePath;
            this.running = true;
            File file = new File(filePath);
            this.lastModified = file.exists() ? file.lastModified() : 0;
        }

        @Override
        public void run() {
            if (running) {
                File file = new File(filePath);
                if (file.exists()) {
                    long currentModified = file.lastModified();
                    if (currentModified > lastModified) {
                        lastModified = currentModified;
                    }
                }
            }
        }

        public boolean hasChanged() {
            File file = new File(filePath);
            return file.exists() && file.lastModified() > lastModified;
        }

        public String getFilePath() {
            return filePath;
        }

        public void stop() {
            running = false;
        }
    }

    /**
     * 导入导出功能
     */
    @SuppressWarnings("unused")
    public void exportConfigs(String filePath, boolean includeSensitive) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("# 配置导出");
            writer.println("# 导出时间: " + new Date());
            writer.println("# 总配置项: " + configStore.size());
            writer.println();

            for (Map.Entry<String, ConfigItem> entry : configStore.entrySet()) {
                ConfigItem item = entry.getValue();
                if (!includeSensitive && item.isSensitive()) {
                    continue;
                }

                writer.println("# " + item.getKey());
                writer.println("#   来源: " + item.getSource());
                writer.println("#   版本: " + item.getVersion());
                writer.println("#   时间: " + new Date(item.getTimestamp()));
                if (item.getDescription() != null) {
                    writer.println("#   描述: " + item.getDescription());
                }
                writer.println(item.getKey() + "=" + item.getValue());
                writer.println();
            }

            log("配置已导出到: " + filePath);
        } catch (IOException e) {
            logError("导出配置失败: " + e.getMessage());
        }
    }

    @SuppressWarnings("unused")
    public void importConfigs(String filePath, boolean overwrite) {
        Properties props = new Properties();
        try (InputStream is = new FileInputStream(filePath)) {
            props.load(is);

            int imported = 0;
            int skipped = 0;

            for (String key : props.stringPropertyNames()) {
                if (configStore.containsKey(key) && !overwrite) {
                    skipped++;
                    continue;
                }

                setConfig(key, props.getProperty(key), ConfigSource.FILE);
                imported++;
            }

            log("配置导入完成: 导入" + imported + "项，跳过" + skipped + "项");
        } catch (IOException e) {
            logError("导入配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取子配置管理器
     */
    @SuppressWarnings("unused")
    public AppConfig getAppConfig() {
        return appConfig;
    }

    @SuppressWarnings("unused")
    public ExcelConfig getExcelConfig() {
        return excelConfig;
    }

    @SuppressWarnings("unused")
    public WebConfig getWebConfig() {
        return webConfig;
    }

    /**
     * 重新加载所有配置
     */
    @SuppressWarnings("unused")
    public void reloadAll() {
        log("重新加载所有配置...");

        // 清空配置
        configStore.clear();
        configHistory.clear();
        for (Properties props : sourceProperties.values()) {
            props.clear();
        }

        // 重新加载
        loadAllConfigs();

        log("所有配置已重新加载");
    }

    /**
     * 打印配置摘要
     */
    @SuppressWarnings("unused")
    public void printConfigSummary() {
        System.out.println("========== 配置管理器摘要 ==========");
        System.out.println("总配置项: " + configStore.size());
        System.out.println("配置源统计:");

        Map<ConfigSource, Integer> sourceCount = new EnumMap<>(ConfigSource.class);
        for (ConfigItem item : configStore.values()) {
            sourceCount.merge(item.getSource(), 1, Integer::sum);
        }

        for (Map.Entry<ConfigSource, Integer> entry : sourceCount.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }

        System.out.println("监听器数量: " + listeners.size());
        System.out.println("热更新: " + (hotReloadEnabled ? "启用" : "禁用"));
        System.out.println("监控文件: " + fileWatchTasks.size());
        System.out.println("===================================");
    }

    /**
     * 清理资源
     */
    public void shutdown() {
        disableHotReload();
        scheduler.shutdown();

        for (FileWatchTask task : fileWatchTasks.values()) {
            task.stop();
        }
        fileWatchTasks.clear();

        if (excelConfig != null) {
            excelConfig.cleanup();
        }

        log("配置管理器已关闭");
    }

    private void log(String message) {
        System.out.println("[ConfigManager] " + message);
    }

    private void logWarning(String message) {
        System.out.println("[ConfigManager WARN] " + message);
    }

    private void logError(String message) {
        System.err.println("[ConfigManager ERROR] " + message);
    }
}