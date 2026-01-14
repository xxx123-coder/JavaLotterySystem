package lottery.service; // 定义包路径，表明该类属于lottery.service包，负责抽奖业务逻辑

// 导入必要的类
import lottery.dao.DataManager; // 导入数据管理器，用于数据访问
import java.util.*; // 导入Java常用工具类，包括List、Map、ArrayList、HashMap等

/**
 * 抽奖服务
 * 负责彩票开奖、中奖计算和结果管理
 */
public class LotteryService {
    private DataManager dataManager; // 数据管理器实例，用于操作数据

    /**
     * 构造函数
     * @param dataManager 数据管理器实例
     */
    public LotteryService(DataManager dataManager) { // 构造函数
        this.dataManager = dataManager; // 初始化数据管理器
    }

    /**
     * 执行抽奖
     * @return Map<String, Object> 包含抽奖结果的Map
     */
    public Map<String, Object> drawLottery() { // 修改返回类型为Map
        // 生成7个随机号码（1-36）
        Set<Integer> numbers = new TreeSet<>(); // 使用TreeSet自动排序且不重复
        Random random = new Random(); // 创建随机数生成器

        while (numbers.size() < 7) { // 循环直到生成7个不重复的号码
            numbers.add(random.nextInt(36) + 1); // 生成1-36的随机数
        }

        // 格式化中奖号码
        StringBuilder winningNumbers = new StringBuilder(); // 创建字符串构建器
        for (int num : numbers) { // 遍历生成的号码
            winningNumbers.append(String.format("%02d ", num)); // 格式化为两位数，加空格
        }
        String result = winningNumbers.toString().trim(); // 转换为字符串并去除首尾空格

        // 生成开奖结果ID和期号
        int resultId = generateResultId(); // 调用方法生成开奖结果ID
        int period = getNextPeriod(); // 调用方法获取下一期期号

        // 保存开奖结果
        Map<String, Object> lotteryResult = new HashMap<>(); // 创建HashMap存储开奖结果
        lotteryResult.put("id", resultId); // 放入结果ID
        lotteryResult.put("period", period); // 放入期号
        lotteryResult.put("winningNumbers", result); // 放入中奖号码
        lotteryResult.put("drawTime", new Date()); // 放入当前时间作为抽奖时间

        dataManager.addResult(lotteryResult); // 调用数据管理器保存开奖结果

        // 计算并保存中奖记录
        int totalWinners = 0; // 总中奖人数统计
        int totalPrizeAmount = 0; // 总奖金金额统计
        List<Map<String, Object>> allWinnings = new ArrayList<>(); // 存储所有中奖记录

        // 获取所有用户
        List<Map<String, Object>> allUsers = dataManager.getAllUsers(); // 从数据管理器获取所有用户
        for (Map<String, Object> user : allUsers) { // 遍历所有用户
            int userId = ((Number) user.get("id")).intValue(); // 获取用户ID并转换为int
            Map<String, Object> checkResult = checkWinning(userId, result); // 检查该用户是否中奖

            if ((Boolean) checkResult.get("hasWinning")) { // 如果该用户有中奖
                List<Map<String, Object>> userWinnings = (List<Map<String, Object>>) checkResult.get("winnings"); // 获取用户的中奖详情
                for (Map<String, Object> winning : userWinnings) { // 遍历用户的中奖记录
                    Map<String, Object> winningRecord = new HashMap<>(); // 创建中奖记录Map
                    winningRecord.put("id", generateWinningId()); // 放入中奖记录ID
                    winningRecord.put("userId", userId); // 放入用户ID
                    winningRecord.put("ticketId", winning.get("ticketId")); // 放入彩票ID
                    winningRecord.put("resultId", resultId); // 放入开奖结果ID
                    winningRecord.put("matchCount", winning.get("matchCount")); // 放入匹配号码数量
                    winningRecord.put("prizeLevel", winning.get("prizeLevel")); // 放入中奖等级
                    winningRecord.put("prizeAmount", winning.get("prizeAmount")); // 放入奖金金额
                    winningRecord.put("winTime", new Date()); // 放入中奖时间
                    winningRecord.put("isNotified", false); // 放入通知状态，初始为未通知

                    dataManager.addWinning(winningRecord); // 保存中奖记录到数据管理器
                    allWinnings.add(winningRecord); // 添加到总中奖记录列表

                    totalWinners++; // 中奖人数加1
                    totalPrizeAmount += ((Number) winning.get("prizeAmount")).doubleValue(); // 累加总奖金金额

                    // 更新用户余额（中奖金额）
                    double currentBalance = ((Number) user.get("balance")).doubleValue(); // 获取用户当前余额
                    user.put("balance", currentBalance + ((Number) winning.get("prizeAmount")).doubleValue()); // 更新余额，增加中奖金额
                    dataManager.updateUser(user); // 保存用户信息到数据管理器
                }
            }
        }

        // 返回抽奖结果
        Map<String, Object> drawResult = new HashMap<>(); // 创建抽奖结果Map
        drawResult.put("success", true); // 放入成功标志
        drawResult.put("winningNumbers", result); // 放入中奖号码
        drawResult.put("period", period); // 放入期号
        drawResult.put("resultId", resultId); // 放入开奖结果ID
        drawResult.put("totalWinners", totalWinners); // 放入总中奖人数
        drawResult.put("totalPrizeAmount", totalPrizeAmount); // 放入总奖金金额
        drawResult.put("message", "抽奖完成！" + (totalWinners > 0 ?
                "共有" + totalWinners + "位用户中奖，总奖金￥" + totalPrizeAmount : "无人中奖")); // 放入消息

        return drawResult; // 返回抽奖结果
    }

