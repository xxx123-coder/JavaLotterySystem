package lottery.service;

import lottery.dao.DataManager;
import lottery.model.LotteryResult;
import lottery.model.Ticket;
import lottery.util.NumberUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 抽奖服务类，处理抽奖和中奖判断逻辑
 */
public class LotteryService {
    private final DataManager dataManager;

    public LotteryService() {
        // DataManager是单例模式，使用getInstance()
        this.dataManager = DataManager.getInstance();
    }

    public LotteryService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * 执行抽奖
     * @return 中奖号码字符串
     */
    public synchronized String drawLottery() {
        // 生成7个中奖号码（1-36随机）
        int[] winningNumbersArray = NumberUtils.generateRandomNumbers(7, 1, 36);

        // 转换为字符串格式
        StringBuilder winningNumbersBuilder = new StringBuilder();
        for (int i = 0; i < winningNumbersArray.length; i++) {
            winningNumbersBuilder.append(winningNumbersArray[i]);
            if (i < winningNumbersArray.length - 1) {
                winningNumbersBuilder.append(",");
            }
        }
        String winningNumbers = winningNumbersBuilder.toString();

        // 遍历所有彩票判断中奖情况
        List<Ticket> allTickets = dataManager.getAllTickets();
        List<Ticket> updatedTickets = new ArrayList<>();

        for (Ticket ticket : allTickets) {
            // 判断是否中奖
            String prizeLevel = checkWinning(ticket, winningNumbers);

            if (!prizeLevel.equals("未中奖")) {
                // 创建中奖结果记录
                LotteryResult result = new LotteryResult();
                result.setWinningNumbers(winningNumbers);
                result.setDrawTime(new Date());
                result.setWinnerUserId(ticket.getUserId());
                result.setPrizeLevel(prizeLevel);
                result.setMultiplier(1); // 默认倍数

                // 保存中奖结果
                dataManager.addResult(result);

                // 根据中奖等级给用户发放奖金
                double prizeAmount = calculatePrizeAmount(prizeLevel);
                if (prizeAmount > 0) {
                    UserService userService = new UserService(dataManager);
                    userService.recharge(ticket.getUserId(), prizeAmount);
                }
            }
        }

        return winningNumbers;
    }

    /**
     * 判断单张彩票是否中奖
     * @param ticket 彩票对象
     * @param winningNumbers 中奖号码字符串
     * @return 中奖等级（特等奖/一等奖/二等奖/三等奖/未中奖）
     */
    public String checkWinning(Ticket ticket, String winningNumbers) {
        if (ticket == null || winningNumbers == null || winningNumbers.isEmpty()) {
            return "未中奖";
        }

        // 解析彩票号码和中奖号码
        String[] ticketNumberStrs = ticket.getNumbers().split(",");
        String[] winningNumberStrs = winningNumbers.split(",");

        int[] ticketNumbers = new int[ticketNumberStrs.length];
        int[] winningNumbersArray = new int[winningNumberStrs.length];

        try {
            for (int i = 0; i < ticketNumberStrs.length; i++) {
                ticketNumbers[i] = Integer.parseInt(ticketNumberStrs[i].trim());
            }
            for (int i = 0; i < winningNumberStrs.length; i++) {
                winningNumbersArray[i] = Integer.parseInt(winningNumberStrs[i].trim());
            }
        } catch (NumberFormatException e) {
            return "未中奖";
        }

        // 计算匹配数量
        int matchCount = 0;
        for (int ticketNum : ticketNumbers) {
            for (int winningNum : winningNumbersArray) {
                if (ticketNum == winningNum) {
                    matchCount++;
                    break;
                }
            }
        }

        // 确定中奖等级
        switch (matchCount) {
            case 7:
                return "特等奖";
            case 6:
                return "一等奖";
            case 5:
                return "二等奖";
            case 4:
                return "三等奖";
            default:
                return "未中奖";
        }
    }

    /**
     * 计算奖金金额
     * @param prizeLevel 中奖等级
     * @return 奖金金额
     */
    private double calculatePrizeAmount(String prizeLevel) {
        switch (prizeLevel) {
            case "特等奖":
                return 1000000.0;
            case "一等奖":
                return 50000.0;
            case "二等奖":
                return 1000.0;
            case "三等奖":
                return 100.0;
            default:
                return 0.0;
        }
    }

    /**
     * 检查用户是否有中奖记录
     * @param userId 用户ID
     * @return 是否有中奖记录
     */
    public boolean hasWinningNotification(int userId) {
        List<LotteryResult> userResults = getUserWinningResults(userId);
        return !userResults.isEmpty();
    }

    /**
     * 获取用户的中奖记录
     * @param userId 用户ID
     * @return 中奖结果列表
     */
    public List<LotteryResult> getUserWinningResults(int userId) {
        List<LotteryResult> userResults = new ArrayList<>();
        List<LotteryResult> allResults = dataManager.getAllResults();

        for (LotteryResult result : allResults) {
            if (result.getWinnerUserId() == userId) {
                userResults.add(result);
            }
        }

        return userResults;
    }
}