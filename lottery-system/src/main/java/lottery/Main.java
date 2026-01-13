package lottery;

import lottery.dao.DataManager;
import lottery.service.LotteryService;
import lottery.ui.WebServer;
import lottery.util.FileUtils;
import lottery.model.User;
import lottery.model.LotteryResult;

import java.util.Scanner;

/**
 * 彩票系统主程序类
 * 程序入口点，提供命令行界面和多种运行模式
 */
public class Main {
    private static DataManager dataManager;
    private static WebServer webServer;
    private static boolean isRunning = true;

    /**
     * 程序主入口
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 显示欢迎信息
        printWelcomeMessage();

        // 加载配置
        loadConfiguration();

        // 初始化数据管理器
        initializeDataManager();

        // 解析命令行参数
        if (args.length > 0) {
            handleCommandLineArgs(args);
        } else {
            // 交互式命令行界面
            runInteractiveMode();
        }

        // 优雅关闭处理
        handleShutdownHook();
    }

    /**
     * 打印欢迎信息
     */
    private static void printWelcomeMessage() {
        System.out.println("=========================================");
        System.out.println("      彩票系统 v1.0");
        System.out.println("=========================================");
        System.out.println();
    }

    /**
     * 加载系统配置
     */
    private static void loadConfiguration() {
        System.out.println("[INFO] 正在加载系统配置...");

        try {
            // 使用FileUtils加载配置
            String serverPort = FileUtils.getConfigValue("server.port", "8080");
            String dataDir = FileUtils.getConfigValue("excel.data.dir", "data");

            System.out.println("[INFO] 配置加载完成:");
            System.out.println("  - 服务器端口: " + serverPort);
            System.out.println("  - 数据目录: " + dataDir);
            System.out.println();
        } catch (Exception e) {
            System.err.println("[ERROR] 加载配置失败: " + e.getMessage());
            System.out.println("[INFO] 使用默认配置");
        }
    }

