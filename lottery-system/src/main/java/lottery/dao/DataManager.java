package lottery.dao; // 定义包路径，表明该类属于lottery.dao包，负责数据访问层的数据管理

import java.util.*; // 导入Java常用工具类，包括List、Map、ArrayList、HashMap等

/**
 * 数据管理器（单例模式）
 * 使用单例模式确保整个应用程序中只有一个DataManager实例，统一管理数据
 */
public class DataManager {
    private static DataManager instance; // 静态实例变量，用于保存单例对象
    private ExcelDao excelDao; // Excel数据访问对象，用于读写Excel文件
    private boolean initialized = false; // 初始化标志，标记数据是否已成功加载

    // 数据缓存：使用List<Map>结构在内存中缓存数据，避免频繁读写Excel文件
    private List<Map<String, Object>> users = new ArrayList<>(); // 用户数据缓存
    private List<Map<String, Object>> tickets = new ArrayList<>(); // 彩票数据缓存
    private List<Map<String, Object>> results = new ArrayList<>(); // 开奖结果数据缓存
    private List<Map<String, Object>> winnings = new ArrayList<>(); // 新增：中奖记录缓存

    private DataManager() { // 私有构造函数，防止外部直接实例化
        excelDao = new ExcelDao(); // 初始化ExcelDao对象
    }

    public static synchronized DataManager getInstance() { // 静态方法获取单例实例，synchronized保证线程安全
        if (instance == null) { // 如果实例为空，则创建新实例
            instance = new DataManager(); // 创建DataManager实例
            instance.initialize(); // 调用初始化方法加载数据
        }
        return instance; // 返回单例实例
    }

    /**
     * 初始化数据
     * 从Excel文件加载数据到内存缓存中
     */
    private void initialize() {
        System.out.println("正在初始化数据管理器..."); // 输出初始化日志信息

        try {
            // 调用ExcelDao加载各类数据到内存缓存
            users = excelDao.loadUsers(); // 加载用户数据
            tickets = excelDao.loadTickets(); // 加载彩票数据
            results = excelDao.loadResults(); // 加载开奖结果数据
            winnings = excelDao.loadWinnings(); // 新增：加载中奖记录数据

            // 如果没有用户，创建默认用户
            if (users.isEmpty()) { // 检查用户数据是否为空
                System.out.println("创建默认用户..."); // 输出创建默认用户日志
                createDefaultUser(); // 调用方法创建默认用户
            }

            initialized = true; // 设置初始化标志为true，表示数据加载完成
            System.out.println("数据加载完成:"); // 输出加载完成日志
            System.out.println("  用户数量: " + users.size()); // 输出用户数量
            System.out.println("  彩票数量: " + tickets.size()); // 输出彩票数量
            System.out.println("  开奖结果: " + results.size()); // 输出开奖结果数量
            System.out.println("  中奖记录: " + winnings.size()); // 新增：输出中奖记录数量

        } catch (Exception e) { // 捕获初始化过程中的异常
            System.err.println("数据初始化失败: " + e.getMessage()); // 输出错误信息
            // 即使初始化失败，也创建空数据列表，保证程序能继续运行
            users = new ArrayList<>(); // 创建空用户列表
            tickets = new ArrayList<>(); // 创建空彩票列表
            results = new ArrayList<>(); // 创建空开奖结果列表
            winnings = new ArrayList<>(); // 新增：创建空中奖记录列表

            // 创建默认用户
            createDefaultUser(); // 调用方法创建默认用户

            initialized = true; // 设置初始化标志为true
            System.out.println("已使用默认数据"); // 输出使用默认数据日志
        }
    }

    /**
     * 创建默认用户
     * 当没有用户数据时，创建一个管理员用户
     */
    private void createDefaultUser() {
        try {
            Map<String, Object> defaultUser = new HashMap<>(); // 创建HashMap存储默认用户信息
            defaultUser.put("id", 1); // 设置用户ID为1
            defaultUser.put("username", "admin"); // 设置用户名为admin
            defaultUser.put("password", "123456"); // 设置密码为123456
            defaultUser.put("balance", 1000.0); // 设置账户余额为1000.0
            defaultUser.put("phone", "13800000000"); // 设置手机号码

            users.add(defaultUser); // 将默认用户添加到用户缓存列表
            excelDao.saveUsers(users); // 调用ExcelDao将用户数据保存到Excel文件
            System.out.println("默认用户创建成功: admin/123456"); // 输出创建成功日志
        } catch (Exception e) { // 捕获创建默认用户过程中的异常
            System.err.println("创建默认用户失败: " + e.getMessage()); // 输出错误信息
        }
    }

    /**
     * 保存所有数据
     * 将内存缓存中的数据保存到Excel文件中
     */
    public synchronized void saveAll() { // synchronized确保保存操作的线程安全
        try {
            excelDao.saveUsers(users); // 保存用户数据
            excelDao.saveTickets(tickets); // 保存彩票数据
            excelDao.saveResults(results); // 保存开奖结果数据
            excelDao.saveWinnings(winnings); // 新增：保存中奖记录数据
        } catch (Exception e) { // 捕获保存过程中的异常
            System.err.println("保存数据失败: " + e.getMessage()); // 输出错误信息
        }
    }

