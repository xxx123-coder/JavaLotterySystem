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
    public String drawLottery() {
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

        // 保存开奖结果
        Map<String, Object> lotteryResult = new HashMap<>();
        lotteryResult.put("id", generateResultId());
        lotteryResult.put("period", getNextPeriod());
        lotteryResult.put("winningNumbers", result);
        lotteryResult.put("drawTime", new Date());

        dataManager.addResult(lotteryResult);
        return result;
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
     * 计算匹配号码数量
     */
    private int countMatchingNumbers(String ticketNumbers, String winningNumbers) {
        Set<String> ticketSet = new HashSet<>(Arrays.asList(ticketNumbers.split(" ")));
        Set<String> winningSet = new HashSet<>(Arrays.asList(winningNumbers.split(" ")));

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