    /**
     * 获取所有开奖结果
     * @return List<Map<String, Object>> 所有开奖结果列表
     */
    public List<Map<String, Object>> getAllLotteryResults() { // 获取所有开奖结果
        return dataManager.getAllResults(); // 调用数据管理器获取所有开奖结果
    }

    /**
     * 检查中奖情况
     * @param userId 用户ID
     * @param winningNumbers 中奖号码字符串
     * @return Map<String, Object> 包含中奖情况的Map
     */
    public Map<String, Object> checkWinning(int userId, String winningNumbers) { // 检查中奖情况
        List<Map<String, Object>> userTickets = dataManager.getTicketsByUserId(userId); // 获取该用户的所有彩票
        Map<String, Object> result = new HashMap<>(); // 创建结果Map
        result.put("hasWinning", false); // 初始化为没有中奖
        result.put("winnings", new ArrayList<Map<String, Object>>()); // 初始化中奖详情列表

        for (Map<String, Object> ticket : userTickets) { // 遍历用户的所有彩票
            String ticketNumbers = (String) ticket.get("numbers"); // 获取彩票号码
            int matchCount = countMatchingNumbers(ticketNumbers, winningNumbers); // 计算匹配号码数量

            if (matchCount > 0) { // 如果匹配数量大于0，表示中奖
                Map<String, Object> winning = new HashMap<>(); // 创建中奖详情Map
                winning.put("ticketId", ticket.get("id")); // 放入彩票ID
                winning.put("numbers", ticketNumbers); // 放入彩票号码
                winning.put("matchCount", matchCount); // 放入匹配数量
                winning.put("prizeLevel", getPrizeLevel(matchCount)); // 放入中奖等级
                winning.put("prizeAmount", calculatePrize(matchCount,
                        ((Number) ticket.get("betCount")).intValue())); // 放入奖金金额

                ((List<Map<String, Object>>) result.get("winnings")).add(winning); // 将中奖详情添加到结果列表
                result.put("hasWinning", true); // 设置为有中奖
            }
        }

        return result; // 返回结果
    }

    /**
     * 获取用户中奖记录
     * @param userId 用户ID
     * @return List<Map<String, Object>> 用户的中奖记录列表
     */
    public List<Map<String, Object>> getUserWinningResults(int userId) { // 获取用户中奖记录
        List<Map<String, Object>> allResults = dataManager.getAllResults(); // 获取所有开奖结果
        List<Map<String, Object>> userWinnings = new ArrayList<>(); // 创建用户中奖记录列表

        for (Map<String, Object> result : allResults) { // 遍历所有开奖结果
            String winningNumbers = (String) result.get("winningNumbers"); // 获取开奖号码
            Map<String, Object> checkResult = checkWinning(userId, winningNumbers); // 检查该用户在该期是否中奖

            if ((Boolean) checkResult.get("hasWinning")) { // 如果中奖
                Map<String, Object> winning = new HashMap<>(); // 创建中奖记录Map
                winning.put("period", result.get("period")); // 放入期号
                winning.put("winningNumbers", winningNumbers); // 放入开奖号码
                winning.put("drawTime", result.get("drawTime")); // 放入开奖时间
                winning.put("details", checkResult.get("winnings")); // 放入中奖详情
                userWinnings.add(winning); // 添加到用户中奖记录列表
            }
        }

        return userWinnings; // 返回用户中奖记录
    }

