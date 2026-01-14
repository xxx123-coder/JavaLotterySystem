package lottery.service; // 定义包路径，表明该类属于lottery.service包，负责彩票购买业务逻辑

// 导入必要的类
import lottery.dao.DataManager; // 导入数据管理器，用于数据访问
import java.util.*; // 导入Java常用工具类，包括List、Map、ArrayList、HashMap等

/**
 * 彩票服务类
 * 负责彩票的购买、查询和管理
 */
public class TicketService {
    private final DataManager dataManager; // 数据管理器实例，final表示不可变
    private static final double PRICE_PER_BET = 2.0; // 每注价格常量，2元
    private static final int NUMBERS_COUNT = 7; // 每张彩票的号码数量常量
    private static final int MIN_NUMBER = 1; // 最小号码常量
    private static final int MAX_NUMBER = 36; // 最大号码常量

    /**
     * 构造函数
     * @param dataManager 数据管理器实例
     */
    public TicketService(DataManager dataManager) { // 构造函数
        if (dataManager == null) { // 检查参数是否为null
            throw new IllegalArgumentException("DataManager不能为null"); // 抛出异常
        }
        this.dataManager = dataManager; // 初始化数据管理器
        System.out.println("TicketService初始化完成"); // 输出初始化完成日志
    }

    /**
     * 手动选号购买彩票
     * @param userId 用户ID
     * @param numbers 号码字符串，用逗号分隔
     * @param betCount 投注数
     * @return Map<String, Object> 购买的彩票信息
     */
    public Map<String, Object> buyManualTicket(int userId, String numbers, int betCount) { // 手动选号购买彩票
        // 验证参数
        if (betCount <= 0) { // 检查投注数是否大于0
            throw new IllegalArgumentException("注数必须大于0"); // 抛出异常
        }

        if (numbers == null || numbers.trim().isEmpty()) { // 检查号码是否为空
            throw new IllegalArgumentException("号码不能为空"); // 抛出异常
        }

        // 验证号码格式
        String[] numberArray = numbers.split(","); // 按逗号分割号码字符串
        if (numberArray.length != NUMBERS_COUNT) { // 检查号码数量是否为7个
            throw new IllegalArgumentException("必须选择" + NUMBERS_COUNT + "个号码"); // 抛出异常
        }

        // 验证号码范围
        Set<Integer> selectedNumbers = new TreeSet<>(); // 使用TreeSet存储号码，自动排序且去重
        for (String numStr : numberArray) { // 遍历号码数组
            try {
                int num = Integer.parseInt(numStr.trim()); // 将字符串转换为整数
                if (num < MIN_NUMBER || num > MAX_NUMBER) { // 检查号码是否在1-36范围内
                    throw new IllegalArgumentException("号码必须在" + MIN_NUMBER + "-" + MAX_NUMBER + "之间"); // 抛出异常
                }
                selectedNumbers.add(num); // 添加到Set中
            } catch (NumberFormatException e) { // 捕获数字格式异常
                throw new IllegalArgumentException("号码格式不正确: " + numStr); // 抛出异常
            }
        }

        if (selectedNumbers.size() != NUMBERS_COUNT) { // 检查是否有重复号码
            throw new IllegalArgumentException("号码不能重复"); // 抛出异常
        }

        // 排序号码
        StringBuilder sortedNumbers = new StringBuilder(); // 创建字符串构建器
        for (int num : selectedNumbers) { // 遍历已排序的号码
            sortedNumbers.append(num).append(","); // 追加号码和逗号
        }
        String sortedNumbersStr = sortedNumbers.substring(0, sortedNumbers.length() - 1); // 去除最后一个逗号

        // 计算总金额
        double totalCost = calculateTotalCost(betCount); // 调用方法计算总金额

        // 检查用户余额
        Map<String, Object> user = dataManager.findUserById(userId); // 根据用户ID查找用户
        if (user == null) { // 如果用户不存在
            throw new IllegalArgumentException("用户不存在"); // 抛出异常
        }

        double balance = ((Number) user.get("balance")).doubleValue(); // 获取用户余额
        if (balance < totalCost) { // 检查余额是否足够
            throw new IllegalArgumentException("余额不足"); // 抛出异常
        }

        // 扣款
        user.put("balance", balance - totalCost); // 更新用户余额

        // 创建彩票数据
        Map<String, Object> ticket = new HashMap<>(); // 创建彩票Map
        ticket.put("id", generateTicketId()); // 放入彩票ID
        ticket.put("userId", userId); // 放入用户ID
        ticket.put("numbers", sortedNumbersStr); // 放入号码字符串
        ticket.put("betCount", betCount); // 放入投注数
        ticket.put("purchaseTime", new Date()); // 放入当前时间作为购买时间
        ticket.put("manual", true); // 设置为手动选号

        // 保存彩票
        dataManager.addTicket(ticket); // 调用数据管理器保存彩票

        // 更新用户余额
        dataManager.saveAll(); // 保存所有数据（包括用户余额和彩票）

        return ticket; // 返回彩票信息
    }

