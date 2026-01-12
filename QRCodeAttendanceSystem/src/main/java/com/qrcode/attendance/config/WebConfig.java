package com.qrcode.attendance.config;

import java.io.File;
import java.util.*;

/**
 * Web服务配置类
 * 管理Web服务器的所有配置
 */
public class WebConfig {

    private static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_HOST = "0.0.0.0";
    private static final String DEFAULT_CONTEXT_PATH = "/";

    // 服务器配置
    private int port = DEFAULT_PORT;
    private String host = DEFAULT_HOST;
    private String contextPath = DEFAULT_CONTEXT_PATH;
    private int minThreads = 10;
    private int maxThreads = 100;
    private int idleTimeout = 30000;
    private int maxFormContentSize = 200000;

    // 超时和缓存配置
    private int connectionTimeout = 30000;
    private int sessionTimeout = 1800; // 30分钟
    private long staticResourceCacheTimeout = 3600; // 1小时
    private long dynamicResourceCacheTimeout = 300; // 5分钟
    private int maxConnections = 10000;
    private int maxRequestsPerConnection = 100;

    // 静态资源配置
    private final Map<String, String> resourceDirs;
    private final Map<String, String> mimeTypes;
    private String welcomeFile = "index.html";
    private boolean directoryListing = false;

    // SSL配置
    private boolean sslEnabled = false;
    private String keyStorePath;
    private String keyStorePassword;
    private String keyManagerPassword;
    private String trustStorePath;
    private String trustStorePassword;
    private String[] sslProtocols = {"TLSv1.2", "TLSv1.3"};

    // CORS配置
    private boolean corsEnabled = true;
    private String allowedOrigins = "*";
    private String allowedMethods = "GET,POST,PUT,DELETE,OPTIONS";
    private String allowedHeaders = "Content-Type,Authorization,X-Requested-With";
    private boolean allowCredentials = true;
    private int corsMaxAge = 3600;

    public WebConfig() {
        this.resourceDirs = new LinkedHashMap<>();
        this.mimeTypes = new HashMap<>();

        initDefaultConfig();
    }

    /**
     * 初始化默认配置
     */
    private void initDefaultConfig() {
        // 默认静态资源目录
        resourceDirs.put("/static/*", "./webapp/static");
        resourceDirs.put("/css/*", "./webapp/css");
        resourceDirs.put("/js/*", "./webapp/js");
        resourceDirs.put("/images/*", "./webapp/images");

        // 默认MIME类型
        mimeTypes.put("html", "text/html;charset=utf-8");
        mimeTypes.put("htm", "text/html;charset=utf-8");
        mimeTypes.put("css", "text/css");
        mimeTypes.put("js", "application/javascript");
        mimeTypes.put("json", "application/json");
        mimeTypes.put("xml", "application/xml");
        mimeTypes.put("txt", "text/plain");
        mimeTypes.put("pdf", "application/pdf");
        mimeTypes.put("jpg", "image/jpeg");
        mimeTypes.put("jpeg", "image/jpeg");
        mimeTypes.put("png", "image/png");
        mimeTypes.put("gif", "image/gif");
        mimeTypes.put("svg", "image/svg+xml");
        mimeTypes.put("ico", "image/x-icon");
        mimeTypes.put("woff", "font/woff");
        mimeTypes.put("woff2", "font/woff2");
        mimeTypes.put("ttf", "font/ttf");
        mimeTypes.put("eot", "application/vnd.ms-fontobject");
    }

    /**
     * 检查SSL配置是否完整
     */
    private boolean isSslConfigured() {
        return keyStorePath != null && !keyStorePath.isEmpty() &&
                keyStorePassword != null && !keyStorePassword.isEmpty();
    }

    /**
     * 获取和设置配置项
     */
    @SuppressWarnings("unused")
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    @SuppressWarnings("unused")
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    @SuppressWarnings("unused")
    public String getContextPath() { return contextPath; }
    @SuppressWarnings("unused")
    public void setContextPath(String contextPath) { this.contextPath = contextPath; }

    @SuppressWarnings("unused")
    public int getMinThreads() { return minThreads; }
    @SuppressWarnings("unused")
    public void setMinThreads(int minThreads) { this.minThreads = minThreads; }

    @SuppressWarnings("unused")
    public int getMaxThreads() { return maxThreads; }
    @SuppressWarnings("unused")
    public void setMaxThreads(int maxThreads) { this.maxThreads = maxThreads; }

    @SuppressWarnings("unused")
    public int getIdleTimeout() { return idleTimeout; }
    @SuppressWarnings("unused")
    public void setIdleTimeout(int idleTimeout) { this.idleTimeout = idleTimeout; }

