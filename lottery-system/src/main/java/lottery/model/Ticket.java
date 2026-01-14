package lottery.model; // 定义包路径，表明该类属于lottery.model包，负责彩票实体

// 导入日期类
import java.util.Date; // 导入Date类，用于处理购买时间

/**
 * 彩票实体类
 * 存储彩票的购买信息
 */
public class Ticket {
    // 实体字段定义
    private int id;             // 彩票ID - 唯一标识符
    private int userId;         // 购买用户ID - 关联用户的ID
    private String numbers;     // 号码（格式如"1,2,3,4,5,6,7"） - 彩票号码
    private int betCount;       // 投注数 - 购买的注数
    private Date purchaseTime;  // 购买时间 - 彩票购买的时间
    private boolean isManual;   // 是否手动选号 - true表示手动选号，false表示随机选号
    private String type;        // 彩票类型 - 添加此字段，manual或random
    private boolean drawn;      // 是否已开奖 - true表示已开奖，false表示未开奖

    /**
     * 无参构造函数
     * 创建空的Ticket对象
     */
    public Ticket() {
    }

    /**
     * 全参构造函数
     * 使用所有参数创建Ticket对象
     * @param id 彩票ID
     * @param userId 用户ID
     * @param numbers 彩票号码
     * @param betCount 投注数
     * @param purchaseTime 购买时间
     * @param isManual 是否手动选号
     */
    public Ticket(int id, int userId, String numbers, int betCount, Date purchaseTime, boolean isManual) {
        this.id = id; // 设置彩票ID
        this.userId = userId; // 设置用户ID
        this.numbers = numbers; // 设置彩票号码
        this.betCount = betCount; // 设置投注数
        this.purchaseTime = purchaseTime; // 设置购买时间
        this.isManual = isManual; // 设置是否手动选号
        this.type = isManual ? "manual" : "random"; // 根据isManual设置彩票类型
    }

    // Getter和Setter方法
    public int getId() { // 获取彩票ID
        return id; // 返回彩票ID
    }

    public void setId(int id) { // 设置彩票ID
        this.id = id; // 设置彩票ID
    }

    public int getUserId() { // 获取用户ID
        return userId; // 返回用户ID
    }

    public void setUserId(int userId) { // 设置用户ID
        this.userId = userId; // 设置用户ID
    }

    public String getNumbers() { // 获取彩票号码
        return numbers; // 返回彩票号码
    }

    public void setNumbers(String numbers) { // 设置彩票号码
        this.numbers = numbers; // 设置彩票号码
    }

    public int getBetCount() { // 获取投注数
        return betCount; // 返回投注数
    }

    public void setBetCount(int betCount) { // 设置投注数
        this.betCount = betCount; // 设置投注数
    }

    public Date getPurchaseTime() { // 获取购买时间
        return purchaseTime; // 返回购买时间
    }

    public void setPurchaseTime(Date purchaseTime) { // 设置购买时间
        this.purchaseTime = purchaseTime; // 设置购买时间
    }

    public boolean isManual() { // 获取是否手动选号
        return isManual; // 返回是否手动选号
    }

    public void setManual(boolean manual) { // 设置是否手动选号
        isManual = manual; // 设置是否手动选号
        this.type = manual ? "manual" : "random"; // 同时更新彩票类型
    }

    /**
     * 为了兼容ExcelDao，添加type的getter
     * @return 彩票类型，manual或random
     */
    public String getType() { // 获取彩票类型
        return type != null ? type : (isManual ? "manual" : "random"); // 如果type不为空返回type，否则根据isManual计算
    }

    /**
     * 为了兼容ExcelDao，添加type的setter
     * @param type 彩票类型，manual或random
     */
    public void setType(String type) { // 设置彩票类型
        this.type = type; // 设置彩票类型
        this.isManual = "manual".equals(type); // 同时更新isManual字段
    }

    public boolean isDrawn() { // 获取是否已开奖
        return drawn; // 返回是否已开奖
    }

    public void setDrawn(boolean drawn) { // 设置是否已开奖
        this.drawn = drawn; // 设置是否已开奖
    }

    /**
     * 重写toString方法
     * 返回对象的字符串表示，便于调试和日志输出
     * @return 对象的字符串表示
     */
    @Override
    public String toString() { // 重写Object类的toString方法
        return "Ticket{" + // 返回格式化的字符串
                "id=" + id +
                ", userId=" + userId +
                ", numbers='" + numbers + '\'' +
                ", betCount=" + betCount +
                ", purchaseTime=" + purchaseTime +
                ", isManual=" + isManual +
                '}';
    }
}