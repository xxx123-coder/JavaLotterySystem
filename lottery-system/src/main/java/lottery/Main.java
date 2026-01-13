package lottery;

import lottery.dao.DataManager;
import lottery.service.LotteryService;
import lottery.service.UserService;
import lottery.service.TicketService;
import lottery.ui.WebServer;
import lottery.util.FileUtils;
import lottery.util.PathManager;
import lottery.model.User;
import lottery.model.LotteryResult;
import lottery.model.Ticket;

import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.io.File;

/**
 * 彩票系统主程序类
 * 程序入口点，提供命令行界面和多种运行模式
 */
public class Main {
    private static DataManager dataManager;
    private static WebServer webServer;
    private static boolean isRunning = true;
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * 程序主入口
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

        // 关闭扫描器
        scanner.close();
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
            // 首先打印路径信息，帮助调试
            System.out.println("[INFO] 当前工作目录: " + System.getProperty("user.dir"));

            // 使用PathManager获取路径信息
            PathManager.printPathInfo();

            // 从配置文件获取配置
            String serverPort = FileUtils.getConfigValue("server.port", "8080");
            String dataDir = FileUtils.getConfigValue("excel.data.dir", "data");

            // 尝试创建数据目录
            String projectRoot = PathManager.getProjectRoot();
            File dataDirFile = new File(projectRoot, dataDir);
            if (!dataDirFile.exists()) {
                System.out.println("[INFO] 创建数据目录: " + dataDirFile.getAbsolutePath());
                boolean created = dataDirFile.mkdirs();
                if (!created) {
                    System.out.println("[WARN] 创建数据目录失败，请检查权限");
                }
            }

