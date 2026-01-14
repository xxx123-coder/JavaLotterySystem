package lottery.model; // 定义包路径，表明该类属于lottery.model包，负责开奖结果实体

// 导入日期类
import java.util.Date; // 导入Date类，用于处理日期和时间

/**
 * 抽奖结果实体类
 * 存储彩票开奖结果和中奖信息
 */
public class LotteryResult {
    // 实体字段定义
    private int id;                 // 结果ID - 唯一标识符
    private String winningNumbers;  // 中奖号码 - 格式如"1,2,3,4,5,6,7"
    private Date drawTime;          // 抽奖时间 - 开奖的具体日期和时间
    private int winnerUserId;       // 中奖用户ID - 中奖用户的唯一标识
    private String prizeLevel;      // 中奖等级（特等奖/一等奖） - 如"特等奖"、"一等奖"
    private int multiplier;         // 中奖倍数 - 投注的倍数
    private int ticketId;           // 彩票ID - 中奖彩票的唯一标识
    private double prizeAmount;     // 奖金金额 - 中奖金额
    private String period;          // 期号 - 开奖的期次编号
    private String numbers;         // 号码（与winningNumbers一致） - 冗余字段，与winningNumbers相同
    private String prizeInfo;       // 奖金信息 - 包含中奖等级和倍数的组合信息

    /**
     * 无参构造函数
     * 创建空的LotteryResult对象
     */
    public LotteryResult() {
    }

    /**
     * 全参构造函数
     * 使用所有参数创建LotteryResult对象
     * @param id 结果ID
     * @param winningNumbers 中奖号码
     * @param drawTime 抽奖时间
     * @param winnerUserId 中奖用户ID
     * @param prizeLevel 中奖等级
     * @param multiplier 中奖倍数
     * @param ticketId 彩票ID
     * @param prizeAmount 奖金金额
     * @param period 期号
     */
    public LotteryResult(int id, String winningNumbers, Date drawTime, int winnerUserId,
                         String prizeLevel, int multiplier, int ticketId, double prizeAmount, String period) {
        this.id = id; // 设置结果ID
        this.winningNumbers = winningNumbers; // 设置中奖号码
        this.numbers = winningNumbers; // 同时设置numbers字段，与winningNumbers一致
        this.drawTime = drawTime; // 设置抽奖时间
        this.winnerUserId = winnerUserId; // 设置中奖用户ID
        this.prizeLevel = prizeLevel; // 设置中奖等级
        this.multiplier = multiplier; // 设置中奖倍数
        this.ticketId = ticketId; // 设置彩票ID
        this.prizeAmount = prizeAmount; // 设置奖金金额
        this.period = period; // 设置期号
        this.prizeInfo = prizeLevel + " x" + multiplier; // 根据中奖等级和倍数生成奖金信息
    }

    // Getter和Setter方法
    public int getId() { // 获取结果ID
        return id; // 返回结果ID
    }

    public void setId(int id) { // 设置结果ID
        this.id = id; // 设置结果ID
        if (this.period == null) { // 如果期号为空
            this.period = String.valueOf(id); // 使用结果ID作为期号
        }
    }

    public String getWinningNumbers() { // 获取中奖号码
        return winningNumbers; // 返回中奖号码
    }

    public void setWinningNumbers(String winningNumbers) { // 设置中奖号码
        this.winningNumbers = winningNumbers; // 设置中奖号码
        this.numbers = winningNumbers; // 同时更新numbers字段
    }

    public Date getDrawTime() { // 获取抽奖时间
        return drawTime; // 返回抽奖时间
    }

    public void setDrawTime(Date drawTime) { // 设置抽奖时间
        this.drawTime = drawTime; // 设置抽奖时间
    }

    public int getWinnerUserId() { // 获取中奖用户ID
        return winnerUserId; // 返回中奖用户ID
    }

    public void setWinnerUserId(int winnerUserId) { // 设置中奖用户ID
        this.winnerUserId = winnerUserId; // 设置中奖用户ID
    }

    public String getPrizeLevel() { // 获取中奖等级
        return prizeLevel; // 返回中奖等级
    }

    public void setPrizeLevel(String prizeLevel) { // 设置中奖等级
        this.prizeLevel = prizeLevel; // 设置中奖等级
        updatePrizeInfo(); // 更新奖金信息
    }

    public int getMultiplier() { // 获取中奖倍数
        return multiplier; // 返回中奖倍数
    }

    public void setMultiplier(int multiplier) { // 设置中奖倍数
        this.multiplier = multiplier; // 设置中奖倍数
        updatePrizeInfo(); // 更新奖金信息
    }

    public int getTicketId() { // 获取彩票ID
        return ticketId; // 返回彩票ID
    }

    public void setTicketId(int ticketId) { // 设置彩票ID
        this.ticketId = ticketId; // 设置彩票ID
    }

    public double getPrizeAmount() { // 获取奖金金额
        return prizeAmount; // 返回奖金金额
    }

    public void setPrizeAmount(double prizeAmount) { // 设置奖金金额
        this.prizeAmount = prizeAmount; // 设置奖金金额
    }

    public String getPeriod() { // 获取期号
        return period != null ? period : String.valueOf(id); // 如果期号不为空返回期号，否则返回结果ID
    }

    public void setPeriod(String period) { // 设置期号
        this.period = period; // 设置期号
    }

    public String getNumbers() { // 获取号码
        return numbers != null ? numbers : winningNumbers; // 如果numbers不为空返回numbers，否则返回winningNumbers
    }

    public void setNumbers(String numbers) { // 设置号码
        this.numbers = numbers; // 设置numbers字段
        this.winningNumbers = numbers; // 同时更新winningNumbers字段
    }

    public String getPrizeInfo() { // 获取奖金信息
        return prizeInfo != null ? prizeInfo : (prizeLevel + " x" + multiplier); // 如果prizeInfo不为空返回prizeInfo，否则根据中奖等级和倍数生成
    }

    public void setPrizeInfo(String prizeInfo) { // 设置奖金信息
        this.prizeInfo = prizeInfo; // 设置奖金信息
    }

    /**
     * 更新奖金信息
     * 根据当前中奖等级和倍数生成奖金信息字符串
     */
    private void updatePrizeInfo() { // 私有方法，用于更新奖金信息
        this.prizeInfo = prizeLevel + " x" + multiplier; // 将中奖等级和倍数组合成字符串
    }

    /**
     * 重写toString方法
     * 返回对象的字符串表示，便于调试和日志输出
     * @return 对象的字符串表示
     */
    @Override
    public String toString() { // 重写Object类的toString方法
        return "LotteryResult{" + // 返回格式化的字符串
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