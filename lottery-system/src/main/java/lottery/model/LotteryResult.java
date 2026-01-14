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
    private int ticketId;           // 彩票ID
    private double prizeAmount;     // 奖金金额
    private String period;          // 期号
    private String numbers;         // 号码（与winningNumbers一致）
    private String prizeInfo;       // 奖金信息

    /**
     * 无参构造函数
     */
    public LotteryResult() {
    }

    /**
     * 全参构造函数
     */
    public LotteryResult(int id, String winningNumbers, Date drawTime, int winnerUserId,
                         String prizeLevel, int multiplier, int ticketId, double prizeAmount, String period) {
        this.id = id;
        this.winningNumbers = winningNumbers;
        this.numbers = winningNumbers;
        this.drawTime = drawTime;
        this.winnerUserId = winnerUserId;
        this.prizeLevel = prizeLevel;
        this.multiplier = multiplier;
        this.ticketId = ticketId;
        this.prizeAmount = prizeAmount;
        this.period = period;
        this.prizeInfo = prizeLevel + " x" + multiplier;
    }

    // Getter和Setter方法
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        if (this.period == null) {
            this.period = String.valueOf(id);
        }
    }

    public String getWinningNumbers() {
        return winningNumbers;
    }

    public void setWinningNumbers(String winningNumbers) {
        this.winningNumbers = winningNumbers;
        this.numbers = winningNumbers;
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
        updatePrizeInfo();
    }

    public int getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(int multiplier) {
        this.multiplier = multiplier;
        updatePrizeInfo();
    }

    public int getTicketId() {
        return ticketId;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }

    public double getPrizeAmount() {
        return prizeAmount;
    }

    public void setPrizeAmount(double prizeAmount) {
        this.prizeAmount = prizeAmount;
    }

    public String getPeriod() {
        return period != null ? period : String.valueOf(id);
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getNumbers() {
        return numbers != null ? numbers : winningNumbers;
    }

    public void setNumbers(String numbers) {
        this.numbers = numbers;
        this.winningNumbers = numbers;
    }

    public String getPrizeInfo() {
        return prizeInfo != null ? prizeInfo : (prizeLevel + " x" + multiplier);
    }

    public void setPrizeInfo(String prizeInfo) {
        this.prizeInfo = prizeInfo;
    }

    // 更新奖金信息
    private void updatePrizeInfo() {
        this.prizeInfo = prizeLevel + " x" + multiplier;
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
                ", ticketId=" + ticketId +
                ", prizeAmount=" + prizeAmount +
                ", period='" + period + '\'' +
                '}';
    }
}