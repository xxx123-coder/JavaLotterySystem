package lottery.model;

import java.util.Date;

/**
 * 抽奖结果实体类
 * 存储彩票开奖结果和中奖信息
 */
public class LotteryResult {
    private int id;                 // 结果ID
    private String winningNumbers;  // 中奖号码
    private Date drawTime;          // 抽奖时间
    private int winnerUserId;       // 中奖用户ID
    private String prizeLevel;      // 中奖等级（特等奖/一等奖）
    private int multiplier;         // 中奖倍数
    private int ticketId;
    private double prizeAmount;

    /**
     * 无参构造函数
     */
    public LotteryResult() {
    }

    /**
     * 全参构造函数
     * @param id 结果ID
     * @param winningNumbers 中奖号码
     * @param drawTime 抽奖时间
     * @param winnerUserId 中奖用户ID
     * @param prizeLevel 中奖等级
     * @param multiplier 中奖倍数
     */
    public LotteryResult(int id, String winningNumbers, Date drawTime, int winnerUserId,
                         String prizeLevel, int multiplier) {
        this.id = id;
        this.winningNumbers = winningNumbers;
        this.drawTime = drawTime;
        this.winnerUserId = winnerUserId;
        this.prizeLevel = prizeLevel;
        this.multiplier = multiplier;
    }

    // Getter和Setter方法
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWinningNumbers() {
        return winningNumbers;
    }

    public void setWinningNumbers(String winningNumbers) {
        this.winningNumbers = winningNumbers;
    }

    public Date getDrawTime() {
        return drawTime;
    }

    public void setDrawTime(Date drawTime) {
        this.drawTime = drawTime;
    }

    public int getWinnerUserId() {
        return winnerUserId;
    }

    public void setWinnerUserId(int winnerUserId) {
        this.winnerUserId = winnerUserId;
    }

    public String getPrizeLevel() {
        return prizeLevel;
    }

    public void setPrizeLevel(String prizeLevel) {
        this.prizeLevel = prizeLevel;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(int multiplier) {
        this.multiplier = multiplier;
    }

    @Override
    public String toString() {
        return "LotteryResult{" +
                "id=" + id +
                ", winningNumbers='" + winningNumbers + '\'' +
                ", drawTime=" + drawTime +
                ", winnerUserId=" + winnerUserId +
                ", prizeLevel='" + prizeLevel + '\'' +
                ", multiplier=" + multiplier +
                '}';
    }
    public void setTicketId(int ticketId) {
        this.ticketId = ticketId; // 假设有 ticketId 字段
    }

    public void setPrizeAmount(double prizeAmount) {
        this.prizeAmount = prizeAmount; // 假设有 prizeAmount 字段
    }

    public double getPrizeAmount() {
        return this.prizeAmount;
    }
}