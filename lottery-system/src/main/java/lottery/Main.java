package lottery;

import lottery.dao.DataManager;
import lottery.service.UserService;
import lottery.service.TicketService;
import lottery.service.LotteryService;
import lottery.ui.WebServer;
import lottery.ui.ServletHandler;

/**
 * 彩票系统主入口
 * 这个类负责系统的初始化和启动
 */
public class Main {
    // 静态变量，用于存储数据管理器和Web服务器实例
    private static DataManager dataManager;
    private static WebServer webServer;

    /**
     * 主方法，程序的入口点
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 输出启动信息到控制台
        System.out.println("彩票系统启动中...");

        // 添加关闭钩子，用于程序退出时的清理工作
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // 输出关闭信息
            System.out.println("正在关闭系统...");
            // 如果Web服务器存在，停止它
            if (webServer != null) webServer.stop();
            // 如果数据管理器存在，执行关闭操作
            if (dataManager != null) dataManager.shutdown();
            // 输出关闭完成信息
            System.out.println("系统已关闭");
        }));

        try {
            // 1. 初始化数据管理器
            // 获取DataManager的单例实例
            dataManager = DataManager.getInstance();
            // 等待数据管理器初始化完成
            if (!waitForDataManagerInitialized()) {
                // 如果初始化失败，输出错误信息并退出程序
                System.err.println("数据管理器初始化失败，程序退出");
                System.exit(1);
            }

            // 2. 初始化业务服务
            // 创建用户服务，传入数据管理器实例
            UserService userService = new UserService(dataManager);
            // 创建彩票服务，传入数据管理器实例
            TicketService ticketService = new TicketService(dataManager);
            // 创建抽奖服务，传入数据管理器实例
            LotteryService lotteryService = new LotteryService(dataManager);

            // 3. 创建Servlet处理器
            // 创建ServletHandler实例
            ServletHandler servletHandler = new ServletHandler();
            // 将各个业务服务设置到ServletHandler中
            servletHandler.setUserService(userService);
            servletHandler.setTicketService(ticketService);
            servletHandler.setLotteryService(lotteryService);

            // 4. 启动Web服务器
            // 创建Web服务器实例
            webServer = new WebServer();
            // 将Servlet处理器设置到Web服务器中
            webServer.setServletHandler(servletHandler);
            // 启动Web服务器
            webServer.start();

            // 输出服务器启动成功信息
            System.out.println("Web服务器已启动，监听端口: " + webServer.getPort());
            System.out.println("按 Ctrl+C 停止服务器");

            // 5. 保持主线程运行
            // 使用无限循环保持主线程不退出，直到收到中断信号
            while (true) {
                // 每次休眠1秒，避免占用过多CPU资源
                Thread.sleep(1000);
            }

        } catch (Exception e) {
            // 捕获启动过程中的任何异常
            System.err.println("系统启动失败: " + e.getMessage());
            // 异常退出程序，状态码1表示异常终止
            System.exit(1);
        }
    }

    /**
     * 等待数据管理器初始化完成
     * @return 初始化是否成功完成
     */
    private static boolean waitForDataManagerInitialized() {
        // 最大等待时间：5秒
        int maxWait = 5000;
        // 已等待的时间
        int waited = 0;
        // 循环检查数据管理器是否已初始化
        while (!dataManager.isInitialized() && waited < maxWait) {
            try {
                // 每次检查间隔100毫秒
                Thread.sleep(100);
                // 累计已等待时间
                waited += 100;
            } catch (InterruptedException e) {
                // 如果线程被中断，恢复中断状态并返回false
                Thread.currentThread().interrupt();
                return false;
            }
        }
        // 返回数据管理器的初始化状态
        return dataManager.isInitialized();
    }
}