package lottery.service;

import lottery.dao.DataManager;
import java.util.*;

/**
 * 用户服务
 */
public class UserService {
    private DataManager dataManager;

    public UserService(DataManager dataManager) {
        this.dataManager = dataManager;
        System.out.println("UserService初始化完成");
    }

    /**
     * 用户登录
     */
    public Map<String, Object> login(String username, String password) {
        Map<String, Object> user = dataManager.findUserByUsername(username);
        if (user != null && user.get("password").equals(password)) {
            return user;
        }
        return null;
    }

    /**
     * 用户注册
     */
    public boolean register(String username, String password, String phone) {
        if (dataManager.findUserByUsername(username) != null) {
            return false; // 用户名已存在
        }

        // 生成用户ID
        List<Map<String, Object>> users = dataManager.getAllUsers();
        int maxId = 0;
        for (Map<String, Object> user : users) {
            Object idObj = user.get("id");
            if (idObj != null) {
                int userId;
                if (idObj instanceof Integer) {
                    userId = (Integer) idObj;
                } else if (idObj instanceof Double) {
                    userId = ((Double) idObj).intValue();
                } else {
                    continue;
                }
                if (userId > maxId) maxId = userId;
            }
        }

        Map<String, Object> user = new HashMap<>();
        user.put("id", maxId + 1);
        user.put("username", username);
        user.put("password", password);
        user.put("phone", phone);
        user.put("balance", 0.0);

        dataManager.addUser(user);
        return true;
    }

    /**
     * 获取用户信息
     */
    public Map<String, Object> getUserInfo(int userId) {
        return dataManager.findUserById(userId);
    }

    /**
     * 用户充值
     */
    public boolean recharge(int userId, double amount) {
        Map<String, Object> user = dataManager.findUserById(userId);
        if (user == null) return false;

        // 获取当前余额
        double currentBalance = 0.0;
        Object balanceObj = user.get("balance");
        if (balanceObj instanceof Double) {
            currentBalance = (Double) balanceObj;
        } else if (balanceObj instanceof Integer) {
            currentBalance = ((Integer) balanceObj).doubleValue();
        }

        // 更新余额
        user.put("balance", currentBalance + amount);

        // 使用更新方法
        dataManager.updateUser(user);
        return true;
    }

    /**
     * 扣款
     */
    public boolean deduct(int userId, double amount) {
        Map<String, Object> user = dataManager.findUserById(userId);
        if (user == null) return false;

        // 获取当前余额
        double currentBalance = 0.0;
        Object balanceObj = user.get("balance");
        if (balanceObj instanceof Double) {
            currentBalance = (Double) balanceObj;
        } else if (balanceObj instanceof Integer) {
            currentBalance = ((Integer) balanceObj).doubleValue();
        }

        if (currentBalance < amount) return false;

        // 更新余额
        user.put("balance", currentBalance - amount);

        // 使用更新方法
        dataManager.updateUser(user);
        return true;
    }
}