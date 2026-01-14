package lottery.model; // 定义包路径，表明该类属于lottery.model包，负责用户实体

/**
 * 用户实体类
 * 存储用户的基本信息和账户余额
 */
public class User {
    // 实体字段定义
    private int id;             // 用户ID - 唯一标识符
    private String name;        // 用户名 - 添加此字段，为了兼容ExcelDao
    private String username;    // 用户名 - 登录用户名
    private String password;    // 密码 - 登录密码
    private double balance;     // 账户余额 - 用户账户中的金额
    private String phone;       // 电话号码 - 用户手机号

    /**
     * 无参构造函数
     * 创建空的User对象
     */
    public User() {
    }

    /**
     * 全参构造函数
     * 使用所有参数创建User对象
     * @param id 用户ID
     * @param username 用户名
     * @param password 密码
     * @param balance 账户余额
     * @param phone 电话号码
     */
    public User(int id, String username, String password, double balance, String phone) {
        this.id = id; // 设置用户ID
        this.username = username; // 设置用户名
        this.name = username; // 同时设置name字段，与username保持一致
        this.password = password; // 设置密码
        this.balance = balance; // 设置账户余额
        this.phone = phone; // 设置电话号码
    }

    // Getter和Setter方法
    public int getId() { // 获取用户ID
        return id; // 返回用户ID
    }

    public void setId(int id) { // 设置用户ID
        this.id = id; // 设置用户ID
    }

    /**
     * 为了兼容ExcelDao，添加name的getter
     * @return 用户名，优先返回name字段，如果name为空则返回username
     */
    public String getName() { // 获取用户名
        return name != null ? name : username; // 如果name不为空返回name，否则返回username
    }

    /**
     * 为了兼容ExcelDao，添加name的setter
     * @param name 用户名
     */
    public void setName(String name) { // 设置用户名
        this.name = name; // 设置name字段
        this.username = name; // 同时更新username字段，保持一致性
    }

    public String getUsername() { // 获取用户名
        return username; // 返回username字段
    }

    public void setUsername(String username) { // 设置用户名
        this.username = username; // 设置username字段
        this.name = username; // 同时更新name字段，保持一致性
    }

    public String getPassword() { // 获取密码
        return password; // 返回密码
    }

    public void setPassword(String password) { // 设置密码
        this.password = password; // 设置密码
    }

    public double getBalance() { // 获取账户余额
        return balance; // 返回账户余额
    }

    public void setBalance(double balance) { // 设置账户余额
        this.balance = balance; // 设置账户余额
    }

    public String getPhone() { // 获取电话号码
        return phone; // 返回电话号码
    }

    public void setPhone(String phone) { // 设置电话号码
        this.phone = phone; // 设置电话号码
    }

    /**
     * 重写toString方法
     * 返回对象的字符串表示，便于调试和日志输出
     * @return 对象的字符串表示
     */
    @Override
    public String toString() { // 重写Object类的toString方法
        return "User{" + // 返回格式化的字符串
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", balance=" + balance +
                ", phone='" + phone + '\'' +
                '}';
    }
}