package lottery;

import lottery.dao.DataManager;
import lottery.service.LotteryService;
import lottery.service.UserService;
import lottery.service.TicketService;
import lottery.ui.WebServer;
import lottery.util.FileUtils;
import lottery.util.PathManager;
import lottery.util.Logger;
import lottery.model.User;
import lottery.model.LotteryResult;
import lottery.model.Ticket;

import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 彩票系统主程序类
 * 优化初始化顺序，增强异常处理
 */
public class Main {
    private static DataManager dataManager;
    private static WebServer webServer;
    private static final AtomicBoolean isRunning = new AtomicBoolean(true);
    private static final Scanner scanner = new Scanner(System.in);

    // 服务实例
    private static UserService userService;
    private static TicketService ticketService;
    private static LotteryService lotteryService;

    // 初始化状态监控
    private static volatile boolean servicesInitialized = false;
    private static volatile boolean shutdownInProgress = false;

    // 日志记录器
    private static final Logger logger = Logger.getLogger(Main.class);

    /**
     * 程序主入口
     */
    public static void main(String[] args) {
        try {
            // 设置未捕获异常处理器
            Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
                logger.error("未捕获的异常在线程 " + thread.getName() + ": " + throwable.getMessage(), throwable);
                System.err.println("[FATAL] 发生未捕获异常: " + throwable.getMessage());
                gracefulShutdown(1);
            });

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

        } catch (Exception e) {
            logger.error("程序启动失败: " + e.getMessage(), e);
            System.err.println("[FATAL] 程序启动失败: " + e.getMessage());
            gracefulShutdown(1);

        } finally {
            // 确保资源释放
            cleanupResources();
            // scanner.close(); // 已经在 cleanupResources() 中关闭
        }
    }

    /**
     * 打印欢迎信息
     */
    private static void printWelcomeMessage() {
        System.out.println("=========================================");
        System.out.println("      彩票系统 v1.0");
        System.out.println("      增强版 - 支持数据备份与恢复");
        System.out.println("=========================================");
        System.out.println();
    }

    /**
     * 加载系统配置（增强版）
     */
    private static void loadConfiguration() {
        logger.info("正在加载系统配置...");

        try {
            // 打印环境信息
            logger.info("Java版本: " + System.getProperty("java.version"));
            logger.info("操作系统: " + System.getProperty("os.name"));
            logger.info("当前工作目录: " + System.getProperty("user.dir"));

            // 使用PathManager获取路径信息
            PathManager.printPathInfo();

            // 从配置文件获取配置
            String serverPort = FileUtils.getConfigValue("server.port", "8080");
            String dataDir = FileUtils.getConfigValue("excel.data.dir", "data");
            String backupEnabled = FileUtils.getConfigValue("backup.enabled", "true");
            String logLevel = FileUtils.getConfigValue("log.level", "INFO");

            // 配置日志
            Logger.setLevel(logLevel);

            // 验证端口号
            int port;
            try {
                port = Integer.parseInt(serverPort);
                if (port < 1 || port > 65535) {
                    logger.warn("端口号无效: " + port + "，使用默认端口8080");
                    port = 8080;
                }
            } catch (NumberFormatException e) {
                logger.warn("端口号格式错误: " + serverPort + "，使用默认端口8080");
                port = 8080;
            }

            // 尝试创建数据目录
            String dataDirPath = PathManager.normalizePath(dataDir);
            if (!PathManager.ensureDirectoryExists(dataDirPath)) {
                logger.warn("创建数据目录失败，使用备用目录");
                dataDirPath = PathManager.getDataDir();
            }

            logger.info("配置加载完成:");
            logger.info("  - 服务器端口: " + port);
            logger.info("  - 数据目录: " + dataDirPath);
            logger.info("  - 备份功能: " + backupEnabled);
            logger.info("  - 日志级别: " + logLevel);
            System.out.println();

        } catch (Exception e) {
            logger.error("加载配置失败: " + e.getMessage(), e);
            System.out.println("[INFO] 使用默认配置");
            System.out.println("  - 服务器端口: 8080");
            System.out.println("  - 数据目录: data");
            System.out.println("  - 备份功能: 启用");
            System.out.println();
        }
    }

    /**
     * 初始化数据管理器（增强版）
     */
    private static void initializeDataManager() {
        logger.info("正在初始化数据管理器...");

        int maxRetries = 3;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                dataManager = DataManager.getInstance();

                // 验证数据管理器状态
                if (!dataManager.isInitialized()) {
                    throw new IllegalStateException("数据管理器初始化失败");
                }

                logger.info("数据加载完成:");
                logger.info("  - 用户数量: " + dataManager.getUserCount());
                logger.info("  - 彩票数量: " + dataManager.getTicketCount());
                logger.info("  - 开奖结果: " + dataManager.getResultCount());
                System.out.println();

                return; // 成功，退出循环

            } catch (Exception e) {
                retryCount++;
                logger.warn("初始化数据管理器失败 (尝试 " + retryCount + "/" + maxRetries + "): " + e.getMessage());

                if (retryCount >= maxRetries) {
                    logger.error("数据管理器初始化彻底失败，程序退出");
                    System.err.println("[ERROR] 初始化数据管理器失败: " + e.getMessage());
                    gracefulShutdown(1);
                    return;
                }

                // 等待后重试 - 使用更优雅的等待方式
                try {
                    // 使用指数退避策略避免忙等待
                    long sleepTime = 2000L * retryCount;
                    logger.debug("等待 " + sleepTime + " 毫秒后重试");
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    logger.error("初始化过程被中断");
                    gracefulShutdown(1);
                    return;
                }
            }
        }
    }

    /**
     * 初始化所有服务（优化顺序）
     */
    private static synchronized void initializeServices() {
        if (servicesInitialized) {
            logger.info("服务已初始化，跳过重复初始化");
            return;
        }

        logger.info("正在初始化服务...");

        try {
            // 1. 先初始化业务服务（依赖于DataManager）
            userService = new UserService(dataManager);
            ticketService = new TicketService(dataManager);
            lotteryService = new LotteryService(dataManager);

            logger.info("业务服务初始化完成");

            // 2. 初始化Web服务器
            webServer = new WebServer();

            // 从配置获取端口
            String portStr = FileUtils.getConfigValue("server.port", "8080");
            int port;
            try {
                port = Integer.parseInt(portStr);
                if (port < 1 || port > 65535) {
                    port = 8080;
                }
            } catch (NumberFormatException e) {
                port = 8080;
            }

            webServer.setPort(port);
            logger.info("Web服务器配置完成，端口: " + port);

            servicesInitialized = true;
            logger.info("所有服务初始化完成");
            System.out.println();

        } catch (Exception e) {
            logger.error("初始化服务失败: " + e.getMessage(), e);
            throw new RuntimeException("服务初始化失败", e);
        }
    }

    /**
     * 处理命令行参数
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

            case "-backup":
            case "-b":
                runBackupMode();
                break;

            case "-recover":
            case "-r":
                runRecoveryMode();
                break;

            case "-help":
            case "-h":
                printHelp();
                break;

            case "-version":
            case "-v":
                printVersion();
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
        while (isRunning.get() && !shutdownInProgress) {
            printMenu();
            System.out.print("请选择操作 (1-7): ");

            try {
                String choice = scanner.nextLine().trim();

                if (choice.isEmpty()) {
                    continue;
                }

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
                        runBackupMode();
                        break;

                    case "5":
                        runRecoveryMode();
                        break;

                    case "6":
                        showSystemStatus();
                        break;

                    case "7":
                        exitProgram();
                        break;

                    default:
                        System.out.println("[ERROR] 无效的选择，请重新输入");
                        break;
                }

            } catch (Exception e) {
                logger.error("交互模式发生错误: " + e.getMessage(), e);
                System.out.println("[ERROR] 操作发生错误: " + e.getMessage());
            }
        }
    }

    /**
     * 打印菜单（增强版）
     */
    private static void printMenu() {
        System.out.println("\n请选择运行模式:");
        System.out.println("1. 正常启动 (启动Web界面)");
        System.out.println("2. 测试模式 (运行批量测试)");
        System.out.println("3. 抽奖模式 (直接执行抽奖)");
        System.out.println("4. 备份模式 (手动备份数据)");
        System.out.println("5. 恢复模式 (从备份恢复)");
        System.out.println("6. 系统状态 (查看运行状态)");
        System.out.println("7. 退出程序");
        System.out.println();
    }

    /**
     * 正常启动：启动Web界面（增强版）
     */
    private static void startWebServer() {
        logger.info("启动Web服务器...");

        try {
            // 确保服务已初始化
            if (!servicesInitialized) {
                initializeServices();
            }

            // 验证Web服务器状态
            if (webServer == null) {
                throw new IllegalStateException("Web服务器未初始化");
            }

            // 启动Web服务器线程
            Thread serverThread = createServerThread();
            serverThread.start();

            System.out.println("[INFO] Web服务器已启动");
            System.out.println("[INFO] 请在浏览器中访问: http://localhost:" +
                    FileUtils.getConfigValue("server.port", "8080"));
            System.out.println("[INFO] 按Ctrl+C停止服务器");
            System.out.println();

            // 等待服务器运行
            try {
                serverThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("服务器线程被中断");
            }

        } catch (Exception e) {
            logger.error("启动Web服务器失败: " + e.getMessage(), e);
            System.err.println("[ERROR] 启动Web服务器失败: " + e.getMessage());
        }
    }

    /**
     * 创建Web服务器线程
     */
    private static Thread createServerThread() {
        return new Thread(() -> {
            Thread.currentThread().setName("WebServer-Thread");

            try {
                logger.info("Web服务器线程启动");
                webServer.start();

            } catch (Exception e) {
                logger.error("Web服务器运行失败: " + e.getMessage(), e);
                System.err.println("[ERROR] Web服务器运行失败: " + e.getMessage());

                // 尝试重启
                if (!shutdownInProgress) {
                    logger.info("尝试重启Web服务器...");
                    tryRestartWebServer();
                }
            }
        });
    }

    /**
     * 尝试重启Web服务器
     */
    private static void tryRestartWebServer() {
        try {
            Thread.sleep(5000); // 等待5秒后重启

            if (!shutdownInProgress && isRunning.get()) {
                logger.info("正在重启Web服务器...");
                startWebServer();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
            System.out.println("3. 压力测试");
            System.out.println("4. 返回主菜单");
            System.out.println();

            System.out.print("请选择 (1-4): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    runAutoRegisterTest();
                    break;

                case "2":
                    runLotterySimulationTest();
                    break;

                case "3":
                    runStressTest();
                    break;

                case "4":
                    return; // 返回主菜单

                default:
                    System.out.println("[ERROR] 无效的选择");
                    break;
            }

        } catch (Exception e) {
            logger.error("测试模式执行失败: " + e.getMessage(), e);
            System.err.println("[ERROR] 测试模式执行失败: " + e.getMessage());
        }
    }

    /**
     * 运行自动注册测试
     */
    private static void runAutoRegisterTest() {
        System.out.print("请输入要注册的用户数量 (默认100): ");
        String input = scanner.nextLine().trim();

        int userCount = 100;
        if (!input.isEmpty()) {
            try {
                userCount = Integer.parseInt(input);
                if (userCount <= 0 || userCount > 10000) {
                    System.out.println("[WARN] 用户数必须为1-10000，使用默认值: 100");
                    userCount = 100;
                }
            } catch (NumberFormatException e) {
                System.out.println("[WARN] 输入格式错误，使用默认值: 100");
            }
        }

        System.out.println("[INFO] 开始自动注册测试，注册用户数: " + userCount);
        System.out.println("[INFO] 注意：此测试将在当前数据库中创建测试用户");
        System.out.print("[INFO] 是否继续？(y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (!confirm.equals("y") && !confirm.equals("yes")) {
            System.out.println("[INFO] 测试已取消");
            return;
        }

        internalAutoRegisterTest(userCount);
    }

    /**
     * 内部自动注册测试逻辑
     */
    private static void internalAutoRegisterTest(int userCount) {
        logger.info("开始创建测试用户...");

        if (userService == null) {
            userService = new UserService(dataManager);
        }
        if (ticketService == null) {
            ticketService = new TicketService(dataManager);
        }

        Random random = new Random();
        int successCount = 0;
        int totalTickets = 0;

        long startTime = System.currentTimeMillis();

        for (int i = 1; i <= userCount; i++) {
            try {
                // 生成测试用户信息
                String username = "test_user_" + String.format("%06d", i);
                String password = "password_" + i;
                String phone = generateRandomPhone(i);

                // 注册用户
                boolean registered = userService.register(username, password, phone);

                if (registered) {
                    // 登录用户获取ID
                    User user = userService.login(username, password);

                    if (user != null) {
                        // 随机充值10-100元
                        double amount = 10 + random.nextInt(91);
                        userService.recharge(user.getId(), amount);

                        // 每个用户随机购买1-5张彩票
                        int ticketCount = 1 + random.nextInt(5);

                        for (int j = 0; j < ticketCount; j++) {
                            // 随机选择选号方式
                            boolean manual = random.nextBoolean();

                            if (manual) {
                                // 手动选号
                                String numbers = generateRandomNumbers();
                                try {
                                    ticketService.buyManualTicket(user.getId(), numbers, 1);
                                    totalTickets++;
                                } catch (Exception e) {
                                    logger.warn("购买手动选号彩票失败: " + e.getMessage());
                                }
                            } else {
                                // 随机选号
                                try {
                                    ticketService.buyRandomTicket(user.getId(), 1);
                                    totalTickets++;
                                } catch (Exception e) {
                                    logger.warn("购买随机选号彩票失败: " + e.getMessage());
                                }
                            }
                        }

                        successCount++;
                    }
                }

                // 显示进度
                if (i % 100 == 0 || i == userCount) {
                    double progress = (double) i / userCount * 100;
                    System.out.printf("[INFO] 进度: %.1f%% (%d/%d)%n", progress, i, userCount);
                }

                // 每100个用户保存一次数据
                if (i % 100 == 0) {
                    dataManager.forceSave();
                }

            } catch (Exception e) {
                logger.error("创建用户失败 (序号 " + i + "): " + e.getMessage());
                System.err.println("[ERROR] 创建用户失败 (序号 " + i + "): " + e.getMessage());
            }
        }

        // 最终保存
        dataManager.forceSave();

        long endTime = System.currentTimeMillis();
        long timeSpent = endTime - startTime;

        // 重新加载数据获取准确统计
        dataManager.loadAllData();

        System.out.println("\n[INFO] 自动注册测试完成!");
        System.out.println("  - 成功注册用户: " + successCount + "/" + userCount);
        System.out.println("  - 购买彩票总数: " + totalTickets);
        System.out.println("  - 当前用户总数: " + dataManager.getUserCount());
        System.out.println("  - 当前彩票总数: " + dataManager.getTicketCount());
        System.out.println("  - 测试耗时: " + timeSpent + " 毫秒");
        System.out.println("  - 平均耗时: " + String.format("%.2f", (double)timeSpent/userCount) + " 毫秒/用户");
        System.out.println();

        // 显示统计信息
        printTestStatistics();
    }

    /**
     * 打印测试统计信息
     */
    private static void printTestStatistics() {
        List<User> users = dataManager.getAllUsers();
        List<Ticket> tickets = dataManager.getAllTickets();

        if (users.isEmpty()) {
            System.out.println("[INFO] 暂无用户数据");
            return;
        }

        // 计算统计信息
        double totalBalance = 0;
        double maxBalance = 0;
        double minBalance = Double.MAX_VALUE;

        int manualCount = 0;
        int randomCount = 0;

        for (User user : users) {
            double balance = user.getBalance();
            totalBalance += balance;
            maxBalance = Math.max(maxBalance, balance);
            minBalance = Math.min(minBalance, balance);
        }

        for (Ticket ticket : tickets) {
            if (ticket.isManual()) {
                manualCount++;
            } else {
                randomCount++;
            }
        }

        double avgBalance = totalBalance / users.size();

        System.out.println("===== 测试统计信息 =====");
        System.out.println("用户总数: " + users.size());
        System.out.println("彩票总数: " + tickets.size());
        System.out.println("用户平均余额: ¥" + String.format("%.2f", avgBalance));
        System.out.println("用户最大余额: ¥" + String.format("%.2f", maxBalance));
        System.out.println("用户最小余额: ¥" + String.format("%.2f", minBalance));
        System.out.println("手动选号票数: " + manualCount + " (" +
                String.format("%.1f", (double)manualCount/tickets.size()*100) + "%)");
        System.out.println("随机选号票数: " + randomCount + " (" +
                String.format("%.1f", (double)randomCount/tickets.size()*100) + "%)");
        System.out.println("总购票金额: ¥" + String.format("%.2f", tickets.size() * 2.0));
        System.out.println("=======================");
        System.out.println();
    }

    /**
     * 运行压力测试
     */
    private static void runStressTest() {
        System.out.print("请输入并发用户数 (默认10): ");
        String input = scanner.nextLine().trim();

        int concurrentUsers = 10;
        if (!input.isEmpty()) {
            try {
                concurrentUsers = Integer.parseInt(input);
                if (concurrentUsers <= 0 || concurrentUsers > 100) {
                    System.out.println("[WARN] 并发用户数必须为1-100，使用默认值: 10");
                    concurrentUsers = 10;
                }
            } catch (NumberFormatException e) {
                System.out.println("[WARN] 输入格式错误，使用默认值: 10");
            }
        }

        System.out.println("[INFO] 开始压力测试，并发用户数: " + concurrentUsers);
        logger.info("开始压力测试，并发用户数: " + concurrentUsers);

        // 简化实现，实际应使用线程池
        long startTime = System.currentTimeMillis();

        List<Thread> threads = createStressTestThreads(concurrentUsers);

        // 等待所有线程完成
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("等待线程时被中断");
                break;
            }
        }

        long endTime = System.currentTimeMillis();
        long timeSpent = endTime - startTime;

        System.out.println("\n[INFO] 压力测试完成!");
        System.out.println("  - 并发用户数: " + concurrentUsers);
        System.out.println("  - 总耗时: " + timeSpent + " 毫秒");
        System.out.println("  - 平均响应时间: " + String.format("%.2f", (double)timeSpent/concurrentUsers) + " 毫秒/用户");
        System.out.println("  - 当前内存使用: " +
                String.format("%.2f", (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024.0 / 1024.0) + " MB");
        System.out.println();
    }

    /**
     * 创建压力测试线程
     */
    private static List<Thread> createStressTestThreads(int concurrentUsers) {
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < concurrentUsers; i++) {
            final int userId = i + 1;
            Thread thread = createStressTestThread(userId);
            threads.add(thread);
            thread.start();
        }
        return threads;
    }

    /**
     * 创建单个压力测试线程
     */
    private static Thread createStressTestThread(final int userId) {
        return new Thread(() -> {
            try {
                // 模拟用户操作
                Random random = new Random();
                for (int j = 0; j < 10; j++) {
                    // 随机购买彩票
                    if (ticketService != null && userService != null) {
                        // 这里简化实现
                        Thread.sleep(random.nextInt(100));
                    }
                }
            } catch (Exception e) {
                logger.warn("压力测试线程异常: " + e.getMessage());
            }
        }, "StressTest-Thread-" + userId);
    }

    /**
     * 运行抽奖模拟测试
     */
    private static void runLotterySimulationTest() {
        System.out.println("[INFO] 进入抽奖模拟测试...");

        System.out.print("请输入模拟的抽奖次数 (默认3): ");
        String input = scanner.nextLine().trim();

        int drawCount = 3;
        if (!input.isEmpty()) {
            try {
                drawCount = Integer.parseInt(input);
                if (drawCount <= 0 || drawCount > 100) {
                    System.out.println("[WARN] 抽奖次数必须为1-100，使用默认值: 3");
                    drawCount = 3;
                }
            } catch (NumberFormatException e) {
                System.out.println("[WARN] 输入格式错误，使用默认值: 3");
            }
        }

        System.out.println("[INFO] 开始抽奖模拟测试，抽奖次数: " + drawCount);
        internalLotterySimulationTest(drawCount);
    }

    /**
     * 内部抽奖模拟测试逻辑
     */
    private static void internalLotterySimulationTest(int drawCount) {
        if (lotteryService == null) {
            lotteryService = new LotteryService(dataManager);
        }

        for (int i = 1; i <= drawCount; i++) {
            System.out.println("\n[INFO] 第 " + i + " 次抽奖...");

            try {
                // 执行抽奖
                String winningNumbers = lotteryService.drawLottery();
                System.out.println("[INFO] 抽奖完成!");
                System.out.println("  - 中奖号码: " + winningNumbers);

                // 获取最新结果
                LotteryResult latestResult = dataManager.getLatestResult();

                if (latestResult != null && latestResult.getWinnerUserId() > 0) {
                    System.out.println("  - 中奖等级: " + latestResult.getPrizeLevel());
                    System.out.println("  - 中奖用户ID: " + latestResult.getWinnerUserId());
                    System.out.println("  - 抽奖时间: " + latestResult.getDrawTime());

                    // 获取中奖用户信息
                    User winner = dataManager.getUserById(latestResult.getWinnerUserId());
                    if (winner != null) {
                        System.out.println("  - 中奖用户: " + winner.getUsername());
                        System.out.println("  - 用户余额: ¥" + winner.getBalance());
                    }
                } else {
                    System.out.println("  - 本期无人中大奖");
                }

                // 等待一下
                Thread.sleep(1000);

            } catch (Exception e) {
                logger.error("抽奖过程发生错误: " + e.getMessage(), e);
                System.err.println("[ERROR] 抽奖过程发生错误: " + e.getMessage());
            }
        }

        System.out.println("\n[INFO] 抽奖模拟测试完成!");
        System.out.println("  - 抽奖次数: " + drawCount);
        System.out.println("  - 当前结果总数: " + dataManager.getResultCount());
        System.out.println();
    }

    /**
     * 抽奖模式：直接执行抽奖
     */
    private static void runDrawMode() {
        System.out.println("[INFO] 进入抽奖模式...");

        try {
            if (lotteryService == null) {
                lotteryService = new LotteryService(dataManager);
            }

            System.out.println("[INFO] 正在执行抽奖...");
            String winningNumbers = lotteryService.drawLottery();

            System.out.println("[INFO] 抽奖完成!");
            System.out.println("  - 中奖号码: " + winningNumbers);

            // 获取最新结果
            LotteryResult latestResult = dataManager.getLatestResult();
            if (latestResult != null && latestResult.getWinnerUserId() > 0) {
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
            logger.error("抽奖执行失败: " + e.getMessage(), e);
            System.err.println("[ERROR] 抽奖执行失败: " + e.getMessage());
        }
    }

    /**
     * 备份模式：手动备份数据
     */
    private static void runBackupMode() {
        System.out.println("[INFO] 进入备份模式...");

        try {
            System.out.print("[INFO] 确认执行手动备份？(y/n): ");
            String confirm = scanner.nextLine().trim().toLowerCase();

            if (confirm.equals("y") || confirm.equals("yes")) {
                System.out.println("[INFO] 开始执行手动备份...");
                dataManager.manualBackup();
                System.out.println("[INFO] 手动备份完成");
            } else {
                System.out.println("[INFO] 备份已取消");
            }

        } catch (Exception e) {
            logger.error("备份操作失败: " + e.getMessage(), e);
            System.err.println("[ERROR] 备份操作失败: " + e.getMessage());
        }
    }

    /**
     * 恢复模式：从备份恢复数据
     */
    private static void runRecoveryMode() {
        System.out.println("[INFO] 进入恢复模式...");
        System.out.println("[WARN] 警告：恢复操作将覆盖当前数据！");
        System.out.print("[INFO] 确认从备份恢复数据？(y/n): ");

        String confirm = scanner.nextLine().trim().toLowerCase();
        if (!confirm.equals("y") && !confirm.equals("yes")) {
            System.out.println("[INFO] 恢复操作已取消");
            return;
        }

        try {
            System.out.println("[INFO] 开始从备份恢复数据...");
            dataManager.recoverFromBackup();
            System.out.println("[INFO] 数据恢复完成");

        } catch (Exception e) {
            logger.error("数据恢复失败: " + e.getMessage(), e);
            System.err.println("[ERROR] 数据恢复失败: " + e.getMessage());
        }
    }

    /**
     * 显示系统状态
     */
    private static void showSystemStatus() {
        System.out.println("\n===== 系统状态信息 =====");
        System.out.println("运行状态: " + (isRunning.get() ? "运行中" : "已停止"));
        System.out.println("服务初始化: " + (servicesInitialized ? "已完成" : "未完成"));
        System.out.println("Web服务器: " + (webServer != null && webServer.isRunning() ? "运行中" : "已停止"));
        System.out.println("\n数据统计:");
        System.out.println("  - 用户数量: " + dataManager.getUserCount());
        System.out.println("  - 彩票数量: " + dataManager.getTicketCount());
        System.out.println("  - 开奖结果: " + dataManager.getResultCount());
        System.out.println("\n系统信息:");
        System.out.println("  - Java版本: " + System.getProperty("java.version"));
        System.out.println("  - 操作系统: " + System.getProperty("os.name"));
        System.out.println("  - 总内存: " +
                String.format("%.2f", Runtime.getRuntime().totalMemory() / 1024.0 / 1024.0) + " MB");
        System.out.println("  - 可用内存: " +
                String.format("%.2f", Runtime.getRuntime().freeMemory() / 1024.0 / 1024.0) + " MB");
        System.out.println("  - 已用内存: " +
                String.format("%.2f", (Runtime.getRuntime().totalMemory() -
                        Runtime.getRuntime().freeMemory()) / 1024.0 / 1024.0) + " MB");
        System.out.println("======================\n");
    }

    /**
     * 退出程序（增强版）
     */
    private static void exitProgram() {
        gracefulShutdown(0);
    }

    /**
     * 优雅关闭程序
     */
    private static void gracefulShutdown(int exitCode) {
        if (shutdownInProgress) {
            return;
        }

        shutdownInProgress = true;
        isRunning.set(false);

        System.out.println("\n[INFO] 正在优雅关闭程序...");
        logger.info("开始优雅关闭程序");

        try {
            // 1. 停止Web服务器
            if (webServer != null && webServer.isRunning()) {
                System.out.println("[INFO] 停止Web服务器...");
                webServer.stop();
                System.out.println("[INFO] Web服务器已停止");
            }

            // 2. 关闭数据管理器
            if (dataManager != null) {
                System.out.println("[INFO] 保存数据并关闭数据管理器...");
                dataManager.shutdown();
                System.out.println("[INFO] 数据管理器已关闭");
            }

            System.out.println("[INFO] 程序已安全退出");
            logger.info("程序已安全退出");

        } catch (Exception e) {
            logger.error("关闭程序时发生错误: " + e.getMessage(), e);
            System.err.println("[ERROR] 关闭程序时发生错误: " + e.getMessage());
        } finally {
            cleanupResources();
            System.exit(exitCode);
        }
    }

    /**
     * 处理关闭钩子
     */
    private static void handleShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!shutdownInProgress) {
                System.out.println();
                System.out.println("[INFO] 检测到程序关闭信号，正在保存数据...");
                logger.info("检测到程序关闭信号");

                try {
                    // 保存数据
                    if (dataManager != null) {
                        dataManager.forceSave();
                        System.out.println("[INFO] 数据已保存");
                    }

                    System.out.println("[INFO] 程序已安全退出");

                } catch (Exception e) {
                    System.err.println("[ERROR] 关闭过程中发生错误: " + e.getMessage());
                    logger.error("关闭过程中发生错误: " + e.getMessage(), e);
                }
            }
        }));
    }

    /**
     * 清理资源
     */
    private static void cleanupResources() {
        try {
            if (scanner != null) {
                scanner.close();
            }
        } catch (Exception e) {
            // 忽略关闭异常
            logger.warn("关闭Scanner时发生异常: " + e.getMessage());
        }
    }

    /**
     * 打印版本信息
     */
    private static void printVersion() {
        System.out.println("彩票系统 v1.0");
        System.out.println("增强版 - 支持数据备份、恢复和自动保存");
        System.out.println("构建时间: 2024年");
        System.out.println("Java版本要求: 1.8+");
        System.out.println();
    }

    /**
     * 打印帮助信息（增强版）
     */
    private static void printHelp() {
        System.out.println("彩票系统使用说明:");
        System.out.println();
        System.out.println("命令行参数:");
        System.out.println("  -start, -s   启动Web服务器（默认模式）");
        System.out.println("  -test, -t    运行批量测试模式");
        System.out.println("  -draw, -d    直接执行抽奖");
        System.out.println("  -backup, -b  手动备份数据");
        System.out.println("  -recover, -r 从备份恢复数据");
        System.out.println("  -version, -v 显示版本信息");
        System.out.println("  -help, -h    显示此帮助信息");
        System.out.println();
        System.out.println("示例:");
        System.out.println("  java -jar lottery-system.jar -start");
        System.out.println("  java -jar lottery-system.jar -test");
        System.out.println("  java -jar lottery-system.jar -draw");
        System.out.println("  java -jar lottery-system.jar -backup");
        System.out.println();
        System.out.println("交互模式:");
        System.out.println("  直接运行程序（不带参数）进入交互式命令行界面");
        System.out.println();
    }

    /**
     * 生成随机电话号码
     */
    private static String generateRandomPhone(int seed) {
        Random random = new Random(seed);
        String[] prefixes = {"130", "131", "132", "133", "134", "135", "136",
                "137", "138", "139", "150", "151", "152", "153",
                "155", "156", "157", "158", "159", "180", "181",
                "182", "183", "184", "185", "186", "187", "188", "189"};

        String prefix = prefixes[random.nextInt(prefixes.length)];

        // 生成后8位
        StringBuilder suffix = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            suffix.append(random.nextInt(10));
        }

        return prefix + suffix;
    }

    /**
     * 生成随机彩票号码
     */
    private static String generateRandomNumbers() {
        Random random = new Random();
        List<Integer> numbers = new ArrayList<>();

        while (numbers.size() < 7) {
            int num = 1 + random.nextInt(36);
            if (!numbers.contains(num)) {
                numbers.add(num);
            }
        }

        // 排序
        numbers.sort(Integer::compareTo);

        // 转换为字符串
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numbers.size(); i++) {
            sb.append(numbers.get(i));
            if (i < numbers.size() - 1) {
                sb.append(",");
            }
        }

        return sb.toString();
    }
}