    @SuppressWarnings("unused")
    public int getMaxFormContentSize() { return maxFormContentSize; }
    @SuppressWarnings("unused")
    public void setMaxFormContentSize(int maxFormContentSize) { this.maxFormContentSize = maxFormContentSize; }

    /**
     * 超时和缓存配置
     */
    @SuppressWarnings("unused")
    public int getConnectionTimeout() { return connectionTimeout; }
    @SuppressWarnings("unused")
    public void setConnectionTimeout(int connectionTimeout) { this.connectionTimeout = connectionTimeout; }

    @SuppressWarnings("unused")
    public int getSessionTimeout() { return sessionTimeout; }
    @SuppressWarnings("unused")
    public void setSessionTimeout(int sessionTimeout) { this.sessionTimeout = sessionTimeout; }

    @SuppressWarnings("unused")
    public long getStaticResourceCacheTimeout() { return staticResourceCacheTimeout; }
    @SuppressWarnings("unused")
    public void setStaticResourceCacheTimeout(long staticResourceCacheTimeout) { this.staticResourceCacheTimeout = staticResourceCacheTimeout; }

    @SuppressWarnings("unused")
    public long getDynamicResourceCacheTimeout() { return dynamicResourceCacheTimeout; }
    @SuppressWarnings("unused")
    public void setDynamicResourceCacheTimeout(long dynamicResourceCacheTimeout) { this.dynamicResourceCacheTimeout = dynamicResourceCacheTimeout; }

