package lottery.test;

import lottery.service.UserService;
import lottery.service.TicketService;
import lottery.dao.DataManager;
import lottery.model.User;
import lottery.model.Ticket;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * 自动注册和购票测试类
 * 用于批量测试用户注册和彩票购买功能
 */
public class AutoRegisterTest {
    private final UserService userService;
    private final TicketService ticketService;
    private final DataManager dataManager;
    private final Random random;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String LOG_FILE = "test_log_" + dateFormat.format(new Date()).replaceAll("[: ]", "_") + ".txt";

    public AutoRegisterTest() {
        this.dataManager = DataManager.getInstance();
        this.userService = new UserService(dataManager);
        this.ticketService = new TicketService(dataManager);
        this.random = new Random();

        // 加载现有数据
        dataManager.loadAllData();

        // 初始化日志
        log("========== 彩票系统测试开始 ==========");
        log("测试时间: " + dateFormat.format(new Date()));
    }

    /**
     * 批量注册用户
     * @param count 要注册的用户数量
     */
    public void registerUsers(int count) {
        log("开始批量注册用户，数量: " + count);

        long startTime = System.currentTimeMillis();
        int successCount = 0;

        for (int i = 1; i <= count; i++) {
            try {
                // 生成随机用户名
                String username = String.format("user_%05d", i);

                // 统一密码
                String password = "123456";

                // 生成随机电话号码
                String phone = generateRandomPhone();

                // 注册用户
                boolean success = userService.register(username, password, phone);

                if (success) {
                    // 随机充值10-100元
                    User user = userService.login(username, password);
                    if (user != null) {
                        double amount = 10 + random.nextInt(91); // 10-100元
                        userService.recharge(user.getId(), amount);
                        successCount++;
                    }
                }

                // 显示进度
                if (i % 1000 == 0 || i == count) {
                    double progress = (double) i / count * 100;
                    log(String.format("注册进度: %.1f%% (%d/%d)", progress, i, count));
                }

            } catch (Exception e) {
                log("注册用户失败 (用户" + i + "): " + e.getMessage());
            }

            // 每100个用户保存一次数据，避免内存溢出
            if (i % 100 == 0) {
                dataManager.saveAllData();
            }
        }

        // 最终保存
        dataManager.saveAllData();

        long endTime = System.currentTimeMillis();
        double timeSpent = (endTime - startTime) / 1000.0;

        log(String.format("用户注册完成: 成功 %d/%d, 耗时 %.2f 秒", successCount, count, timeSpent));
    }

    /**
     * 为所有用户批量购买彩票
     */
    public void buyTicketsForUsers() {
        List<User> users = dataManager.getAllUsers();
        log("开始为 " + users.size() + " 个用户购买彩票");

        long startTime = System.currentTimeMillis();
        int totalTickets = 0;

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);

            try {
                // 每个用户随机购买1-5张彩票
                int ticketCount = 1 + random.nextInt(5);

                for (int j = 0; j < ticketCount; j++) {
                    // 随机选择选号方式
                    boolean manual = random.nextBoolean();

                    // 随机投注数（1-10倍）
                    int betCount = 1 + random.nextInt(10);

                    if (manual) {
                        // 手动选号：生成随机号码
                        String numbers = generateRandomNumbers();
                        ticketService.buyManualTicket(user.getId(), numbers, betCount);
                    } else {
                        // 随机选号
                        ticketService.buyRandomTicket(user.getId(), betCount);
                    }

                    totalTickets++;
                }

                // 显示进度
                if (i % 100 == 0 || i == users.size() - 1) {
                    double progress = (double) (i + 1) / users.size() * 100;
                    log(String.format("购票进度: %.1f%% (%d/%d), 已购彩票: %d",
                            progress, i + 1, users.size(), totalTickets));
                }

            } catch (Exception e) {
                log("用户购票失败 (用户" + user.getUsername() + "): " + e.getMessage());
            }

            // 每50个用户保存一次数据
            if (i % 50 == 0) {
                dataManager.saveAllData();
            }
        }

        // 最终保存
        dataManager.saveAllData();

        long endTime = System.currentTimeMillis();
        double timeSpent = (endTime - startTime) / 1000.0;

        log(String.format("彩票购买完成: 共购买 %d 张彩票, 耗时 %.2f 秒", totalTickets, timeSpent));
    }

    /**
     * 执行完整测试流程
     */
    public static void main(String[] args) {
        try {
            AutoRegisterTest test = new AutoRegisterTest();

            // 解析命令行参数
            int userCount = 100000; // 默认10万用户
            if (args.length > 0) {
                try {
                    userCount = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    log("参数格式错误，使用默认值: 100000");
                }
            }

            log("测试配置: 注册用户数 = " + userCount);
            log("--------------------------------------");

            // 1. 批量注册用户
            test.registerUsers(userCount);

            log("--------------------------------------");

            // 2. 批量购买彩票
            test.buyTicketsForUsers();

            log("--------------------------------------");

            // 3. 打印统计信息
            printStatistics(test.dataManager);

            log("========== 测试完成 ==========");

        } catch (Exception e) {
            log("测试过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 打印统计信息
     */
    private static void printStatistics(DataManager dataManager) {
        List<User> users = dataManager.getAllUsers();
        List<Ticket> tickets = dataManager.getAllTickets();

        log("===== 测试结果统计 =====");
        log("总用户数: " + users.size());
        log("总彩票数: " + tickets.size());

        // 计算用户平均余额
        double totalBalance = 0;
        for (User user : users) {
            totalBalance += user.getBalance();
        }
        double avgBalance = users.isEmpty() ? 0 : totalBalance / users.size();
        log("用户平均余额: ¥" + String.format("%.2f", avgBalance));

        // 计算总购票金额
        double totalTicketCost = tickets.size() * 2.0; // 每张票2元
        log("总购票金额: ¥" + String.format("%.2f", totalTicketCost));

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
        log("手动选号票数: " + manualCount);
        log("随机选号票数: " + randomCount);
    }

    /**
     * 生成随机电话号码
     */
    private String generateRandomPhone() {
        // 生成13x, 15x, 18x开头的手机号
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
    private String generateRandomNumbers() {
        StringBuilder numbers = new StringBuilder();
        boolean[] used = new boolean[37]; // 索引1-36

        for (int i = 0; i < 7; i++) {
            int num;
            do {
                num = 1 + random.nextInt(36);
            } while (used[num]);

            used[num] = true;
            numbers.append(num);

            if (i < 6) {
                numbers.append(",");
            }
        }

        return numbers.toString();
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