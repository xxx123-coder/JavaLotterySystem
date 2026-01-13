package lottery.service;

import lottery.dao.DataManager;
import lottery.model.User;

import java.util.List;

/**
 * 用户服务类，处理用户相关业务逻辑
 */
public class UserService {
    private final DataManager dataManager;

    public UserService() {
        // DataManager是单例模式，使用getInstance()
        this.dataManager = DataManager.getInstance();
    }

    public UserService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * 用户注册
     * @param username 用户名
     * @param password 密码
     * @param phone 手机号
     * @return 注册是否成功
     */
    public synchronized boolean register(String username, String password, String phone) {
        // 参数验证
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("手机号不能为空");
        }

        // 检查用户名是否已存在
        List<User> users = dataManager.getAllUsers();
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return false; // 用户名已存在
            }
        }

        // 密码加密（简单使用hashCode）
        String encryptedPassword = String.valueOf(password.hashCode());

        // 创建新用户
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(encryptedPassword);
        newUser.setPhone(phone);
        newUser.setBalance(0.0);
        // ID由DataManager自动分配

        // 保存用户
        dataManager.addUser(newUser);
        return true;
    }

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 登录成功的用户对象，失败返回null
     */
    public synchronized User login(String username, String password) {
        if (username == null || password == null) {
            return null;
        }

        // 密码加密
        String encryptedPassword = String.valueOf(password.hashCode());

        // 查找用户
        List<User> users = dataManager.getAllUsers();
        for (User user : users) {
            if (user.getUsername().equals(username) &&
                    user.getPassword().equals(encryptedPassword)) {
                return user; // 登录成功
            }
        }

        return null; // 登录失败
    }

    /**
     * 用户充值
     * @param userId 用户ID
     * @param amount 充值金额
     * @return 充值是否成功
     */
    public synchronized boolean recharge(int userId, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("充值金额必须大于0");
        }

        User user = dataManager.getUserById(userId);
        if (user == null) {
            return false; // 用户不存在
        }

        // 更新余额
        double newBalance = user.getBalance() + amount;
        user.setBalance(newBalance);

        // 保存更新
        dataManager.updateUser(user);
        return true;
    }

    /**
     * 用户扣款
     * @param userId 用户ID
     * @param amount 扣款金额
     * @return 扣款是否成功
     */
    public synchronized boolean deduct(int userId, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("扣款金额必须大于0");
        }

        User user = dataManager.getUserById(userId);
        if (user == null) {
            return false; // 用户不存在
        }

        // 检查余额是否充足
        if (user.getBalance() < amount) {
            return false; // 余额不足
        }

        // 更新余额
        double newBalance = user.getBalance() - amount;
        user.setBalance(newBalance);

        // 保存更新
        dataManager.updateUser(user);
        return true;
    }

    /**
     * 获取用户信息
     * @param userId 用户ID
     * @return 用户对象
     */
    public User getUserInfo(int userId) {
        return dataManager.getUserById(userId);
    }

    /**
     * 获取用户余额
     * @param userId 用户ID
     * @return 用户余额
     */
    public double getUserBalance(int userId) {
        User user = dataManager.getUserById(userId);
        return user != null ? user.getBalance() : -1.0;
    }
}