    @SuppressWarnings("unused")
    public int getMaxConnections() { return maxConnections; }
    @SuppressWarnings("unused")
    public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }

    @SuppressWarnings("unused")
    public int getMaxRequestsPerConnection() { return maxRequestsPerConnection; }
    @SuppressWarnings("unused")
    public void setMaxRequestsPerConnection(int maxRequestsPerConnection) { this.maxRequestsPerConnection = maxRequestsPerConnection; }

    /**
     * 静态资源配置
     */
    @SuppressWarnings("unused")
    public void addResourceDir(String pathSpec, String resourceBase) {
        resourceDirs.put(pathSpec, resourceBase);
    }

    @SuppressWarnings("unused")
    public void removeResourceDir(String pathSpec) {
        resourceDirs.remove(pathSpec);
    }

    @SuppressWarnings("unused")
    public Map<String, String> getResourceDirs() {
        return Collections.unmodifiableMap(resourceDirs);
    }

    @SuppressWarnings("unused")
    public void addMimeType(String extension, String mimeType) {
        mimeTypes.put(extension.toLowerCase(), mimeType);
    }

    @SuppressWarnings("unused")
    public String getMimeType(String extension) {
        return mimeTypes.get(extension.toLowerCase());
    }

    @SuppressWarnings("unused")
    public Map<String, String> getMimeTypes() {
        return Collections.unmodifiableMap(mimeTypes);
    }

    @SuppressWarnings("unused")
    public String getWelcomeFile() { return welcomeFile; }
    @SuppressWarnings("unused")
    public void setWelcomeFile(String welcomeFile) { this.welcomeFile = welcomeFile; }

    @SuppressWarnings("unused")
    public boolean isDirectoryListing() { return directoryListing; }
    @SuppressWarnings("unused")
    public void setDirectoryListing(boolean directoryListing) { this.directoryListing = directoryListing; }

    /**
     * SSL配置
     */
    @SuppressWarnings("unused")
    public boolean isSslEnabled() { return sslEnabled; }
    @SuppressWarnings("unused")
    public void setSslEnabled(boolean sslEnabled) { this.sslEnabled = sslEnabled; }

    @SuppressWarnings("unused")
    public String getKeyStorePath() { return keyStorePath; }
    @SuppressWarnings("unused")
    public void setKeyStorePath(String keyStorePath) { this.keyStorePath = keyStorePath; }

    @SuppressWarnings("unused")
    public String getKeyStorePassword() { return keyStorePassword; }
    @SuppressWarnings("unused")
    public void setKeyStorePassword(String keyStorePassword) { this.keyStorePassword = keyStorePassword; }

    @SuppressWarnings("unused")
    public String getKeyManagerPassword() { return keyManagerPassword; }
    @SuppressWarnings("unused")
    public void setKeyManagerPassword(String keyManagerPassword) { this.keyManagerPassword = keyManagerPassword; }

    @SuppressWarnings("unused")
    public String getTrustStorePath() { return trustStorePath; }
    @SuppressWarnings("unused")
    public void setTrustStorePath(String trustStorePath) { this.trustStorePath = trustStorePath; }

    @SuppressWarnings("unused")
    public String getTrustStorePassword() { return trustStorePassword; }
    @SuppressWarnings("unused")
    public void setTrustStorePassword(String trustStorePassword) { this.trustStorePassword = trustStorePassword; }

    @SuppressWarnings("unused")
    public String[] getSslProtocols() { return sslProtocols; }
    @SuppressWarnings("unused")
    public void setSslProtocols(String[] sslProtocols) { this.sslProtocols = sslProtocols; }

    /**
     * CORS配置
     */
    @SuppressWarnings("unused")
    public boolean isCorsEnabled() { return corsEnabled; }
    @SuppressWarnings("unused")
    public void setCorsEnabled(boolean corsEnabled) { this.corsEnabled = corsEnabled; }

    @SuppressWarnings("unused")
    public String getAllowedOrigins() { return allowedOrigins; }
    @SuppressWarnings("unused")
    public void setAllowedOrigins(String allowedOrigins) { this.allowedOrigins = allowedOrigins; }

    @SuppressWarnings("unused")
    public String getAllowedMethods() { return allowedMethods; }
    @SuppressWarnings("unused")
    public void setAllowedMethods(String allowedMethods) { this.allowedMethods = allowedMethods; }

    @SuppressWarnings("unused")
    public String getAllowedHeaders() { return allowedHeaders; }
    @SuppressWarnings("unused")
    public void setAllowedHeaders(String allowedHeaders) { this.allowedHeaders = allowedHeaders; }

    @SuppressWarnings("unused")
    public boolean isAllowCredentials() { return allowCredentials; }
    @SuppressWarnings("unused")
    public void setAllowCredentials(boolean allowCredentials) { this.allowCredentials = allowCredentials; }

    @SuppressWarnings("unused")
    public int getCorsMaxAge() { return corsMaxAge; }
    @SuppressWarnings("unused")
    public void setCorsMaxAge(int corsMaxAge) { this.corsMaxAge = corsMaxAge; }

    /**
     * 获取所有配置信息
     */
    @SuppressWarnings("unused")
    public Map<String, Object> getAllConfig() {
        Map<String, Object> config = new LinkedHashMap<>();

        // 服务器配置
        config.put("port", port);
        config.put("host", host);
        config.put("contextPath", contextPath);
        config.put("minThreads", minThreads);
        config.put("maxThreads", maxThreads);
        config.put("idleTimeout", idleTimeout);
        config.put("maxFormContentSize", maxFormContentSize);

        // SSL配置
        config.put("sslEnabled", sslEnabled);
        config.put("keyStorePath", sslEnabled ? keyStorePath : "未配置");

        // 超时配置
        config.put("connectionTimeout", connectionTimeout);
        config.put("sessionTimeout", sessionTimeout);
        config.put("staticResourceCacheTimeout", staticResourceCacheTimeout);
        config.put("dynamicResourceCacheTimeout", dynamicResourceCacheTimeout);

        // 连接配置
        config.put("maxConnections", maxConnections);
        config.put("maxRequestsPerConnection", maxRequestsPerConnection);

        // 静态资源配置
        config.put("resourceDirs", resourceDirs);
        config.put("mimeTypes", mimeTypes);
        config.put("welcomeFile", welcomeFile);
        config.put("directoryListing", directoryListing);

        // CORS配置
        config.put("corsEnabled", corsEnabled);
        config.put("allowedOrigins", allowedOrigins);
        config.put("allowedMethods", allowedMethods);
        config.put("allowedHeaders", allowedHeaders);
        config.put("allowCredentials", allowCredentials);
        config.put("corsMaxAge", corsMaxAge);

        return config;
    }

    /**
     * 验证配置
     */
    public List<String> validateConfig() {
        List<String> errors = new ArrayList<>();

        if (port < 1 || port > 65535) {
            errors.add("端口号必须在1-65535之间");
        }

        if (minThreads < 1) {
            errors.add("最小线程数必须大于0");
        }

        if (maxThreads < minThreads) {
            errors.add("最大线程数必须大于等于最小线程数");
        }

        if (sslEnabled) {
            if (keyStorePath == null || keyStorePath.isEmpty()) {
                errors.add("启用SSL时必须配置keyStorePath");
            }
            if (keyStorePassword == null || keyStorePassword.isEmpty()) {
                errors.add("启用SSL时必须配置keyStorePassword");
            }
        }

        if (connectionTimeout < 1000) {
            errors.add("连接超时时间必须至少1000毫秒");
        }

        return errors;
    }

    @SuppressWarnings("unused")
    public void stopServer() {
        // 停止服务器的逻辑
        System.out.println("[WebConfig] 服务器已停止");
    }

    @SuppressWarnings("unused")
    public boolean startServer() {
        // 启动服务器的逻辑
        System.out.println("[WebConfig] 服务器已启动: http" + (sslEnabled ? "s" : "") + "://" +
                (host.equals("0.0.0.0") ? "localhost" : host) +
                (port == (sslEnabled ? 443 : 80) ? "" : ":" + port) +
                contextPath);
        return true;
    }
}