    /**
     * 随机选号购买彩票
     * @param userId 用户ID
     * @param betCount 投注数
     * @return Map<String, Object> 购买的彩票信息
     */
    public Map<String, Object> buyRandomTicket(int userId, int betCount) { // 随机选号购买彩票
        if (betCount <= 0) { // 检查投注数是否大于0
            throw new IllegalArgumentException("注数必须大于0"); // 抛出异常
        }

        // 生成随机号码
        Random random = new Random(); // 创建随机数生成器
        Set<Integer> numbers = new TreeSet<>(); // 使用TreeSet存储号码，自动排序且去重
        while (numbers.size() < NUMBERS_COUNT) { // 循环直到生成7个不重复的号码
            numbers.add(random.nextInt(MAX_NUMBER) + 1); // 生成1-36的随机数
        }

        // 格式化号码
        StringBuilder numbersBuilder = new StringBuilder(); // 创建字符串构建器
        for (int num : numbers) { // 遍历生成的号码
            numbersBuilder.append(num).append(","); // 追加号码和逗号
        }
        String numbersStr = numbersBuilder.substring(0, numbersBuilder.length() - 1); // 去除最后一个逗号

        // 计算总金额
        double totalCost = calculateTotalCost(betCount); // 调用方法计算总金额

        // 检查用户余额
        Map<String, Object> user = dataManager.findUserById(userId); // 根据用户ID查找用户
        if (user == null) { // 如果用户不存在
            throw new IllegalArgumentException("用户不存在"); // 抛出异常
        }

        double balance = ((Number) user.get("balance")).doubleValue(); // 获取用户余额
        if (balance < totalCost) { // 检查余额是否足够
            throw new IllegalArgumentException("余额不足"); // 抛出异常
        }

        // 扣款
        user.put("balance", balance - totalCost); // 更新用户余额

        // 创建彩票数据
        Map<String, Object> ticket = new HashMap<>(); // 创建彩票Map
        ticket.put("id", generateTicketId()); // 放入彩票ID
        ticket.put("userId", userId); // 放入用户ID
        ticket.put("numbers", numbersStr); // 放入号码字符串
        ticket.put("betCount", betCount); // 放入投注数
        ticket.put("purchaseTime", new Date()); // 放入当前时间作为购买时间
        ticket.put("manual", false); // 设置为随机选号

        // 保存彩票
        dataManager.addTicket(ticket); // 调用数据管理器保存彩票

        // 更新用户余额
        dataManager.saveAll(); // 保存所有数据

        return ticket; // 返回彩票信息
    }

    /**
     * 获取用户的彩票
     * @param userId 用户ID
     * @return List<Map<String, Object>> 用户的彩票列表
     */
    public List<Map<String, Object>> getUserTickets(int userId) { // 获取用户的彩票
        if (userId <= 0) { // 检查用户ID是否有效
            throw new IllegalArgumentException("用户ID无效"); // 抛出异常
        }
        return dataManager.getTicketsByUserId(userId); // 调用数据管理器获取用户的彩票
    }

    /**
     * 获取所有彩票
     * @return List<Map<String, Object>> 所有彩票列表
     */
    public List<Map<String, Object>> getAllTickets() { // 获取所有彩票
        return dataManager.getAllTickets(); // 调用数据管理器获取所有彩票
    }

    /**
     * 计算总价格
     * @param betCount 投注数
     * @return double 总价格
     */
    public double calculateTotalCost(int betCount) { // 计算总价格
        return betCount * PRICE_PER_BET; // 返回投注数乘以每注价格
    }

    /**
     * 生成彩票ID
     * @return int 下一个可用的彩票ID
     */
    private int generateTicketId() { // 生成彩票ID
        List<Map<String, Object>> tickets = dataManager.getAllTickets(); // 获取所有彩票
        int maxId = 0; // 最大ID初始化为0
        for (Map<String, Object> ticket : tickets) { // 遍历所有彩票
            int id = ((Number) ticket.get("id")).intValue(); // 获取当前彩票ID
            if (id > maxId) maxId = id; // 更新最大ID
        }
        return maxId + 1; // 返回最大ID加1
    }
}