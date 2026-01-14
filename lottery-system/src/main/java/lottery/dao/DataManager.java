package lottery.dao;

import java.util.*;

/**
 * 数据管理器（单例模式）
 */
public class DataManager {
    private static DataManager instance;
    private ExcelDao excelDao;
    private boolean initialized = false;

    // 数据缓存
    private List<Map<String, Object>> users = new ArrayList<>();
    private List<Map<String, Object>> tickets = new ArrayList<>();
    private List<Map<String, Object>> results = new ArrayList<>();
    private List<Map<String, Object>> winnings = new ArrayList<>(); // 新增：中奖记录缓存

    private DataManager() {
        excelDao = new ExcelDao();
    }

    public static synchronized DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
            instance.initialize();
        }
        return instance;
    }

    /**
     * 初始化数据
     */
    private void initialize() {
        System.out.println("正在初始化数据管理器...");

        try {
            users = excelDao.loadUsers();
            tickets = excelDao.loadTickets();
            results = excelDao.loadResults();
            winnings = excelDao.loadWinnings(); // 新增：加载中奖记录

            // 如果没有用户，创建默认用户
            if (users.isEmpty()) {
                System.out.println("创建默认用户...");
                createDefaultUser();
            }

            initialized = true;
            System.out.println("数据加载完成:");
            System.out.println("  用户数量: " + users.size());
            System.out.println("  彩票数量: " + tickets.size());
            System.out.println("  开奖结果: " + results.size());
            System.out.println("  中奖记录: " + winnings.size()); // 新增

        } catch (Exception e) {
            System.err.println("数据初始化失败: " + e.getMessage());
            // 即使初始化失败，也创建空数据列表
            users = new ArrayList<>();
            tickets = new ArrayList<>();
            results = new ArrayList<>();
            winnings = new ArrayList<>(); // 新增

            // 创建默认用户
            createDefaultUser();

            initialized = true;
            System.out.println("已使用默认数据");
        }
    }

    /**
     * 创建默认用户
     */
    private void createDefaultUser() {
        try {
            Map<String, Object> defaultUser = new HashMap<>();
            defaultUser.put("id", 1);
            defaultUser.put("username", "admin");
            defaultUser.put("password", "123456");
            defaultUser.put("balance", 1000.0);
            defaultUser.put("phone", "13800000000");

            users.add(defaultUser);
            excelDao.saveUsers(users);
            System.out.println("默认用户创建成功: admin/123456");
        } catch (Exception e) {
            System.err.println("创建默认用户失败: " + e.getMessage());
        }
    }

    /**
     * 保存所有数据
     */
    public synchronized void saveAll() {
        try {
            excelDao.saveUsers(users);
            excelDao.saveTickets(tickets);
            excelDao.saveResults(results);
            excelDao.saveWinnings(winnings); // 新增：保存中奖记录
        } catch (Exception e) {
            System.err.println("保存数据失败: " + e.getMessage());
        }
    }

    /**
     * 关闭系统
     */
    public void shutdown() {
        saveAll();
    }

    /**
     * 检查是否已初始化
     */
    public boolean isInitialized() {
        return initialized;
    }

    // 用户相关操作
    public synchronized void addUser(Map<String, Object> user) {
        users.add(user);
        saveAll();
    }

    public synchronized List<Map<String, Object>> getAllUsers() {
        return new ArrayList<>(users);
    }

    public synchronized Map<String, Object> findUserById(int id) {
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

                if (userId == id) {
                    return user;
                }
            }
        }
        return null;
    }

    public synchronized Map<String, Object> findUserByUsername(String username) {
        for (Map<String, Object> user : users) {
            if (user.get("username") != null && user.get("username").equals(username)) {
                return user;
            }
        }
        return null;
    }

    /**
     * 更新用户信息
     */
    public synchronized void updateUser(Map<String, Object> updatedUser) {
        for (int i = 0; i < users.size(); i++) {
            Map<String, Object> user = users.get(i);
            Object idObj = user.get("id");
            Object updatedIdObj = updatedUser.get("id");

            if (idObj != null && updatedIdObj != null) {
                int userId, updatedUserId;

                if (idObj instanceof Integer) userId = (Integer) idObj;
                else if (idObj instanceof Double) userId = ((Double) idObj).intValue();
                else continue;

                if (updatedIdObj instanceof Integer) updatedUserId = (Integer) updatedIdObj;
                else if (updatedIdObj instanceof Double) updatedUserId = ((Double) updatedIdObj).intValue();
                else continue;

                if (userId == updatedUserId) {
                    users.set(i, updatedUser);
                    saveAll();
                    return;
                }
            }
        }
    }

    // 彩票相关操作
    public synchronized void addTicket(Map<String, Object> ticket) {
        tickets.add(ticket);
        saveAll();
    }

    public synchronized List<Map<String, Object>> getTicketsByUserId(int userId) {
        List<Map<String, Object>> userTickets = new ArrayList<>();
        for (Map<String, Object> ticket : tickets) {
            Object userIdObj = ticket.get("userId");
            if (userIdObj != null) {
                int ticketUserId;
                if (userIdObj instanceof Integer) {
                    ticketUserId = (Integer) userIdObj;
                } else if (userIdObj instanceof Double) {
                    ticketUserId = ((Double) userIdObj).intValue();
                } else {
                    continue;
                }

                if (ticketUserId == userId) {
                    userTickets.add(ticket);
                }
            }
        }
        return userTickets;
    }

    public synchronized List<Map<String, Object>> getAllTickets() {
        return new ArrayList<>(tickets);
    }

    // 开奖结果相关操作
    public synchronized void addResult(Map<String, Object> result) {
        results.add(result);
        saveAll();
    }

    public synchronized List<Map<String, Object>> getAllResults() {
        return new ArrayList<>(results);
    }

    // 新增：中奖记录相关操作
    public synchronized void addWinning(Map<String, Object> winning) {
        winnings.add(winning);
        saveAll();
    }

    public synchronized List<Map<String, Object>> getAllWinnings() {
        return new ArrayList<>(winnings);
    }

    public synchronized List<Map<String, Object>> getWinningsByUserId(int userId) {
        List<Map<String, Object>> userWinnings = new ArrayList<>();
        for (Map<String, Object> winning : winnings) {
            Object userIdObj = winning.get("userId");
            if (userIdObj != null) {
                int winningUserId;
                if (userIdObj instanceof Integer) {
                    winningUserId = (Integer) userIdObj;
                } else if (userIdObj instanceof Double) {
                    winningUserId = ((Double) userIdObj).intValue();
                } else {
                    continue;
                }

                if (winningUserId == userId) {
                    userWinnings.add(winning);
                }
            }
        }
        return userWinnings;
    }

    public synchronized List<Map<String, Object>> getUnreadWinningsByUserId(int userId) {
        List<Map<String, Object>> unreadWinnings = new ArrayList<>();
        for (Map<String, Object> winning : winnings) {
            Object userIdObj = winning.get("userId");
            Object isNotifiedObj = winning.get("isNotified");

            if (userIdObj != null && isNotifiedObj != null) {
                int winningUserId;
                boolean isNotified;

                if (userIdObj instanceof Integer) {
                    winningUserId = (Integer) userIdObj;
                } else if (userIdObj instanceof Double) {
                    winningUserId = ((Double) userIdObj).intValue();
                } else {
                    continue;
                }

                if (isNotifiedObj instanceof Boolean) {
                    isNotified = (Boolean) isNotifiedObj;
                } else if (isNotifiedObj instanceof String) {
                    isNotified = Boolean.parseBoolean((String) isNotifiedObj);
                } else {
                    continue;
                }

                if (winningUserId == userId && !isNotified) {
                    unreadWinnings.add(winning);
                }
            }
        }
        return unreadWinnings;
    }

    public synchronized void markWinningsAsRead(int userId) {
        for (Map<String, Object> winning : winnings) {
            Object userIdObj = winning.get("userId");
            if (userIdObj != null) {
                int winningUserId;
                if (userIdObj instanceof Integer) {
                    winningUserId = (Integer) userIdObj;
                } else if (userIdObj instanceof Double) {
                    winningUserId = ((Double) userIdObj).intValue();
                } else {
                    continue;
                }

                if (winningUserId == userId) {
                    winning.put("isNotified", true);
                }
            }
        }
        saveAll();
    }

    // 获取统计数据
    public int getUserCount() {
        return users.size();
    }

    public int getTicketCount() {
        return tickets.size();
    }

    public int getResultCount() {
        return results.size();
    }

    public int getWinningCount() { // 新增
        return winnings.size();
    }
}