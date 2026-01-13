package lottery.model;

import java.util.Date;

/**
 * 彩票实体类
 * 存储彩票的购买信息
 */
public class Ticket {
    private int id;             // 彩票ID
    private int userId;         // 购买用户ID
    private String numbers;     // 号码（格式如"1,2,3,4,5,6,7"）
    private int betCount;       // 投注数
    private Date purchaseTime;  // 购买时间
    private boolean isManual;   // 是否手动选号
    private boolean drawn;

    /**
     * 无参构造函数
     */
    public Ticket() {
    }

    /**
     * 全参构造函数
     * @param id 彩票ID
     * @param userId 购买用户ID
     * @param numbers 号码
     * @param betCount 投注数
     * @param purchaseTime 购买时间
     * @param isManual 是否手动选号
     */
    public Ticket(int id, int userId, String numbers, int betCount, Date purchaseTime, boolean isManual) {
        this.id = id;
        this.userId = userId;
        this.numbers = numbers;
        this.betCount = betCount;
        this.purchaseTime = purchaseTime;
        this.isManual = isManual;
    }

    // Getter和Setter方法
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getNumbers() {
        return numbers;
    }

    public void setNumbers(String numbers) {
        this.numbers = numbers;
    }

    public int getBetCount() {
        return betCount;
    }

    public void setBetCount(int betCount) {
        this.betCount = betCount;
    }

    public Date getPurchaseTime() {
        return purchaseTime;
    }

    public void setPurchaseTime(Date purchaseTime) {
        this.purchaseTime = purchaseTime;
    }

    public boolean isManual() {
        return isManual;
    }

    public void setManual(boolean manual) {
        isManual = manual;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "id=" + id +
                ", userId=" + userId +
                ", numbers='" + numbers + '\'' +
                ", betCount=" + betCount +
                ", purchaseTime=" + purchaseTime +
                ", isManual=" + isManual +
                '}';
    }
    public boolean isDrawn() {
        return this.drawn; // 假设有 drawn 字段
    }

    public void setDrawn(boolean drawn) {
        this.drawn = drawn;
    }
}