    /**
     * 关闭系统
     * 在系统关闭前保存所有数据
     */
    public void shutdown() {
        saveAll(); // 调用saveAll方法保存数据
    }

    /**
     * 检查是否已初始化
     */
    public boolean isInitialized() { // 获取初始化状态
        return initialized; // 返回初始化标志
    }

    // 用户相关操作
    public synchronized void addUser(Map<String, Object> user) { // 添加新用户
        users.add(user); // 将用户添加到缓存列表
        saveAll(); // 保存数据到Excel文件
    }

    public synchronized List<Map<String, Object>> getAllUsers() { // 获取所有用户
        return new ArrayList<>(users); // 返回用户列表的副本，防止外部修改内部数据
    }

    public synchronized Map<String, Object> findUserById(int id) { // 根据用户ID查找用户
        for (Map<String, Object> user : users) { // 遍历用户列表
            Object idObj = user.get("id"); // 获取用户的ID值
            if (idObj != null) { // 检查ID值不为空
                int userId; // 定义变量存储用户ID
                if (idObj instanceof Integer) { // 检查ID是否为Integer类型
                    userId = (Integer) idObj; // 强制转换为Integer
                } else if (idObj instanceof Double) { // 检查ID是否为Double类型
                    userId = ((Double) idObj).intValue(); // 转换为Integer
                } else {
                    continue; // 如果类型不匹配，跳过当前用户
                }

                if (userId == id) { // 检查用户ID是否匹配
                    return user; // 返回找到的用户
                }
            }
        }
        return null; // 没有找到返回null
    }

    public synchronized Map<String, Object> findUserByUsername(String username) { // 根据用户名查找用户
        for (Map<String, Object> user : users) { // 遍历用户列表
            if (user.get("username") != null && user.get("username").equals(username)) { // 检查用户名是否匹配
                return user; // 返回找到的用户
            }
        }
        return null; // 没有找到返回null
    }

    /**
     * 更新用户信息
     */
    public synchronized void updateUser(Map<String, Object> updatedUser) { // 更新用户信息
        for (int i = 0; i < users.size(); i++) { // 遍历用户列表
            Map<String, Object> user = users.get(i); // 获取当前用户
            Object idObj = user.get("id"); // 获取当前用户的ID
            Object updatedIdObj = updatedUser.get("id"); // 获取更新后用户的ID

            if (idObj != null && updatedIdObj != null) { // 检查两个ID都不为空
                int userId, updatedUserId; // 定义变量存储ID值

                if (idObj instanceof Integer) userId = (Integer) idObj; // 处理当前用户ID
                else if (idObj instanceof Double) userId = ((Double) idObj).intValue(); // 处理Double类型ID
                else continue; // 类型不匹配跳过

                if (updatedIdObj instanceof Integer) updatedUserId = (Integer) updatedIdObj; // 处理更新用户ID
                else if (updatedIdObj instanceof Double) updatedUserId = ((Double) updatedIdObj).intValue(); // 处理Double类型ID
                else continue; // 类型不匹配跳过

                if (userId == updatedUserId) { // 检查ID是否匹配
                    users.set(i, updatedUser); // 更新用户信息
                    saveAll(); // 保存到Excel文件
                    return; // 更新完成返回
                }
            }
        }
    }

    // 彩票相关操作
    public synchronized void addTicket(Map<String, Object> ticket) { // 添加彩票
        tickets.add(ticket); // 添加到彩票缓存列表
        saveAll(); // 保存到Excel文件
    }

    public synchronized List<Map<String, Object>> getTicketsByUserId(int userId) { // 根据用户ID获取彩票列表
        List<Map<String, Object>> userTickets = new ArrayList<>(); // 创建列表存储用户彩票
        for (Map<String, Object> ticket : tickets) { // 遍历彩票列表
            Object userIdObj = ticket.get("userId"); // 获取彩票的用户ID
            if (userIdObj != null) { // 检查用户ID不为空
                int ticketUserId; // 定义变量存储用户ID
                if (userIdObj instanceof Integer) { // 检查是否为Integer类型
                    ticketUserId = (Integer) userIdObj; // 强制转换
                } else if (userIdObj instanceof Double) { // 检查是否为Double类型
                    ticketUserId = ((Double) userIdObj).intValue(); // 转换为Integer
                } else {
                    continue; // 类型不匹配跳过
                }

                if (ticketUserId == userId) { // 检查用户ID是否匹配
                    userTickets.add(ticket); // 添加到用户彩票列表
                }
            }
        }
        return userTickets; // 返回用户彩票列表
    }

    public synchronized List<Map<String, Object>> getAllTickets() { // 获取所有彩票
        return new ArrayList<>(tickets); // 返回彩票列表的副本
    }

