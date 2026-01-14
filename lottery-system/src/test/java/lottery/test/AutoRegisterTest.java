package lottery.test;

import lottery.dao.DataManager;
import lottery.service.UserService;
import lottery.service.TicketService;
import java.util.Random;

/**
 * 自动注册和购买彩票测试
 */
public class AutoRegisterTest {
    public static void main(String[] args) {
        System.out.println("开始自动注册用户...");

        try {
            DataManager dataManager = DataManager.getInstance();
            UserService userService = new UserService(dataManager);
            TicketService ticketService = new TicketService(dataManager);

            Random random = new Random();

            // 注册10万用户
            for (int i = 1; i <= 100000; i++) {
                String username = "user" + i;
                String password = "pass" + i;
                String phone = "138" + String.format("%08d", i);

                userService.register(username, password, phone);

                // 为每个用户充值
                userService.recharge(i, 1000); // 每人充值1000元

                // 随机购买彩票
                int ticketCount = random.nextInt(10) + 1; // 每人买1-10张
                for (int j = 0; j < ticketCount; j++) {
                    if (random.nextBoolean()) {
                        // 手动选号
                        String numbers = generateRandomNumbers();
                        ticketService.buyManualTicket(i, numbers, 1);
                    } else {
                        // 随机选号
                        ticketService.buyRandomTicket(i, 1);
                    }
                }

                if (i % 1000 == 0) {
                    System.out.println("已注册 " + i + " 个用户");
                }
            }

            System.out.println("自动注册完成！");
            System.out.println("用户数量: " + dataManager.getUserCount());
            System.out.println("彩票数量: " + dataManager.getTicketCount());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成随机号码
     */
    private static String generateRandomNumbers() {
        Random random = new Random();
        StringBuilder numbers = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            numbers.append(String.format("%02d ", random.nextInt(36) + 1));
        }
        return numbers.toString().trim();
    }
}