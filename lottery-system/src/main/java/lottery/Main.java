package lottery;

import lottery.dao.DataManager;
import lottery.service.UserService;
import lottery.service.TicketService;
import lottery.service.LotteryService;
import lottery.ui.WebServer;
import lottery.ui.ServletHandler;

/**
 * 彩票系统主入口
 */
public class Main {
    private static DataManager dataManager;
    private static WebServer webServer;

    public static void main(String[] args) {
        System.out.println("彩票系统启动中...");

        // 添加关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("正在关闭系统...");
            if (webServer != null) webServer.stop();
            if (dataManager != null) dataManager.shutdown();
            System.out.println("系统已关闭");
        }));

        try {
            // 1. 初始化数据管理器
            dataManager = DataManager.getInstance();
            if (!waitForDataManagerInitialized()) {
                System.err.println("数据管理器初始化失败，程序退出");
                System.exit(1);
            }

            // 2. 初始化业务服务
            UserService userService = new UserService(dataManager);
            TicketService ticketService = new TicketService(dataManager);
            LotteryService lotteryService = new LotteryService(dataManager);

            // 3. 创建Servlet处理器
            ServletHandler servletHandler = new ServletHandler();
            servletHandler.setUserService(userService);
            servletHandler.setTicketService(ticketService);
            servletHandler.setLotteryService(lotteryService);

            // 4. 启动Web服务器
            webServer = new WebServer();
            webServer.setServletHandler(servletHandler);
            webServer.start();

            System.out.println("Web服务器已启动，监听端口: " + webServer.getPort());
            System.out.println("按 Ctrl+C 停止服务器");

            // 5. 保持主线程运行
            while (true) {
                Thread.sleep(1000);
            }

        } catch (Exception e) {
            System.err.println("系统启动失败: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * 等待数据管理器初始化
     */
    private static boolean waitForDataManagerInitialized() {
        int maxWait = 5000; // 5秒超时
        int waited = 0;
        while (!dataManager.isInitialized() && waited < maxWait) {
            try {
                Thread.sleep(100);
                waited += 100;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return dataManager.isInitialized();
    }
}