            System.out.println("[INFO] 配置加载完成:");
            System.out.println("  - 服务器端口: " + serverPort);
            System.out.println("  - 数据目录: " + dataDir);
            System.out.println("  - 项目根目录: " + projectRoot);
            System.out.println("  - 数据目录路径: " + dataDirFile.getAbsolutePath());
            System.out.println();

        } catch (Exception e) {
            System.err.println("[ERROR] 加载配置失败: " + e.getMessage());
            System.out.println("[INFO] 使用默认配置");
            System.out.println("  - 服务器端口: 8080");
            System.out.println("  - 数据目录: data");
            System.out.println();
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
                    exitProgram();
                    break;

                default:
                    System.out.println("[ERROR] 无效的选择，请重新输入");
                    break;
            }
        }
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

            System.out.print("请选择 (1-3): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    runAutoRegisterTest();
                    break;

                case "2":
                    runLotterySimulationTest();
                    break;

                case "3":
                    return; // 返回主菜单

                default:
                    System.out.println("[ERROR] 无效的选择");
                    break;
            }

        } catch (Exception e) {
            System.err.println("[ERROR] 测试模式执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 运行自动注册测试 - 不依赖外部测试类
     */
    private static void runAutoRegisterTest() {
        System.out.print("请输入要注册的用户数量 (默认100): ");
        String input = scanner.nextLine().trim();

        int userCount = 100; // 默认100用户
        if (!input.isEmpty()) {
            try {
                userCount = Integer.parseInt(input);
                if (userCount <= 0) {
                    System.out.println("[WARN] 用户数必须大于0，使用默认值: 100");
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

        // 直接调用内部测试逻辑，不依赖外部测试类
        internalAutoRegisterTest(userCount);
    }

    /**
     * 内部自动注册测试逻辑
     */
    private static void internalAutoRegisterTest(int userCount) {
        System.out.println("[INFO] 开始创建测试用户...");

        UserService userService = new UserService(dataManager);
        TicketService ticketService = new TicketService(dataManager);
        Random random = new Random();
        int successCount = 0;
        int totalTickets = 0;

        long startTime = System.currentTimeMillis();

        for (int i = 1; i <= userCount; i++) {
            try {
                // 生成测试用户信息
                String username = "test_user_" + String.format("%06d", i);
                String password = "123456";
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

                        // 每个用户随机购买1-3张彩票
                        int ticketCount = 1 + random.nextInt(3);

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
                                    System.err.println("[ERROR] 购买彩票失败: " + e.getMessage());
                                }
                            } else {
                                // 随机选号
                                try {
                                    ticketService.buyRandomTicket(user.getId(), 1);
                                    totalTickets++;
                                } catch (Exception e) {
                                    System.err.println("[ERROR] 购买彩票失败: " + e.getMessage());
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
                    dataManager.saveAllData();
                }

            } catch (Exception e) {
                System.err.println("[ERROR] 创建用户失败 (序号 " + i + "): " + e.getMessage());
            }
        }

        // 最终保存
        dataManager.saveAllData();

        long endTime = System.currentTimeMillis();
        long timeSpent = endTime - startTime;

        // 重新加载数据获取准确统计
        dataManager.loadAllData();

        System.out.println("[INFO] 自动注册测试完成!");
        System.out.println("  - 成功注册用户: " + successCount + "/" + userCount);
        System.out.println("  - 购买彩票总数: " + totalTickets);
        System.out.println("  - 当前用户总数: " + dataManager.getAllUsers().size());
        System.out.println("  - 当前彩票总数: " + dataManager.getAllTickets().size());
        System.out.println("  - 测试耗时: " + timeSpent + " 毫秒");
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

        // 计算用户平均余额
        double totalBalance = 0;
        for (User user : users) {
            totalBalance += user.getBalance();
        }
        double avgBalance = totalBalance / users.size();

        // 统计选号方式
        int manualCount = 0;
        int randomCount = 0;
        for (Ticket ticket : tickets) {
            if (ticket.isManual()) {
                manualCount++;
            } else {
                randomCount++;
            }
        }

        System.out.println("===== 测试统计信息 =====");
        System.out.println("用户平均余额: ¥" + String.format("%.2f", avgBalance));
        System.out.println("手动选号票数: " + manualCount);
        System.out.println("随机选号票数: " + randomCount);
        System.out.println("总购票金额: ¥" + (tickets.size() * 2.0));
        System.out.println("=======================");
        System.out.println();
    }

    /**
     * 运行抽奖模拟测试 - 不依赖外部测试类
     */
    private static void runLotterySimulationTest() {
        System.out.println("[INFO] 进入抽奖模拟测试...");

        System.out.print("请输入模拟的抽奖次数 (默认3): ");
        String input = scanner.nextLine().trim();

        int drawCount = 3; // 默认抽奖3次
        if (!input.isEmpty()) {
            try {
                drawCount = Integer.parseInt(input);
                if (drawCount <= 0) {
                    System.out.println("[WARN] 抽奖次数必须大于0，使用默认值: 3");
                    drawCount = 3;
                }
            } catch (NumberFormatException e) {
                System.out.println("[WARN] 输入格式错误，使用默认值: 3");
            }
        }

        System.out.println("[INFO] 开始抽奖模拟测试，抽奖次数: " + drawCount);

        // 直接调用内部测试逻辑，不依赖外部测试类
        internalLotterySimulationTest(drawCount);
    }

    /**
     * 内部抽奖模拟测试逻辑
     */
    private static void internalLotterySimulationTest(int drawCount) {
        LotteryService lotteryService = new LotteryService(dataManager);

        for (int i = 1; i <= drawCount; i++) {
            System.out.println("\n[INFO] 第 " + i + " 次抽奖...");

            try {
                // 执行抽奖
                String winningNumbers = lotteryService.drawLottery();
                System.out.println("[INFO] 抽奖完成!");
                System.out.println("  - 中奖号码: " + winningNumbers);

                // 获取最新结果
                LotteryResult latestResult = dataManager.getLatestResult();

                if (latestResult != null) {
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

                // 等待一下，让用户看清楚
                Thread.sleep(1000);

            } catch (Exception e) {
                System.err.println("[ERROR] 抽奖过程发生错误: " + e.getMessage());
            }
        }

        System.out.println("\n[INFO] 抽奖模拟测试完成!");
        System.out.println("  - 抽奖次数: " + drawCount);
        System.out.println("  - 当前结果总数: " + dataManager.getAllResults().size());
        System.out.println();
    }

    /**
     * 抽奖模式：直接执行抽奖
     */
    private static void runDrawMode() {
        System.out.println("[INFO] 进入抽奖模式...");

        try {
            // 修复：传入dataManager参数
            LotteryService lotteryService = new LotteryService(dataManager);

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
    private static void exitProgram() {
        System.out.println("[INFO] 正在退出程序...");

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

        return prefix + suffix.toString();
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