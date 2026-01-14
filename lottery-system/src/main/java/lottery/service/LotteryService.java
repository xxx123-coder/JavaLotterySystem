package lottery.service;

import lottery.dao.DataManager;
import java.util.*;

/**
 * 抽奖服务
 */
public class LotteryService {
    private DataManager dataManager;

    public LotteryService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * 执行抽奖
     */
    public Map<String, Object> drawLottery() { // 修改返回类型为Map
        // 生成7个随机号码（1-36）
        Set<Integer> numbers = new TreeSet<>();
        Random random = new Random();

        while (numbers.size() < 7) {
            numbers.add(random.nextInt(36) + 1);
        }

        // 格式化中奖号码
        StringBuilder winningNumbers = new StringBuilder();
        for (int num : numbers) {
            winningNumbers.append(String.format("%02d ", num));
        }
        String result = winningNumbers.toString().trim();

        // 生成开奖结果ID和期号
        int resultId = generateResultId();
        int period = getNextPeriod();

        // 保存开奖结果
        Map<String, Object> lotteryResult = new HashMap<>();
        lotteryResult.put("id", resultId);
        lotteryResult.put("period", period);
        lotteryResult.put("winningNumbers", result);
        lotteryResult.put("drawTime", new Date());

        dataManager.addResult(lotteryResult);

        // 计算并保存中奖记录
        int totalWinners = 0;
        int totalPrizeAmount = 0;
        List<Map<String, Object>> allWinnings = new ArrayList<>();

        // 获取所有用户
        List<Map<String, Object>> allUsers = dataManager.getAllUsers();
        for (Map<String, Object> user : allUsers) {
            int userId = ((Number) user.get("id")).intValue();
            Map<String, Object> checkResult = checkWinning(userId, result);

            if ((Boolean) checkResult.get("hasWinning")) {
                List<Map<String, Object>> userWinnings = (List<Map<String, Object>>) checkResult.get("winnings");
                for (Map<String, Object> winning : userWinnings) {
                    Map<String, Object> winningRecord = new HashMap<>();
                    winningRecord.put("id", generateWinningId());
                    winningRecord.put("userId", userId);
                    winningRecord.put("ticketId", winning.get("ticketId"));
                    winningRecord.put("resultId", resultId);
                    winningRecord.put("matchCount", winning.get("matchCount"));
                    winningRecord.put("prizeLevel", winning.get("prizeLevel"));
                    winningRecord.put("prizeAmount", winning.get("prizeAmount"));
                    winningRecord.put("winTime", new Date());
                    winningRecord.put("isNotified", false);

                    dataManager.addWinning(winningRecord);
                    allWinnings.add(winningRecord);

                    totalWinners++;
                    totalPrizeAmount += ((Number) winning.get("prizeAmount")).doubleValue();

                    // 更新用户余额（中奖金额）
                    double currentBalance = ((Number) user.get("balance")).doubleValue();
                    user.put("balance", currentBalance + ((Number) winning.get("prizeAmount")).doubleValue());
                    dataManager.updateUser(user);
                }
            }
        }

        // 返回抽奖结果
        Map<String, Object> drawResult = new HashMap<>();
        drawResult.put("success", true);
        drawResult.put("winningNumbers", result);
        drawResult.put("period", period);
        drawResult.put("resultId", resultId);
        drawResult.put("totalWinners", totalWinners);
        drawResult.put("totalPrizeAmount", totalPrizeAmount);
        drawResult.put("message", "抽奖完成！" + (totalWinners > 0 ?
                "共有" + totalWinners + "位用户中奖，总奖金￥" + totalPrizeAmount : "无人中奖"));

        return drawResult;
    }

    /**
     * 获取所有开奖结果
     */
    public List<Map<String, Object>> getAllLotteryResults() {
        return dataManager.getAllResults();
    }

