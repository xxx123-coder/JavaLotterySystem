package lottery.model;

/**
 * 用户实体类
 * 存储用户的基本信息和账户余额
 */
public class User {
    private int id;             // 用户ID
    private String username;    // 用户名
    private String password;    // 密码
    private double balance;     // 账户余额
    private String phone;       // 电话号码

    /**
     * 无参构造函数
     */
    public User() {
    }

    /**
     * 全参构造函数
     * @param id 用户ID
     * @param username 用户名
     * @param password 密码
     * @param balance 账户余额
     * @param phone 电话号码
     */
    public User(int id, String username, String password, double balance, String phone) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.balance = balance;
        this.phone = phone;
    }

    // Getter和Setter方法
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", balance=" + balance +
                ", phone='" + phone + '\'' +
                '}';
    }
}