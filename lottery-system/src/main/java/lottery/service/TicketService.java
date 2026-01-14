package lottery.service;

import lottery.dao.DataManager;
import java.util.*;

/**
 * 彩票服务类
 */
public class TicketService {
    private final DataManager dataManager;
    private static final double PRICE_PER_BET = 2.0;
    private static final int NUMBERS_COUNT = 7;
    private static final int MIN_NUMBER = 1;
    private static final int MAX_NUMBER = 36;

    public TicketService(DataManager dataManager) {
        if (dataManager == null) {
            throw new IllegalArgumentException("DataManager不能为null");
        }
        this.dataManager = dataManager;
        System.out.println("TicketService初始化完成");
    }

    /**
     * 手动选号购买彩票
     */
    public Map<String, Object> buyManualTicket(int userId, String numbers, int betCount) {
        // 验证参数
        if (betCount <= 0) {
            throw new IllegalArgumentException("注数必须大于0");
        }

        if (numbers == null || numbers.trim().isEmpty()) {
            throw new IllegalArgumentException("号码不能为空");
        }

        // 验证号码格式
        String[] numberArray = numbers.split(",");
        if (numberArray.length != NUMBERS_COUNT) {
            throw new IllegalArgumentException("必须选择" + NUMBERS_COUNT + "个号码");
        }

        // 验证号码范围
        Set<Integer> selectedNumbers = new TreeSet<>();
        for (String numStr : numberArray) {
            try {
                int num = Integer.parseInt(numStr.trim());
                if (num < MIN_NUMBER || num > MAX_NUMBER) {
                    throw new IllegalArgumentException("号码必须在" + MIN_NUMBER + "-" + MAX_NUMBER + "之间");
                }
                selectedNumbers.add(num);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("号码格式不正确: " + numStr);
            }
        }

        if (selectedNumbers.size() != NUMBERS_COUNT) {
            throw new IllegalArgumentException("号码不能重复");
        }

        // 排序号码
        StringBuilder sortedNumbers = new StringBuilder();
        for (int num : selectedNumbers) {
            sortedNumbers.append(num).append(",");
        }
        String sortedNumbersStr = sortedNumbers.substring(0, sortedNumbers.length() - 1);

        // 计算总金额
        double totalCost = calculateTotalCost(betCount);

        // 检查用户余额
        Map<String, Object> user = dataManager.findUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        double balance = ((Number) user.get("balance")).doubleValue();
        if (balance < totalCost) {
            throw new IllegalArgumentException("余额不足");
        }

        // 扣款
        user.put("balance", balance - totalCost);

        // 创建彩票数据
        Map<String, Object> ticket = new HashMap<>();
        ticket.put("id", generateTicketId());
        ticket.put("userId", userId);
        ticket.put("numbers", sortedNumbersStr);
        ticket.put("betCount", betCount);
        ticket.put("purchaseTime", new Date());
        ticket.put("manual", true);

        // 保存彩票
        dataManager.addTicket(ticket);

        // 更新用户余额
        dataManager.saveAll();

        return ticket;
    }

    /**
     * 随机选号购买彩票
     */
    public Map<String, Object> buyRandomTicket(int userId, int betCount) {
        if (betCount <= 0) {
            throw new IllegalArgumentException("注数必须大于0");
        }

        // 生成随机号码
        Random random = new Random();
        Set<Integer> numbers = new TreeSet<>();
        while (numbers.size() < NUMBERS_COUNT) {
            numbers.add(random.nextInt(MAX_NUMBER) + 1);
        }

        // 格式化号码
        StringBuilder numbersBuilder = new StringBuilder();
        for (int num : numbers) {
            numbersBuilder.append(num).append(",");
        }
        String numbersStr = numbersBuilder.substring(0, numbersBuilder.length() - 1);

        // 计算总金额
        double totalCost = calculateTotalCost(betCount);

        // 检查用户余额
        Map<String, Object> user = dataManager.findUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        double balance = ((Number) user.get("balance")).doubleValue();
        if (balance < totalCost) {
            throw new IllegalArgumentException("余额不足");
        }

        // 扣款
        user.put("balance", balance - totalCost);

        // 创建彩票数据
        Map<String, Object> ticket = new HashMap<>();
        ticket.put("id", generateTicketId());
        ticket.put("userId", userId);
        ticket.put("numbers", numbersStr);
        ticket.put("betCount", betCount);
        ticket.put("purchaseTime", new Date());
        ticket.put("manual", false);

        // 保存彩票
        dataManager.addTicket(ticket);

        // 更新用户余额
        dataManager.saveAll();

        return ticket;
    }

    /**
     * 获取用户的彩票
     */
    public List<Map<String, Object>> getUserTickets(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("用户ID无效");
        }
        return dataManager.getTicketsByUserId(userId);
    }

    /**
     * 获取所有彩票
     */
    public List<Map<String, Object>> getAllTickets() {
        return dataManager.getAllTickets();
    }

    /**
     * 计算总价格
     */
    public double calculateTotalCost(int betCount) {
        return betCount * PRICE_PER_BET;
    }

    /**
     * 生成彩票ID
     */
    private int generateTicketId() {
        List<Map<String, Object>> tickets = dataManager.getAllTickets();
        int maxId = 0;
        for (Map<String, Object> ticket : tickets) {
            int id = ((Number) ticket.get("id")).intValue();
            if (id > maxId) maxId = id;
        }
        return maxId + 1;
    }
}