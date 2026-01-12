package com.qrcode.attendance.main;

// 添加必要的导入语句
import com.qrcode.attendance.config.AppConfig;
import com.qrcode.attendance.gui.MainWindow;
import com.qrcode.attendance.server.EmbeddedWebServer;
import com.qrcode.attendance.service.AttendanceService;
import com.qrcode.attendance.service.ClassService;
import com.qrcode.attendance.service.StudentService;
import com.qrcode.attendance.util.LoggerUtil;
import javax.swing.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * 应用程序主控制类 - 单例模式实现
 * 协调整个系统的运行，管理应用程序生命周期，提供各服务实例的访问
 *
 * @author QR签到系统开发团队
 * @version 1.0.0
 * @since 2023-01-01
 */
public class AttendanceApplication {

    private static final Logger LOGGER = LoggerUtil.getLogger(AttendanceApplication.class);
    private static volatile AttendanceApplication instance;

    // 服务实例
    private AppConfig config;
    private MainWindow mainWindow;
    private EmbeddedWebServer webServer;
    private ClassService classService;
    private StudentService studentService;
    private AttendanceService attendanceService;

    // 应用程序状态 - 移除 final 修饰符，因为状态需要修改
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private AtomicBoolean isPaused = new AtomicBoolean(false);
    private ApplicationState currentState = ApplicationState.STOPPED;

    /**
     * 应用程序状态枚举
     * 定义了应用程序可能的所有状态
     */
    public enum ApplicationState {
        STOPPED,       // 已停止
        INITIALIZING,  // 初始化中
        RUNNING,       // 运行中
        PAUSED,        // 已暂停
        STOPPING,      // 停止中
        ERROR          // 错误状态
    }

    /**
     * 私有构造函数
     * 防止外部通过new创建实例，确保单例模式
     */
    private AttendanceApplication() {
        // 单例模式，禁止外部实例化
    }