    /**
     * 检查中奖情况
     */
    public Map<String, Object> checkWinning(int userId, String winningNumbers) {
        List<Map<String, Object>> userTickets = dataManager.getTicketsByUserId(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("hasWinning", false);
        result.put("winnings", new ArrayList<Map<String, Object>>());

        for (Map<String, Object> ticket : userTickets) {
            String ticketNumbers = (String) ticket.get("numbers");
            int matchCount = countMatchingNumbers(ticketNumbers, winningNumbers);

            if (matchCount > 0) {
                Map<String, Object> winning = new HashMap<>();
                winning.put("ticketId", ticket.get("id"));
                winning.put("numbers", ticketNumbers);
                winning.put("matchCount", matchCount);
                winning.put("prizeLevel", getPrizeLevel(matchCount));
                winning.put("prizeAmount", calculatePrize(matchCount,
                        ((Number) ticket.get("betCount")).intValue()));

                ((List<Map<String, Object>>) result.get("winnings")).add(winning);
                result.put("hasWinning", true);
            }
        }

        return result;
    }

    /**
     * 获取用户中奖记录
     */
    public List<Map<String, Object>> getUserWinningResults(int userId) {
        List<Map<String, Object>> allResults = dataManager.getAllResults();
        List<Map<String, Object>> userWinnings = new ArrayList<>();

        for (Map<String, Object> result : allResults) {
            String winningNumbers = (String) result.get("winningNumbers");
            Map<String, Object> checkResult = checkWinning(userId, winningNumbers);

            if ((Boolean) checkResult.get("hasWinning")) {
                Map<String, Object> winning = new HashMap<>();
                winning.put("period", result.get("period"));
                winning.put("winningNumbers", winningNumbers);
                winning.put("drawTime", result.get("drawTime"));
                winning.put("details", checkResult.get("winnings"));
                userWinnings.add(winning);
            }
        }

        return userWinnings;
    }

    /**
     * 获取用户中奖通知（新增）
     */
    public List<Map<String, Object>> getUserWinningNotifications(int userId) {
        return dataManager.getUnreadWinningsByUserId(userId);
    }

    /**
     * 标记用户中奖通知为已读（新增）
     */
    public void markUserNotificationsAsRead(int userId) {
        dataManager.markWinningsAsRead(userId);
    }

    /**
     * 生成开奖结果ID
     */
    private int generateResultId() {
        List<Map<String, Object>> results = dataManager.getAllResults();
        int maxId = 0;
        for (Map<String, Object> result : results) {
            int id = ((Number) result.get("id")).intValue();
            if (id > maxId) maxId = id;
        }
        return maxId + 1;
    }

    /**
     * 获取下一期期号
     */
    private int getNextPeriod() {
        List<Map<String, Object>> results = dataManager.getAllResults();
        int maxPeriod = 0;
        for (Map<String, Object> result : results) {
            int period = ((Number) result.get("period")).intValue();
            if (period > maxPeriod) maxPeriod = period;
        }
        return maxPeriod + 1;
    }

    /**
     * 生成中奖记录ID（新增）
     */
    private int generateWinningId() {
        List<Map<String, Object>> winnings = dataManager.getAllWinnings();
        int maxId = 0;
        for (Map<String, Object> winning : winnings) {
            int id = ((Number) winning.get("id")).intValue();
            if (id > maxId) maxId = id;
        }
        return maxId + 1;
    }

    /**
     * 计算匹配号码数量
     */
    private int countMatchingNumbers(String ticketNumbers, String winningNumbers) {
        // 转换格式：彩票号码用逗号分隔，中奖号码用空格分隔
        String[] ticketArray = ticketNumbers.split(",");
        String[] winningArray = winningNumbers.split(" ");

        Set<String> ticketSet = new HashSet<>(Arrays.asList(ticketArray));
        Set<String> winningSet = new HashSet<>(Arrays.asList(winningArray));

        ticketSet.retainAll(winningSet);
        return ticketSet.size();
    }

    /**
     * 获取中奖等级
     */
    private String getPrizeLevel(int matchCount) {
        switch (matchCount) {
            case 7: return "特等奖";
            case 6: return "一等奖";
            case 5: return "二等奖";
            case 4: return "三等奖";
            default: return "未中奖";
        }
    }

    /**
     * 计算奖金
     */
    private double calculatePrize(int matchCount, int betCount) {
        double prizePerBet = 0;
        switch (matchCount) {
            case 7: prizePerBet = 5000000; break;
            case 6: prizePerBet = 100000; break;
            case 5: prizePerBet = 5000; break;
            case 4: prizePerBet = 100; break;
            default: return 0;
        }
        return prizePerBet * betCount;
    }
}