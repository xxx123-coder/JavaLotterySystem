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
        System.out.println("[LotteryService] 开始执行抽奖...");

        // 生成7个不重复的中奖号码（1-36随机）
        int[] winningNumbersArray = NumberUtils.generateRandomNumbers(7, 1, 36);

        // 确保号码排序（可选，便于比较）
        java.util.Arrays.sort(winningNumbersArray);

        // 转换为字符串格式
        StringBuilder winningNumbersBuilder = new StringBuilder();
        for (int i = 0; i < winningNumbersArray.length; i++) {
            winningNumbersBuilder.append(winningNumbersArray[i]);
            if (i < winningNumbersArray.length - 1) {
                winningNumbersBuilder.append(",");
            }
        }
        String winningNumbers = winningNumbersBuilder.toString();

        System.out.println("[LotteryService] 生成中奖号码: " + winningNumbers);

        // 获取所有未开奖的彩票
        List<Ticket> allTickets = dataManager.getAllTickets();
        System.out.println("[LotteryService] 当前彩票数量: " + allTickets.size());

        List<LotteryResult> newResults = new ArrayList<>();
        int totalWinningTickets = 0;

        for (Ticket ticket : allTickets) {
            // 判断是否中奖
            String prizeLevel = checkWinning(ticket, winningNumbers);

            if (!prizeLevel.equals("未中奖")) {
                System.out.println("[LotteryService] 发现中奖彩票: ID=" + ticket.getId() +
                        ", 用户ID=" + ticket.getUserId() + ", 等级=" + prizeLevel);

                // 创建中奖结果记录
                LotteryResult result = new LotteryResult();
                result.setWinningNumbers(winningNumbers);
                result.setDrawTime(new Date());
                result.setWinnerUserId(ticket.getUserId());
                result.setPrizeLevel(prizeLevel);
                result.setMultiplier(1); // 默认倍数

                // 注意：这些方法需要在LotteryResult类中添加
                // result.setTicketId(ticket.getId()); // 记录关联的彩票ID

                // 计算奖金
                double prizeAmount = calculatePrizeAmount(prizeLevel) * ticket.getBetCount();
                // result.setPrizeAmount(prizeAmount); // 需要在LotteryResult类中添加

                // 保存中奖结果
                dataManager.addResult(result);
                newResults.add(result);

                // 根据中奖等级给用户发放奖金
                if (prizeAmount > 0) {
                    UserService userService = new UserService(dataManager);
                    boolean rechargeSuccess = userService.recharge(ticket.getUserId(), prizeAmount);
                    System.out.println("[LotteryService] 奖金发放" + (rechargeSuccess ? "成功" : "失败") +
                            ": 用户ID=" + ticket.getUserId() + ", 金额=" + prizeAmount);
                }

                totalWinningTickets++;
            }
        }

        // 记录抽奖日志
        System.out.println("[LotteryService] 抽奖完成: 中奖号码=" + winningNumbers +
                ", 中奖彩票数=" + totalWinningTickets +
                ", 新增中奖记录=" + newResults.size());

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

        // 验证号码数量
        if (ticketNumberStrs.length != 7 || winningNumberStrs.length != 7) {
            System.err.println("[LotteryService] 号码数量错误: 彩票=" + ticketNumberStrs.length +
                    ", 开奖=" + winningNumberStrs.length);
            return "未中奖";
        }

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
            System.err.println("[LotteryService] 号码格式错误: " + e.getMessage());
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

    /**
     * 新增：获取所有开奖结果
     * @return 所有开奖结果列表
     */
    public List<LotteryResult> getAllLotteryResults() {
        return dataManager.getAllResults();
    }

    /**
     * 新增：获取指定中奖号码的中奖彩票数量
     * @param winningNumbers 中奖号码
     * @return 中奖彩票数量
     */
    public int getWinningTicketCount(String winningNumbers) {
        if (winningNumbers == null || winningNumbers.isEmpty()) {
            return 0;
        }

        List<LotteryResult> allResults = dataManager.getAllResults();
        int count = 0;

        for (LotteryResult result : allResults) {
            if (result.getWinningNumbers() != null &&
                    result.getWinningNumbers().equals(winningNumbers) &&
                    !result.getPrizeLevel().equals("未中奖")) {
                count++;
            }
        }

        return count;
    }
}