    /**
     * 获取单例实例（双重检查锁定模式）
     *
     * @return AttendanceApplication 单例实例
     */
    public static AttendanceApplication getInstance() {
        if (instance == null) {
            synchronized (AttendanceApplication.class) {
                if (instance == null) {
                    instance = new AttendanceApplication();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化应用程序
     * 启动所有必要的服务和组件
     *
     * @throws RuntimeException 初始化失败时抛出异常
     */
    public void initialize() {
        // 检查是否已在运行中
        if (isRunning.get()) {
            LOGGER.warning("应用程序已在运行中");
            return;
        }

        // 更新状态为初始化中
        setState(ApplicationState.INITIALIZING);

        try {
            // 1. 初始化配置管理器
            config = AppConfig.getInstance();

            // 2. 初始化所有业务服务
            initializeServices();

            // 3. 更新状态为运行中
            isRunning.set(true);
            setState(ApplicationState.RUNNING);

            LOGGER.info("应用程序初始化完成");

        } catch (Exception e) {
            // 4. 处理初始化失败
            setState(ApplicationState.ERROR);
            LOGGER.severe("应用程序初始化失败: " + e.getMessage());
            throw new RuntimeException("初始化失败", e);
        }
    }

    /**
     * 初始化业务服务
     * 创建并初始化所有必要的业务服务实例
     *
     * @throws Exception 服务初始化失败
     */
    private void initializeServices() throws Exception {
        try {
            // 初始化班级管理服务
            classService = ClassService.getInstance();
            classService.initialize();

            // 初始化学生管理服务
            studentService = StudentService.getInstance();
            studentService.initialize();

            // 初始化签到服务
            attendanceService = AttendanceService.getInstance();
            attendanceService.initialize();

            LOGGER.info("业务服务初始化完成");

        } catch (Exception e) {
            LOGGER.severe("业务服务初始化失败: " + e.getMessage());
            throw e; // 重新抛出异常
        }
    }

    /**
     * 暂停应用程序
     * 暂停非核心服务，保留系统基本功能
     */
    public void pause() {
        // 检查是否可暂停
        if (!isRunning.get() || isPaused.get()) {
            return;
        }

        // 更新状态
        isPaused.set(true);
        setState(ApplicationState.PAUSED);

        // 暂停相关服务
        if (webServer != null && webServer.isRunning()) {
            webServer.pause();
        }

        LOGGER.info("应用程序已暂停");
    }

    /**
     * 恢复应用程序
     * 恢复之前暂停的服务
     */
    public void resume() {
        // 检查是否可恢复
        if (!isRunning.get() || !isPaused.get()) {
            return;
        }

        // 更新状态
        isPaused.set(false);
        setState(ApplicationState.RUNNING);

        // 恢复相关服务
        if (webServer != null) {
            webServer.resume();
        }

        LOGGER.info("应用程序已恢复");
    }

    /**
     * 停止应用程序
     * 优雅地停止所有服务，释放资源
     */
    public void stop() {
        // 检查是否已在停止状态
        if (!isRunning.get()) {
            return;
        }

        // 更新状态为停止中
        setState(ApplicationState.STOPPING);

        // 停止所有服务
        stopServices();

        // 更新最终状态
        isRunning.set(false);
        isPaused.set(false);
        setState(ApplicationState.STOPPED);

        LOGGER.info("应用程序已停止");
    }

    /**
     * 停止所有服务
     * 按依赖关系逆序停止服务
     */
    private void stopServices() {
        try {
            // 1. 停止Web服务器（如果有）
            if (webServer != null && webServer.isRunning()) {
                webServer.stop();
            }

            // 2. 关闭业务服务（按依赖关系逆序）
            if (attendanceService != null) {
                attendanceService.shutdown();
            }

            if (studentService != null) {
                studentService.shutdown();
            }

            if (classService != null) {
                classService.shutdown();
            }

            LOGGER.info("所有服务已停止");

        } catch (Exception e) {
            LOGGER.warning("停止服务时发生错误: " + e.getMessage());
        }
    }

    /**
     * 处理窗口关闭事件
     * 用户点击窗口关闭按钮时调用
     */
    public void handleWindowClosing() {
        // 注意：mainWindow 可能为 null，所以使用 null 作为父组件
        int option = JOptionPane.showConfirmDialog(null,
                "确定要退出应用程序吗？",
                "退出确认",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        // 用户确认退出
        if (option == JOptionPane.YES_OPTION) {
            stop();       // 停止应用程序
            System.exit(0); // 退出JVM
        }
    }

    /**
     * 处理系统退出事件
     * 系统级退出信号（如Ctrl+C）时调用
     */
    public void handleSystemExit() {
        LOGGER.info("收到系统退出信号");
        stop();
    }

    /**
     * 设置应用程序状态
     *
     * @param state 新的应用程序状态
     */
    private void setState(ApplicationState state) {
        this.currentState = state;
        LOGGER.info("应用程序状态变更为: " + state);

        // 通知所有状态监听器
        notifyStateChange(state);
    }

    /**
     * 通知状态变化
     * 用于通知监听器应用程序状态变化（扩展点）
     *
     * @param state 新的应用程序状态
     */
    private void notifyStateChange(ApplicationState state) {
        // 这里可以添加事件总线或监听器模式的实现
        // 例如：EventBus.post(new ApplicationStateEvent(state));
    }

    /***************************** 状态查询方法 *****************************/

    /**
     * 获取当前应用程序状态
     *
     * @return 当前应用程序状态
     */
    public ApplicationState getCurrentState() {
        return currentState;
    }

    /**
     * 检查应用程序是否正在运行
     *
     * @return true 如果应用程序正在运行
     */
    public boolean isRunning() {
        return isRunning.get();
    }

    /**
     * 检查应用程序是否已暂停
     *
     * @return true 如果应用程序已暂停
     */
    public boolean isPaused() {
        return isPaused.get();
    }

    /***************************** 服务获取方法 *****************************/

    /**
     * 获取配置管理器实例
     *
     * @return 配置管理器
     */
    public AppConfig getConfig() {
        return config;
    }

    /**
     * 获取主窗口实例
     *
     * @return 主窗口
     */
    public MainWindow getMainWindow() {
        return mainWindow;
    }

    /**
     * 设置主窗口实例
     *
     * @param mainWindow 主窗口实例
     */
    public void setMainWindow(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

    /**
     * 获取Web服务器实例
     *
     * @return Web服务器
     */
    public EmbeddedWebServer getWebServer() {
        return webServer;
    }

    /**
     * 设置Web服务器实例
     *
     * @param webServer Web服务器实例
     */
    public void setWebServer(EmbeddedWebServer webServer) {
        this.webServer = webServer;
    }

    /**
     * 获取班级管理服务实例
     *
     * @return 班级管理服务
     */
    public ClassService getClassService() {
        return classService;
    }

    /**
     * 获取学生管理服务实例
     *
     * @return 学生管理服务
     */
    public StudentService getStudentService() {
        return studentService;
    }

    /**
     * 获取签到服务实例
     *
     * @return 签到服务
     */
    public AttendanceService getAttendanceService() {
        return attendanceService;
    }

    /***************************** 监控功能 *****************************/

    /**
     * 应用程序状态监控
     * 输出当前应用程序状态和系统资源使用情况
     */
    public void monitorApplicationStatus() {
        LOGGER.info("====== 应用程序状态监控 ======");
        LOGGER.info("  - 运行状态: " + (isRunning.get() ? "运行中" : "已停止"));
        LOGGER.info("  - 暂停状态: " + (isPaused.get() ? "已暂停" : "正常"));
        LOGGER.info("  - 当前状态: " + currentState);
        LOGGER.info("  - Web服务器: " + (webServer != null && webServer.isRunning() ? "运行中" : "未运行"));

        // 监控系统资源
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long maxMemory = runtime.maxMemory() / (1024 * 1024);

        LOGGER.info("  - 内存使用: " + usedMemory + "MB / " + totalMemory + "MB (最大: " + maxMemory + "MB)");
        LOGGER.info("  - 可用处理器: " + runtime.availableProcessors());
        LOGGER.info("====== 监控结束 ======");
    }
}