    // 开奖结果相关操作
    public synchronized void addResult(Map<String, Object> result) { // 添加开奖结果
        results.add(result); // 添加到开奖结果缓存列表
        saveAll(); // 保存到Excel文件
    }

    public synchronized List<Map<String, Object>> getAllResults() { // 获取所有开奖结果
        return new ArrayList<>(results); // 返回开奖结果列表的副本
    }

    // 新增：中奖记录相关操作
    public synchronized void addWinning(Map<String, Object> winning) { // 添加中奖记录
        winnings.add(winning); // 添加到中奖记录缓存列表
        saveAll(); // 保存到Excel文件
    }

    public synchronized List<Map<String, Object>> getAllWinnings() { // 获取所有中奖记录
        return new ArrayList<>(winnings); // 返回中奖记录列表的副本
    }

    public synchronized List<Map<String, Object>> getWinningsByUserId(int userId) { // 根据用户ID获取中奖记录
        List<Map<String, Object>> userWinnings = new ArrayList<>(); // 创建列表存储用户中奖记录
        for (Map<String, Object> winning : winnings) { // 遍历中奖记录列表
            Object userIdObj = winning.get("userId"); // 获取中奖记录的用户ID
            if (userIdObj != null) { // 检查用户ID不为空
                int winningUserId; // 定义变量存储用户ID
                if (userIdObj instanceof Integer) { // 检查是否为Integer类型
                    winningUserId = (Integer) userIdObj; // 强制转换
                } else if (userIdObj instanceof Double) { // 检查是否为Double类型
                    winningUserId = ((Double) userIdObj).intValue(); // 转换为Integer
                } else {
                    continue; // 类型不匹配跳过
                }

                if (winningUserId == userId) { // 检查用户ID是否匹配
                    userWinnings.add(winning); // 添加到用户中奖记录列表
                }
            }
        }
        return userWinnings; // 返回用户中奖记录列表
    }

    public synchronized List<Map<String, Object>> getUnreadWinningsByUserId(int userId) { // 获取用户未读中奖通知
        List<Map<String, Object>> unreadWinnings = new ArrayList<>(); // 创建列表存储未读中奖记录
        for (Map<String, Object> winning : winnings) { // 遍历中奖记录列表
            Object userIdObj = winning.get("userId"); // 获取用户ID
            Object isNotifiedObj = winning.get("isNotified"); // 获取通知状态

            if (userIdObj != null && isNotifiedObj != null) { // 检查两个字段都不为空
                int winningUserId; // 定义变量存储用户ID
                boolean isNotified; // 定义变量存储通知状态

                if (userIdObj instanceof Integer) { // 处理用户ID
                    winningUserId = (Integer) userIdObj; // 强制转换
                } else if (userIdObj instanceof Double) { // 处理Double类型
                    winningUserId = ((Double) userIdObj).intValue(); // 转换为Integer
                } else {
                    continue; // 类型不匹配跳过
                }

                if (isNotifiedObj instanceof Boolean) { // 处理通知状态
                    isNotified = (Boolean) isNotifiedObj; // 强制转换
                } else if (isNotifiedObj instanceof String) { // 处理String类型
                    isNotified = Boolean.parseBoolean((String) isNotifiedObj); // 转换为Boolean
                } else {
                    continue; // 类型不匹配跳过
                }

                if (winningUserId == userId && !isNotified) { // 检查用户ID匹配且未通知
                    unreadWinnings.add(winning); // 添加到未读中奖记录列表
                }
            }
        }
        return unreadWinnings; // 返回未读中奖记录列表
    }

    public synchronized void markWinningsAsRead(int userId) { // 标记用户中奖记录为已读
        for (Map<String, Object> winning : winnings) { // 遍历中奖记录列表
            Object userIdObj = winning.get("userId"); // 获取用户ID
            if (userIdObj != null) { // 检查用户ID不为空
                int winningUserId; // 定义变量存储用户ID
                if (userIdObj instanceof Integer) { // 处理Integer类型
                    winningUserId = (Integer) userIdObj; // 强制转换
                } else if (userIdObj instanceof Double) { // 处理Double类型
                    winningUserId = ((Double) userIdObj).intValue(); // 转换为Integer
                } else {
                    continue; // 类型不匹配跳过
                }

                if (winningUserId == userId) { // 检查用户ID是否匹配
                    winning.put("isNotified", true); // 标记为已通知
                }
            }
        }
        saveAll(); // 保存到Excel文件
    }

    // 获取统计数据
    public int getUserCount() { // 获取用户数量
        return users.size(); // 返回用户列表大小
    }

    public int getTicketCount() { // 获取彩票数量
        return tickets.size(); // 返回彩票列表大小
    }

    public int getResultCount() { // 获取开奖结果数量
        return results.size(); // 返回开奖结果列表大小
    }

    public int getWinningCount() { // 新增：获取中奖记录数量
        return winnings.size(); // 返回中奖记录列表大小
    }
}