    /**
     * 获取用户中奖通知（新增）
     * @param userId 用户ID
     * @return List<Map<String, Object>> 用户的未读中奖通知列表
     */
    public List<Map<String, Object>> getUserWinningNotifications(int userId) { // 获取用户中奖通知
        return dataManager.getUnreadWinningsByUserId(userId); // 调用数据管理器获取未读中奖通知
    }

    /**
     * 标记用户中奖通知为已读（新增）
     * @param userId 用户ID
     */
    public void markUserNotificationsAsRead(int userId) { // 标记用户中奖通知为已读
        dataManager.markWinningsAsRead(userId); // 调用数据管理器标记为已读
    }

    /**
     * 生成开奖结果ID
     * @return int 下一个可用的开奖结果ID
     */
    private int generateResultId() { // 生成开奖结果ID
        List<Map<String, Object>> results = dataManager.getAllResults(); // 获取所有开奖结果
        int maxId = 0; // 最大ID初始化为0
        for (Map<String, Object> result : results) { // 遍历所有开奖结果
            int id = ((Number) result.get("id")).intValue(); // 获取当前结果的ID
            if (id > maxId) maxId = id; // 更新最大ID
        }
        return maxId + 1; // 返回最大ID加1
    }

    /**
     * 获取下一期期号
     * @return int 下一期期号
     */
    private int getNextPeriod() { // 获取下一期期号
        List<Map<String, Object>> results = dataManager.getAllResults(); // 获取所有开奖结果
        int maxPeriod = 0; // 最大期号初始化为0
        for (Map<String, Object> result : results) { // 遍历所有开奖结果
            int period = ((Number) result.get("period")).intValue(); // 获取当前期号
            if (period > maxPeriod) maxPeriod = period; // 更新最大期号
        }
        return maxPeriod + 1; // 返回最大期号加1
    }

    /**
     * 生成中奖记录ID（新增）
     * @return int 下一个可用的中奖记录ID
     */
    private int generateWinningId() { // 生成中奖记录ID
        List<Map<String, Object>> winnings = dataManager.getAllWinnings(); // 获取所有中奖记录
        int maxId = 0; // 最大ID初始化为0
        for (Map<String, Object> winning : winnings) { // 遍历所有中奖记录
            int id = ((Number) winning.get("id")).intValue(); // 获取当前中奖记录ID
            if (id > maxId) maxId = id; // 更新最大ID
        }
        return maxId + 1; // 返回最大ID加1
    }

    /**
     * 计算匹配号码数量
     * @param ticketNumbers 彩票号码字符串
     * @param winningNumbers 中奖号码字符串
     * @return int 匹配的号码数量
     */
    private int countMatchingNumbers(String ticketNumbers, String winningNumbers) { // 计算匹配号码数量
        // 转换格式：彩票号码用逗号分隔，中奖号码用空格分隔
        String[] ticketArray = ticketNumbers.split(","); // 按逗号分割彩票号码
        String[] winningArray = winningNumbers.split(" "); // 按空格分割中奖号码

        Set<String> ticketSet = new HashSet<>(Arrays.asList(ticketArray)); // 将彩票号码数组转换为Set
        Set<String> winningSet = new HashSet<>(Arrays.asList(winningArray)); // 将中奖号码数组转换为Set

        ticketSet.retainAll(winningSet); // 取两个Set的交集
        return ticketSet.size(); // 返回交集大小，即匹配的号码数量
    }

    /**
     * 获取中奖等级
     * @param matchCount 匹配的号码数量
     * @return String 中奖等级
     */
    private String getPrizeLevel(int matchCount) { // 获取中奖等级
        switch (matchCount) { // 根据匹配数量判断
            case 7: return "特等奖"; // 匹配7个为特等奖
            case 6: return "一等奖"; // 匹配6个为一等奖
            case 5: return "二等奖"; // 匹配5个为二等奖
            case 4: return "三等奖"; // 匹配4个为三等奖
            default: return "未中奖"; // 其他为未中奖
        }
    }

    /**
     * 计算奖金
     * @param matchCount 匹配的号码数量
     * @param betCount 投注倍数
     * @return double 奖金金额
     */
    private double calculatePrize(int matchCount, int betCount) { // 计算奖金
        double prizePerBet = 0; // 每注奖金初始化为0
        switch (matchCount) { // 根据匹配数量判断每注奖金
            case 7: prizePerBet = 5000000; break; // 特等奖500万
            case 6: prizePerBet = 100000; break; // 一等奖10万
            case 5: prizePerBet = 5000; break; // 二等奖5000
            case 4: prizePerBet = 100; break; // 三等奖100
            default: return 0; // 其他情况奖金为0
        }
        return prizePerBet * betCount; // 返回总奖金 = 每注奖金 × 投注倍数
    }
}