    /**
     * 初始化数据管理器
     */
    private static void initializeDataManager() {
        System.out.println("[INFO] 正在初始化数据管理器...");

        try {
            dataManager = DataManager.getInstance();
            dataManager.loadAllData();

            System.out.println("[INFO] 数据加载完成:");
            System.out.println("  - 用户数量: " + dataManager.getAllUsers().size());
            System.out.println("  - 彩票数量: " + dataManager.getAllTickets().size());
            System.out.println("  - 开奖结果: " + dataManager.getAllResults().size());
            System.out.println();
        } catch (Exception e) {
            System.err.println("[ERROR] 初始化数据管理器失败: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * 初始化所有服务
     */
    private static void initializeServices() {
        System.out.println("[INFO] 正在初始化服务...");

        try {
            // Web服务器初始化
            webServer = new WebServer();

            // 从配置获取端口
            String portStr = FileUtils.getConfigValue("server.port", "8080");
            int port = Integer.parseInt(portStr);
            webServer.setPort(port);

            System.out.println("[INFO] 服务初始化完成");
            System.out.println();
        } catch (Exception e) {
            System.err.println("[ERROR] 初始化服务失败: " + e.getMessage());
        }
    }

    /**
     * 处理命令行参数
     * @param args 命令行参数
     */
    private static void handleCommandLineArgs(String[] args) {
        if (args.length == 0) {
            return;
        }

        String command = args[0].toLowerCase();

        switch (command) {
            case "-start":
            case "-s":
                startWebServer();
                break;

            case "-test":
            case "-t":
                runTestMode();
                break;

            case "-draw":
            case "-d":
                runDrawMode();
                break;

            case "-help":
            case "-h":
                printHelp();
                break;

            default:
                System.out.println("[ERROR] 未知命令: " + command);
                printHelp();
                break;
        }
    }

    /**
     * 运行交互式模式
     */
    private static void runInteractiveMode() {
        Scanner scanner = new Scanner(System.in);

        while (isRunning) {
            printMenu();

            System.out.print("请选择操作 (1-4): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    startWebServer();
                    break;

                case "2":
                    runTestMode();
                    break;

                case "3":
                    runDrawMode();
                    break;

                case "4":
                    exitProgram(scanner);
                    break;

                default:
                    System.out.println("[ERROR] 无效的选择，请重新输入");
                    break;
            }
        }

        scanner.close();
    }

    /**
     * 打印菜单
     */
    private static void printMenu() {
        System.out.println("请选择运行模式:");
        System.out.println("1. 正常启动 (启动Web界面)");
        System.out.println("2. 测试模式 (运行批量测试)");
        System.out.println("3. 抽奖模式 (直接执行抽奖)");
        System.out.println("4. 退出程序");
        System.out.println();
    }

    /**
     * 正常启动：启动Web界面
     */
    private static void startWebServer() {
        System.out.println("[INFO] 启动Web服务器...");

        try {
            initializeServices();

            // 启动Web服务器（在新线程中运行，避免阻塞）
            Thread serverThread = new Thread(() -> {
                try {
                    webServer.start();
                } catch (Exception e) {
                    System.err.println("[ERROR] Web服务器启动失败: " + e.getMessage());
                }
            });

            serverThread.setDaemon(true);
            serverThread.start();

            System.out.println("[INFO] Web服务器已启动");
            System.out.println("[INFO] 请在浏览器中访问: http://localhost:" +
                    FileUtils.getConfigValue("server.port", "8080"));
            System.out.println("[INFO] 按Ctrl+C停止服务器");
            System.out.println();

            // 等待服务器运行
            serverThread.join();

        } catch (Exception e) {
            System.err.println("[ERROR] 启动Web服务器失败: " + e.getMessage());
        }
    }

    /**
     * 测试模式：运行批量测试
     */
    private static void runTestMode() {
        System.out.println("[INFO] 进入测试模式...");

        try {
            System.out.println("请选择测试类型:");
            System.out.println("1. 自动注册测试");
            System.out.println("2. 抽奖模拟测试");
            System.out.println("3. 返回主菜单");
            System.out.println();

            Scanner scanner = new Scanner(System.in);
            System.out.print("请选择 (1-3): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    runAutoRegisterTest(scanner);
                    break;

                case "2":
                    runLotterySimulationTest();
                    break;

                case "3":
                    // 返回主菜单
                    scanner.close();
                    break;

                default:
                    System.out.println("[ERROR] 无效的选择");
                    scanner.close();
                    break;
            }

        } catch (Exception e) {
            System.err.println("[ERROR] 测试模式执行失败: " + e.getMessage());
        }
    }

    /**
     * 运行自动注册测试
     */
    private static void runAutoRegisterTest(Scanner scanner) {
        System.out.print("请输入要注册的用户数量 (默认100000): ");
        String input = scanner.nextLine().trim();

        int userCount = 100000; // 默认10万用户
        if (!input.isEmpty()) {
            try {
                userCount = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("[WARN] 输入格式错误，使用默认值: 100000");
            }
        }

        System.out.println("[INFO] 开始自动注册测试，注册用户数: " + userCount);

        try {
            // 使用反射调用测试类，避免编译依赖
            Class<?> testClass = Class.forName("lottery.test.AutoRegisterTest");
            java.lang.reflect.Method mainMethod = testClass.getMethod("main", String[].class);
            String[] args = { String.valueOf(userCount) };
            mainMethod.invoke(null, (Object) args);

            // 重新加载数据
            dataManager.loadAllData();

            System.out.println("[INFO] 自动注册测试完成");
            System.out.println("  - 当前用户数量: " + dataManager.getAllUsers().size());
            System.out.println("  - 当前彩票数量: " + dataManager.getAllTickets().size());
            System.out.println();

        } catch (Exception e) {
            System.err.println("[ERROR] 自动注册测试失败: " + e.getMessage());
        }
    }

    /**
     * 运行抽奖模拟测试
     */
    private static void runLotterySimulationTest() {
        System.out.println("[INFO] 开始抽奖模拟测试...");

        try {
            // 使用反射调用测试类，避免编译依赖
            Class<?> testClass = Class.forName("lottery.test.LotterySimulation");
            java.lang.reflect.Method mainMethod = testClass.getMethod("main", String[].class);
            mainMethod.invoke(null, (Object) new String[0]);

            // 重新加载数据
            dataManager.loadAllData();

            System.out.println("[INFO] 抽奖模拟测试完成");
            System.out.println();

        } catch (Exception e) {
            System.err.println("[ERROR] 抽奖模拟测试失败: " + e.getMessage());
        }
    }

    /**
     * 抽奖模式：直接执行抽奖
     */
    private static void runDrawMode() {
        System.out.println("[INFO] 进入抽奖模式...");

        try {
            LotteryService lotteryService = new LotteryService();

            System.out.println("[INFO] 正在执行抽奖...");
            String winningNumbers = lotteryService.drawLottery();

            System.out.println("[INFO] 抽奖完成!");
            System.out.println("  - 中奖号码: " + winningNumbers);

            // 获取最新结果
            LotteryResult latestResult = dataManager.getLatestResult();
            if (latestResult != null) {
                System.out.println("  - 中奖用户ID: " + latestResult.getWinnerUserId());
                System.out.println("  - 中奖等级: " + latestResult.getPrizeLevel());

                // 获取中奖用户信息
                User winner = dataManager.getUserById(latestResult.getWinnerUserId());
                if (winner != null) {
                    System.out.println("  - 中奖用户名: " + winner.getUsername());
                    System.out.println("  - 用户余额: ¥" + winner.getBalance());
                }
            } else {
                System.out.println("  - 本期无人中大奖");
            }

            System.out.println();

        } catch (Exception e) {
            System.err.println("[ERROR] 抽奖执行失败: " + e.getMessage());
        }
    }

    /**
     * 退出程序
     */
    private static void exitProgram(Scanner scanner) {
        System.out.println("[INFO] 正在退出程序...");

        try {
            // 关闭扫描器
            scanner.close();

            // 保存所有数据
            if (dataManager != null) {
                dataManager.saveAllData();
                System.out.println("[INFO] 数据已保存");
            }

            // 停止Web服务器
            if (webServer != null && webServer.isRunning()) {
                webServer.stop();
                System.out.println("[INFO] Web服务器已停止");
            }

            isRunning = false;
            System.out.println("[INFO] 程序已退出");
            System.out.println();

        } catch (Exception e) {
            System.err.println("[ERROR] 退出程序时发生错误: " + e.getMessage());
        }
    }

    /**
     * 处理关闭钩子
     */
    private static void handleShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println();
            System.out.println("[INFO] 检测到程序关闭信号，正在保存数据...");

            try {
                // 保存所有数据
                if (dataManager != null) {
                    dataManager.saveAllData();
                    System.out.println("[INFO] 数据已保存");
                }

                // 停止Web服务器
                if (webServer != null && webServer.isRunning()) {
                    webServer.stop();
                    System.out.println("[INFO] Web服务器已停止");
                }

                System.out.println("[INFO] 程序已安全退出");

            } catch (Exception e) {
                System.err.println("[ERROR] 关闭过程中发生错误: " + e.getMessage());
            }
        }));
    }

    /**
     * 打印帮助信息
     */
    private static void printHelp() {
        System.out.println("彩票系统使用说明:");
        System.out.println();
        System.out.println("命令行参数:");
        System.out.println("  -start, -s   启动Web服务器（默认模式）");
        System.out.println("  -test, -t    运行批量测试模式");
        System.out.println("  -draw, -d    直接执行抽奖");
        System.out.println("  -help, -h    显示此帮助信息");
        System.out.println();
        System.out.println("示例:");
        System.out.println("  java -jar lottery-system.jar -start");
        System.out.println("  java -jar lottery-system.jar -test");
        System.out.println("  java -jar lottery-system.jar -draw");
        System.out.println();

        System.out.println("交互模式:");
        System.out.println("  直接运行程序（不带参数）进入交互式命令行界面");
        System.out.println();
    }
}