package lottery.service;

import lottery.dao.DataManager;
import lottery.model.Ticket;
import lottery.model.User;
import lottery.util.NumberUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 彩票服务类，处理彩票购买业务逻辑
 */
public class TicketService {
    private final DataManager dataManager;
    private static final double PRICE_PER_BET = 2.0; // 每注价格

    public TicketService() {
        // DataManager是单例模式，使用getInstance()
        this.dataManager = DataManager.getInstance();
    }

    public TicketService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * 手动选号购买彩票
     * @param userId 用户ID
     * @param numbers 号码字符串，格式如"1,2,3,4,5,6,7"
     * @param betCount 注数
     * @return 购买的彩票对象
     */
    public synchronized Ticket buyManualTicket(int userId, String numbers, int betCount) {
        // 参数验证
        if (betCount <= 0) {
            throw new IllegalArgumentException("注数必须大于0");
        }

        // 验证号码格式
        String[] numberArray = numbers.split(",");
        if (numberArray.length != 7) {
            throw new IllegalArgumentException("必须选择7个号码");
        }

        // 验证号码是否合法（1-36，不重复）
        int[] selectedNumbers = new int[7];
        for (int i = 0; i < 7; i++) {
            try {
                int num = Integer.parseInt(numberArray[i].trim());
                if (num < 1 || num > 36) {
                    throw new IllegalArgumentException("号码必须在1-36之间");
                }
                selectedNumbers[i] = num;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("号码格式不正确");
            }
        }

        // 检查是否有重复号码
        if (!NumberUtils.isUniqueNumbers(selectedNumbers)) {
            throw new IllegalArgumentException("号码不能重复");
        }

        // 计算总金额
        double totalCost = calculateTotalCost(betCount);

        // 检查用户余额是否足够
        User user = dataManager.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (user.getBalance() < totalCost) {
            throw new IllegalArgumentException("余额不足");
        }

        // 扣款
        UserService userService = new UserService(dataManager);
        if (!userService.deduct(userId, totalCost)) {
            throw new RuntimeException("扣款失败");
        }

        // 创建彩票
        Ticket ticket = new Ticket();
        ticket.setUserId(userId);
        ticket.setNumbers(numbers);
        ticket.setBetCount(betCount);
        ticket.setPurchaseTime(new Date());
        ticket.setManual(true); // 手动选号

        // 保存彩票
        dataManager.addTicket(ticket);
        return ticket;
    }

    /**
     * 随机选号购买彩票
     * @param userId 用户ID
     * @param betCount 注数
     * @return 购买的彩票对象
     */
    public synchronized Ticket buyRandomTicket(int userId, int betCount) {
        // 生成7个随机不重复数字（1-36）
        int[] randomNumbers = NumberUtils.generateRandomNumbers(7, 1, 36);

        // 转换为字符串格式
        StringBuilder numbersBuilder = new StringBuilder();
        for (int i = 0; i < randomNumbers.length; i++) {
            numbersBuilder.append(randomNumbers[i]);
            if (i < randomNumbers.length - 1) {
                numbersBuilder.append(",");
            }
        }

        // 调用手动购买方法，但设置为非手动
        Ticket ticket = new Ticket();
        ticket.setUserId(userId);
        ticket.setNumbers(numbersBuilder.toString());
        ticket.setBetCount(betCount);
        ticket.setPurchaseTime(new Date());
        ticket.setManual(false); // 随机选号

        // 计算总金额
        double totalCost = calculateTotalCost(betCount);

        // 检查用户余额是否足够
        User user = dataManager.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (user.getBalance() < totalCost) {
            throw new IllegalArgumentException("余额不足");
        }

        // 扣款
        UserService userService = new UserService(dataManager);
        if (!userService.deduct(userId, totalCost)) {
            throw new RuntimeException("扣款失败");
        }

        // 保存彩票
        dataManager.addTicket(ticket);
        return ticket;
    }

    /**
     * 获取用户的所有彩票
     * @param userId 用户ID
     * @return 彩票列表
     */
    public List<Ticket> getUserTickets(int userId) {
        return dataManager.getTicketsByUserId(userId);
    }

    /**
     * 计算总价格
     * @param betCount 注数
     * @return 总价格
     */
    public double calculateTotalCost(int betCount) {
        if (betCount <= 0) {
            throw new IllegalArgumentException("注数必须大于0");
        }
        return betCount * PRICE_PER_BET;
    }
}