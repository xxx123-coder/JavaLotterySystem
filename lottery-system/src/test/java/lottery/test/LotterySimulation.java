package lottery.test;

import lottery.service.LotteryService;
import lottery.service.UserService;
import lottery.service.TicketService;
import lottery.dao.DataManager;
import lottery.model.LotteryResult;
import lottery.model.Ticket;
import lottery.model.User;
import lottery.util.NumberUtils;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 抽奖过程模拟测试类
 * 用于测试抽奖逻辑、中奖验证和性能测试
 */
public class LotterySimulation {
    private final LotteryService lotteryService;
    private final UserService userService;
    private final TicketService ticketService;
    private final DataManager dataManager;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String LOG_FILE = "lottery_test_log_" + dateFormat.format(new Date()).replaceAll("[: ]", "_") + ".txt";

    public LotterySimulation() {
        this.dataManager = DataManager.getInstance();
        this.lotteryService = new LotteryService(dataManager);
        this.userService = new UserService(dataManager);
        this.ticketService = new TicketService(dataManager);

        // 加载数据
        dataManager.loadAllData();

        // 初始化日志
        log("========== 抽奖模拟测试开始 ==========");
        log("测试时间: " + dateFormat.format(new Date()));
    }

    /**
     * 执行完整抽奖模拟
     */
    public void simulateDraw() {
        log("开始抽奖模拟...");

        long startTime = System.currentTimeMillis();

        try {
            // 执行抽奖
            String winningNumbers = lotteryService.drawLottery();

            // 获取中奖结果
            List<LotteryResult> allResults = lotteryService.getUserWinningResults(0);
            LotteryResult latestResult = allResults.isEmpty() ? null : allResults.get(allResults.size() - 1);

            // 记录结果
            if (latestResult != null) {
                log("抽奖完成!");
                log("中奖号码: " + winningNumbers);
                log("中奖等级: " + latestResult.getPrizeLevel());
                log("中奖用户ID: " + latestResult.getWinnerUserId());
                log("开奖时间: " + dateFormat.format(latestResult.getDrawTime()));

                // 获取中奖用户信息
                User winner = userService.getUserInfo(latestResult.getWinnerUserId());
                if (winner != null) {
                    log("中奖用户: " + winner.getUsername());
                    log("中奖后余额: ¥" + String.format("%.2f", winner.getBalance()));
                }
            } else {
                log("抽奖完成，但本期无人中大奖");
                log("中奖号码: " + winningNumbers);
            }

        } catch (Exception e) {
            log("抽奖过程发生错误: " + e.getMessage());
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        double timeSpent = (endTime - startTime) / 1000.0;

        log(String.format("抽奖模拟完成，耗时: %.2f 秒", timeSpent));
    }

    /**
     * 验证中奖判断逻辑
     */
    public void verifyWinningLogic() {
        log("开始验证中奖判断逻辑...");

        int testCount = 0;
        int passCount = 0;

        // 测试用例1: 全中（7个号码全匹配）
        testCount++;
        if (testFullMatch()) {
            passCount++;
            log("测试用例1 (全中): 通过");
        } else {
            log("测试用例1 (全中): 失败");
        }

        // 测试用例2: 6个号码匹配（一等奖）
        testCount++;
        if (testSixMatch()) {
            passCount++;
            log("测试用例2 (一等奖): 通过");
        } else {
            log("测试用例2 (一等奖): 失败");
        }

        // 测试用例3: 5个号码匹配（二等奖）
        testCount++;
        if (testFiveMatch()) {
            passCount++;
            log("测试用例3 (二等奖): 通过");
        } else {
            log("测试用例3 (二等奖): 失败");
        }

        // 测试用例4: 4个号码匹配（三等奖）
        testCount++;
        if (testFourMatch()) {
            passCount++;
            log("测试用例4 (三等奖): 通过");
        } else {
            log("测试用例4 (三等奖): 失败");
        }

        // 测试用例5: 3个号码匹配（未中奖）
        testCount++;
        if (testThreeMatch()) {
            passCount++;
            log("测试用例5 (未中奖): 通过");
        } else {
            log("测试用例5 (未中奖): 失败");
        }

        // 测试用例6: 无号码匹配（未中奖）
        testCount++;
        if (testNoMatch()) {
            passCount++;
            log("测试用例6 (未中奖): 通过");
        } else {
            log("测试用例6 (未中奖): 失败");
        }

        log(String.format("中奖逻辑验证完成: %d/%d 个测试用例通过", passCount, testCount));
    }

    /**
     * 测试程序性能
     * @param userCount 模拟的用户数量
     */
    public void testPerformance(int userCount) {
        log("开始性能测试，模拟用户数: " + userCount);
        log("--------------------------------------");

        // 1. 测试数据加载性能
        testDataLoading();

        // 2. 测试用户查询性能
        testUserQueryPerformance();

        // 3. 测试彩票查询性能
        testTicketQueryPerformance();

        // 4. 测试抽奖计算性能
        testDrawPerformance(userCount);

        log("--------------------------------------");
        log("性能测试完成");
    }

    /**
     * 执行抽奖测试的主方法
     */
    public static void main(String[] args) {
        try {
            LotterySimulation simulation = new LotterySimulation();

            log("当前系统状态:");
            log("用户数量: " + simulation.dataManager.getAllUsers().size());
            log("彩票数量: " + simulation.dataManager.getAllTickets().size());
            log("--------------------------------------");

            // 解析命令行参数
            boolean runAllTests = true;
            int performanceTestUsers = 10000; // 默认1万用户性能测试

            if (args.length > 0) {
                String command = args[0].toLowerCase();

                if ("verify".equals(command)) {
                    // 只验证中奖逻辑
                    simulation.verifyWinningLogic();
                    runAllTests = false;
                } else if ("performance".equals(command) && args.length > 1) {
                    // 只运行性能测试
                    try {
                        performanceTestUsers = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        log("参数格式错误，使用默认值: 10000");
                    }
                    simulation.testPerformance(performanceTestUsers);
                    runAllTests = false;
                }
            }

            if (runAllTests) {
                // 1. 验证中奖逻辑
                simulation.verifyWinningLogic();
                log("--------------------------------------");

                // 2. 性能测试
                simulation.testPerformance(performanceTestUsers);
                log("--------------------------------------");

                // 3. 执行实际抽奖模拟
                simulation.simulateDraw();
            }

            log("========== 抽奖模拟测试完成 ==========");

        } catch (Exception e) {
            log("测试过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== 私有辅助方法 ==========

    /**
     * 测试全中情况（特等奖）
     */
    private boolean testFullMatch() {
        try {
            // 创建测试彩票和中奖号码
            String winningNumbers = "1,2,3,4,5,6,7";
            String ticketNumbers = "1,2,3,4,5,6,7";

            // 创建测试彩票
            Ticket testTicket = new Ticket();
            testTicket.setNumbers(ticketNumbers);

            // 判断中奖
            String prizeLevel = lotteryService.checkWinning(testTicket, winningNumbers);

            return "特等奖".equals(prizeLevel);
        } catch (Exception e) {
            log("全中测试异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 测试6个号码匹配（一等奖）
     */
    private boolean testSixMatch() {
        try {
            String winningNumbers = "1,2,3,4,5,6,7";
            String ticketNumbers = "1,2,3,4,5,6,8"; // 第7个号码不同

            Ticket testTicket = new Ticket();
            testTicket.setNumbers(ticketNumbers);

            String prizeLevel = lotteryService.checkWinning(testTicket, winningNumbers);

            return "一等奖".equals(prizeLevel);
        } catch (Exception e) {
            log("一等奖测试异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 测试5个号码匹配（二等奖）
     */
    private boolean testFiveMatch() {
        try {
            String winningNumbers = "1,2,3,4,5,6,7";
            String ticketNumbers = "1,2,3,4,5,8,9"; // 第6-7个号码不同

            Ticket testTicket = new Ticket();
            testTicket.setNumbers(ticketNumbers);

            String prizeLevel = lotteryService.checkWinning(testTicket, winningNumbers);

            return "二等奖".equals(prizeLevel);
        } catch (Exception e) {
            log("二等奖测试异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 测试4个号码匹配（三等奖）
     */
    private boolean testFourMatch() {
        try {
            String winningNumbers = "1,2,3,4,5,6,7";
            String ticketNumbers = "1,2,3,4,8,9,10"; // 第5-7个号码不同

            Ticket testTicket = new Ticket();
            testTicket.setNumbers(ticketNumbers);

            String prizeLevel = lotteryService.checkWinning(testTicket, winningNumbers);

            return "三等奖".equals(prizeLevel);
        } catch (Exception e) {
            log("三等奖测试异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 测试3个号码匹配（未中奖）
     */
    private boolean testThreeMatch() {
        try {
            String winningNumbers = "1,2,3,4,5,6,7";
            String ticketNumbers = "1,2,3,8,9,10,11"; // 只有3个匹配

            Ticket testTicket = new Ticket();
            testTicket.setNumbers(ticketNumbers);

            String prizeLevel = lotteryService.checkWinning(testTicket, winningNumbers);

            return "未中奖".equals(prizeLevel);
        } catch (Exception e) {
            log("未中奖测试异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 测试无号码匹配（未中奖）
     */
    private boolean testNoMatch() {
        try {
            String winningNumbers = "1,2,3,4,5,6,7";
            String ticketNumbers = "8,9,10,11,12,13,14"; // 无匹配

            Ticket testTicket = new Ticket();
            testTicket.setNumbers(ticketNumbers);

            String prizeLevel = lotteryService.checkWinning(testTicket, winningNumbers);

            return "未中奖".equals(prizeLevel);
        } catch (Exception e) {
            log("无匹配测试异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 测试数据加载性能
     */
    private void testDataLoading() {
        log("1. 数据加载性能测试:");

        long startTime = System.currentTimeMillis();

        // 清空缓存，强制重新加载
        DataManager newInstance = DataManager.getInstance();

        long endTime = System.currentTimeMillis();
        long loadTime = endTime - startTime;

        log("   数据加载时间: " + loadTime + " 毫秒");
        log("   用户数量: " + newInstance.getAllUsers().size());
        log("   彩票数量: " + newInstance.getAllTickets().size());
        log("   结果数量: " + newInstance.getAllResults().size());
    }

    /**
     * 测试用户查询性能
     */
    private void testUserQueryPerformance() {
        log("2. 用户查询性能测试:");

        List<User> users = dataManager.getAllUsers();
        if (users.isEmpty()) {
            log("   暂无用户数据");
            return;
        }

        // 测试随机查询1000次
        Random random = new Random();
        int testCount = Math.min(1000, users.size());

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < testCount; i++) {
            int userId = users.get(random.nextInt(users.size())).getId();
            User user = userService.getUserInfo(userId);
            // 仅查询，不做其他操作
        }

        long endTime = System.currentTimeMillis();
        long queryTime = endTime - startTime;

        double avgTime = (double) queryTime / testCount;

        log("   随机查询 " + testCount + " 次，总耗时: " + queryTime + " 毫秒");
        log("   平均每次查询: " + String.format("%.3f", avgTime) + " 毫秒");
    }

    /**
     * 测试彩票查询性能
     */
    private void testTicketQueryPerformance() {
        log("3. 彩票查询性能测试:");

        List<Ticket> tickets = dataManager.getAllTickets();
        if (tickets.isEmpty()) {
            log("   暂无彩票数据");
            return;
        }

        // 测试按用户ID查询
        List<User> users = dataManager.getAllUsers();
        if (users.isEmpty()) {
            log("   暂无用户数据");
            return;
        }

        Random random = new Random();
        int testCount = Math.min(100, users.size());

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < testCount; i++) {
            int userId = users.get(random.nextInt(users.size())).getId();
            List<Ticket> userTickets = ticketService.getUserTickets(userId);
            // 仅查询，不做其他操作
        }

        long endTime = System.currentTimeMillis();
        long queryTime = endTime - startTime;

        double avgTime = (double) queryTime / testCount;

        log("   用户彩票查询 " + testCount + " 次，总耗时: " + queryTime + " 毫秒");
        log("   平均每次查询: " + String.format("%.3f", avgTime) + " 毫秒");
    }

    /**
     * 测试抽奖计算性能
     */
    private void testDrawPerformance(int userCount) {
        log("4. 抽奖计算性能测试:");
        log("   模拟 " + userCount + " 个用户的抽奖计算");

        // 创建测试数据
        List<Ticket> testTickets = createTestTickets(userCount);

        // 测试抽奖计算
        long startTime = System.currentTimeMillis();

        int totalMatches = 0;
        String winningNumbers = "1,2,3,4,5,6,7";

        for (Ticket ticket : testTickets) {
            String prizeLevel = lotteryService.checkWinning(ticket, winningNumbers);
            if (!"未中奖".equals(prizeLevel)) {
                totalMatches++;
            }
        }

        long endTime = System.currentTimeMillis();
        long calcTime = endTime - startTime;

        double avgTime = (double) calcTime / testTickets.size();

        log("   计算 " + testTickets.size() + " 张彩票，总耗时: " + calcTime + " 毫秒");
        log("   平均每张彩票计算时间: " + String.format("%.6f", avgTime) + " 毫秒");
        log("   中奖彩票数量: " + totalMatches);
        log("   中奖率: " + String.format("%.4f", (double) totalMatches / testTickets.size() * 100) + "%");
    }

    /**
     * 创建测试彩票数据
     */
    private List<Ticket> createTestTickets(int userCount) {
        List<Ticket> testTickets = new ArrayList<>();
        Random random = new Random();

        // 每个用户随机1-5张彩票
        int totalTickets = userCount * (1 + random.nextInt(5));

        log("   生成 " + totalTickets + " 张测试彩票...");

        for (int i = 0; i < totalTickets; i++) {
            Ticket ticket = new Ticket();
            ticket.setUserId(random.nextInt(userCount) + 1);
            ticket.setNumbers(generateRandomTicketNumbers());
            ticket.setBetCount(1 + random.nextInt(10));
            testTickets.add(ticket);

            // 显示进度
            if (i % 10000 == 0 && i > 0) {
                double progress = (double) i / totalTickets * 100;
                log("   生成进度: " + String.format("%.1f", progress) + "% (" + i + "/" + totalTickets + ")");
            }
        }

        return testTickets;
    }

    /**
     * 生成随机彩票号码
     */
    private String generateRandomTicketNumbers() {
        Random random = new Random();
        Set<Integer> numbers = new HashSet<>();

        while (numbers.size() < 7) {
            numbers.add(1 + random.nextInt(36));
        }

        List<Integer> sortedNumbers = new ArrayList<>(numbers);
        Collections.sort(sortedNumbers);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sortedNumbers.size(); i++) {
            sb.append(sortedNumbers.get(i));
            if (i < sortedNumbers.size() - 1) {
                sb.append(",");
            }
        }

        return sb.toString();
    }

    /**
     * 记录日志
     */
    private static void log(String message) {
        String timestamp = dateFormat.format(new Date());
        String logMessage = "[" + timestamp + "] " + message;

        // 输出到控制台
        System.out.println(logMessage);

        // 输出到文件
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            writer.println(logMessage);
        } catch (Exception e) {
            System.err.println("写入日志文件失败: " + e.getMessage());
        }
    }
}