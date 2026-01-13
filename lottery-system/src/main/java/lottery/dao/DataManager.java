package lottery.dao;

import lottery.model.User;
import lottery.model.Ticket;
import lottery.model.LotteryResult;
import lottery.util.PathManager;

import java.io.File;
import java.util.*;

public class DataManager {
    // 单例实例
    private static DataManager instance;

    // ExcelDao实例
    private ExcelDao excelDao;

    // 内存数据缓存
    private List<User> userCache;
    private List<Ticket> ticketCache;
    private List<LotteryResult> resultCache;

    /**
     * 私有构造方法
     */
    // 在DataManager.java的构造方法中添加路径调试信息
    private DataManager() {
        excelDao = new ExcelDao();
        excelDao.setDebugMode(true); // 调试模式，生产环境设为false

        userCache = new ArrayList<>();
        ticketCache = new ArrayList<>();
        resultCache = new ArrayList<>();

        // 打印详细的路径信息
        System.out.println("\n[INFO] ========== 初始化DataManager ==========");
        PathManager.printPathInfo(); // 打印详细的路径信息

        // 检查数据目录
        String dataDir = PathManager.getDataDir();
        System.out.println("[INFO] 数据目录: " + dataDir);

        // 尝试创建必要的目录
        File dataDirFile = new File(dataDir);
        if (!dataDirFile.exists()) {
            System.out.println("[INFO] 创建数据目录...");
            boolean created = dataDirFile.mkdirs();
            if (created) {
                System.out.println("[INFO] 数据目录创建成功: " + dataDir);
            } else {
                System.err.println("[ERROR] 无法创建数据目录: " + dataDir);
                System.out.println("[INFO] 尝试在当前目录工作...");
            }
        }

        // 确保Excel文件存在
        try {
            System.out.println("[INFO] 检查Excel文件...");
            excelDao.createExcelFiles();
            System.out.println("[INFO] Excel文件检查完成");
        } catch (Exception e) {
            System.err.println("[ERROR] 创建Excel文件失败: " + e.getMessage());
            System.out.println("[INFO] 继续运行，使用空数据集...");
        }

        // 加载数据
        System.out.println("[INFO] 加载数据...");
        try {
            loadAllData();
            System.out.println("[INFO] 数据加载完成");
        } catch (Exception e) {
            System.err.println("[ERROR] 加载数据失败: " + e.getMessage());
            e.printStackTrace();
            System.out.println("[INFO] 使用空数据集继续...");
        }

        System.out.println("[INFO] DataManager初始化完成\n");
    }

    /**
     * 获取单例实例
     */
    public static synchronized DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    /**
     * 加载所有Excel数据到内存
     */
    public void loadAllData() {
        try {
            userCache = excelDao.loadUsers();
            ticketCache = excelDao.loadTickets();
            resultCache = excelDao.loadResults();
            System.out.println("数据加载完成：用户" + userCache.size() + "条，彩票" + ticketCache.size() + "条，结果" + resultCache.size() + "条");
        } catch (Exception e) {
            System.err.println("加载数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 保存所有内存数据到Excel
     */
    public void saveAllData() {
        try {
            excelDao.saveUsers(userCache);
            excelDao.saveTickets(ticketCache);
            excelDao.saveResults(resultCache);
            System.out.println("数据保存完成");
        } catch (Exception e) {
            System.err.println("保存数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ============ 用户数据访问方法 ============

    /**
     * 根据ID获取用户
     */
    public User getUserById(int id) {
        for (User user : userCache) {
            if (user.getId() == id) {
                return user;
            }
        }
        return null;
    }

    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(userCache);
    }

    /**
     * 添加用户
     */
    public void addUser(User user) {
        // 分配ID
        if (user.getId() == 0) {
            user.setId(excelDao.getNextId("users"));
        }

        userCache.add(user);
        excelDao.addUser(user);
    }

    /**
     * 更新用户信息
     */
    public void updateUser(User updatedUser) {
        for (int i = 0; i < userCache.size(); i++) {
            if (userCache.get(i).getId() == updatedUser.getId()) {
                userCache.set(i, updatedUser);
                excelDao.saveUsers(userCache);
                return;
            }
        }
    }

    // ============ 彩票数据访问方法 ============

    /**
     * 根据ID获取彩票
     */
    public Ticket getTicketById(int id) {
        for (Ticket ticket : ticketCache) {
            if (ticket.getId() == id) {
                return ticket;
            }
        }
        return null;
    }

    /**
     * 根据用户ID获取彩票列表
     */
    public List<Ticket> getTicketsByUserId(int userId) {
        List<Ticket> userTickets = new ArrayList<>();
        for (Ticket ticket : ticketCache) {
            if (ticket.getUserId() == userId) {
                userTickets.add(ticket);
            }
        }
        return userTickets;
    }

    /**
     * 获取所有彩票
     */
    public List<Ticket> getAllTickets() {
        return new ArrayList<>(ticketCache);
    }

    /**
     * 添加彩票
     */
    public void addTicket(Ticket ticket) {
        // 分配ID
        if (ticket.getId() == 0) {
            ticket.setId(excelDao.getNextId("tickets"));
        }

        ticketCache.add(ticket);
        excelDao.addTicket(ticket);
    }

    // ============ 结果数据访问方法 ============

    /**
     * 获取最新的抽奖结果
     */
    public LotteryResult getLatestResult() {
        if (resultCache.isEmpty()) {
            return null;
        }

        // 假设结果按ID递增，最新的是最后一个
        return resultCache.get(resultCache.size() - 1);
    }

    /**
     * 获取所有结果
     */
    public List<LotteryResult> getAllResults() {
        return new ArrayList<>(resultCache);
    }

    /**
     * 添加抽奖结果
     */
    public void addResult(LotteryResult result) {
        // 分配ID
        if (result.getId() == 0) {
            result.setId(excelDao.getNextId("results"));
        }

        resultCache.add(result);
        excelDao.addResult(result);
    }

    // ============ 其他辅助方法 ============

    /**
     * 获取用户缓存（仅内部使用）
     */
    public List<User> getUserCache() {
        return userCache;
    }

    /**
     * 获取彩票缓存（仅内部使用）
     */
    public List<Ticket> getTicketCache() {
        return ticketCache;
    }

    /**
     * 获取结果缓存（仅内部使用）
     */
    public List<LotteryResult> getResultCache() {
        return resultCache;
    }
}