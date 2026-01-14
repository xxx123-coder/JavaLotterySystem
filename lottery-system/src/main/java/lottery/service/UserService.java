package lottery.service; // 定义包路径，表明该类属于lottery.service包，负责用户业务逻辑

// 导入必要的类
import lottery.dao.DataManager; // 导入数据管理器，用于数据访问
import java.util.*; // 导入Java常用工具类，包括List、Map、ArrayList、HashMap等

/**
 * 用户服务
 * 负责用户登录、注册、信息管理和充值扣款
 */
public class UserService {
    private DataManager dataManager; // 数据管理器实例

    /**
     * 构造函数
     * @param dataManager 数据管理器实例
     */
    public UserService(DataManager dataManager) { // 构造函数
        this.dataManager = dataManager; // 初始化数据管理器
        System.out.println("UserService初始化完成"); // 输出初始化完成日志
    }

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return Map<String, Object> 用户信息，如果登录失败返回null
     */
    public Map<String, Object> login(String username, String password) { // 用户登录
        Map<String, Object> user = dataManager.findUserByUsername(username); // 根据用户名查找用户
        if (user != null && user.get("password").equals(password)) { // 如果用户存在且密码匹配
            // 获取用户ID
            Integer userId = null; // 用户ID变量
            Object idObj = user.get("id"); // 获取ID对象
            if (idObj instanceof Integer) { // 如果是Integer类型
                userId = (Integer) idObj; // 赋值
            } else if (idObj instanceof Double) { // 如果是Double类型
                userId = ((Double) idObj).intValue(); // 转换为int
            }

            if (userId != null) { // 如果用户ID不为null
                // 新增：检查未读中奖通知
                List<Map<String, Object>> unreadWinnings = dataManager.getUnreadWinningsByUserId(userId); // 获取未读中奖通知
                user.put("unreadWinnings", unreadWinnings); // 放入用户信息
                user.put("unreadWinningCount", unreadWinnings.size()); // 放入未读中奖通知数量

                // 确保所有必要字段都存在
                if (!user.containsKey("username")) { // 如果用户信息中没有username字段
                    user.put("username", username); // 放入用户名
                }
                if (!user.containsKey("balance")) { // 如果用户信息中没有balance字段
                    user.put("balance", 0.0); // 放入余额，默认为0
                }
            }

            return user; // 返回用户信息
        }
        return null; // 登录失败返回null
    }

    /**
     * 用户注册
     * @param username 用户名
     * @param password 密码
     * @param phone 电话号码
     * @return boolean 注册是否成功
     */
    public boolean register(String username, String password, String phone) { // 用户注册
        if (dataManager.findUserByUsername(username) != null) { // 检查用户名是否已存在
            return false; // 用户名已存在，返回false
        }

        // 生成用户ID
        List<Map<String, Object>> users = dataManager.getAllUsers(); // 获取所有用户
        int maxId = 0; // 最大ID初始化为0
        for (Map<String, Object> user : users) { // 遍历所有用户
            Object idObj = user.get("id"); // 获取ID对象
            if (idObj != null) { // 如果ID不为null
                int userId; // 用户ID变量
                if (idObj instanceof Integer) { // 如果是Integer类型
                    userId = (Integer) idObj; // 赋值
                } else if (idObj instanceof Double) { // 如果是Double类型
                    userId = ((Double) idObj).intValue(); // 转换为int
                } else {
                    continue; // 其他类型跳过
                }
                if (userId > maxId) maxId = userId; // 更新最大ID
            }
        }

        Map<String, Object> user = new HashMap<>(); // 创建用户Map
        user.put("id", maxId + 1); // 放入用户ID
        user.put("username", username); // 放入用户名
        user.put("password", password); // 放入密码
        user.put("phone", phone); // 放入电话号码
        user.put("balance", 0.0); // 放入初始余额0

        dataManager.addUser(user); // 调用数据管理器添加用户
        return true; // 返回注册成功
    }

    /**
     * 获取用户信息
     * @param userId 用户ID
     * @return Map<String, Object> 用户信息
     */
    public Map<String, Object> getUserInfo(int userId) { // 获取用户信息
        Map<String, Object> user = dataManager.findUserById(userId); // 根据用户ID查找用户
        if (user != null) { // 如果用户存在
            // 新增：检查未读中奖通知
            List<Map<String, Object>> unreadWinnings = dataManager.getUnreadWinningsByUserId(userId); // 获取未读中奖通知
            user.put("unreadWinnings", unreadWinnings); // 放入用户信息
            user.put("unreadWinningCount", unreadWinnings.size()); // 放入未读中奖通知数量

            // 确保所有必要字段都存在
            if (!user.containsKey("balance")) { // 如果用户信息中没有balance字段
                user.put("balance", 0.0); // 放入余额，默认为0
            }
        }
        return user; // 返回用户信息
    }

    /**
     * 用户充值
     * @param userId 用户ID
     * @param amount 充值金额
     * @return boolean 充值是否成功
     */
    public boolean recharge(int userId, double amount) { // 用户充值
        Map<String, Object> user = dataManager.findUserById(userId); // 根据用户ID查找用户
        if (user == null) return false; // 用户不存在，返回false

        // 获取当前余额
        double currentBalance = 0.0; // 当前余额变量
        Object balanceObj = user.get("balance"); // 获取余额对象
        if (balanceObj instanceof Double) { // 如果是Double类型
            currentBalance = (Double) balanceObj; // 赋值
        } else if (balanceObj instanceof Integer) { // 如果是Integer类型
            currentBalance = ((Integer) balanceObj).doubleValue(); // 转换为double
        }

        // 更新余额
        user.put("balance", currentBalance + amount); // 更新余额

        // 使用更新方法
        dataManager.updateUser(user); // 调用数据管理器更新用户
        return true; // 返回充值成功
    }

    /**
     * 扣款
     * @param userId 用户ID
     * @param amount 扣款金额
     * @return boolean 扣款是否成功
     */
    public boolean deduct(int userId, double amount) { // 扣款
        Map<String, Object> user = dataManager.findUserById(userId); // 根据用户ID查找用户
        if (user == null) return false; // 用户不存在，返回false

        // 获取当前余额
        double currentBalance = 0.0; // 当前余额变量
        Object balanceObj = user.get("balance"); // 获取余额对象
        if (balanceObj instanceof Double) { // 如果是Double类型
            currentBalance = (Double) balanceObj; // 赋值
        } else if (balanceObj instanceof Integer) { // 如果是Integer类型
            currentBalance = ((Integer) balanceObj).doubleValue(); // 转换为double
        }

        if (currentBalance < amount) return false; // 余额不足，返回false

        // 更新余额
        user.put("balance", currentBalance - amount); // 更新余额

        // 使用更新方法
        dataManager.updateUser(user); // 调用数据管理器更新用户
        return true; // 返回扣款成功
    }

    /**
     * 获取用户中奖记录（新增）
     * @param userId 用户ID
     * @return List<Map<String, Object>> 用户的中奖记录列表
     */
    public List<Map<String, Object>> getUserWinnings(int userId) { // 获取用户中奖记录
        return dataManager.getWinningsByUserId(userId); // 调用数据管理器获取用户的中奖记录
    }

    /**
     * 标记用户中奖通知为已读（新增）
     * @param userId 用户ID
     * @return boolean 标记是否成功
     */
    public boolean markNotificationsAsRead(int userId) { // 标记用户中奖通知为已读
        try {
            dataManager.markWinningsAsRead(userId); // 调用数据管理器标记为已读
            return true; // 返回成功
        } catch (Exception e) { // 捕获异常
            return false; // 返回失败
        }
    }
}