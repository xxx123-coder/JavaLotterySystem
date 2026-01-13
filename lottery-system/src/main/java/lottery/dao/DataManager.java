package lottery.dao;

import lottery.model.User;
import lottery.model.Ticket;
import lottery.model.LotteryResult;
import lottery.util.PathManager;
import lottery.util.Logger;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.text.SimpleDateFormat;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DataManager {
    // 单例实例
    private static volatile DataManager instance;

    // ExcelDao实例
    private ExcelDao excelDao;

    // 内存数据缓存 - 使用线程安全的集合
    private CopyOnWriteArrayList<User> userCache;
    private CopyOnWriteArrayList<Ticket> ticketCache;
    private CopyOnWriteArrayList<LotteryResult> resultCache;

    // 快速查找的映射（用户ID -> 用户对象）
    private ConcurrentHashMap<Integer, User> userMap;
    private ConcurrentHashMap<Integer, Ticket> ticketMap;
    private ConcurrentHashMap<Integer, LotteryResult> resultMap;

    // 用户ID到彩票列表的映射
    private ConcurrentHashMap<Integer, CopyOnWriteArrayList<Ticket>> userTicketsMap;

    // 读写锁 - 确保数据一致性
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // 定时任务执行器
    private ScheduledExecutorService scheduler;
    private final int AUTO_SAVE_INTERVAL = 300; // 5分钟自动保存

    // 初始化状态
    private volatile boolean initialized = false;

    // 备份管理器
    private BackupManager backupManager;

    // 日志记录器
    private static final Logger logger = Logger.getLogger(DataManager.class);

    /**
     * 私有构造方法
     */
    private DataManager() {
        initialize();
    }

    /**
     * 初始化方法
     */
    private void initialize() {
        lock.writeLock().lock();
        try {
            if (initialized) {
                return;
            }

            excelDao = new ExcelDao();
            excelDao.setDebugMode(true);

            // 初始化线程安全集合
            userCache = new CopyOnWriteArrayList<>();
            ticketCache = new CopyOnWriteArrayList<>();
            resultCache = new CopyOnWriteArrayList<>();

            userMap = new ConcurrentHashMap<>();
            ticketMap = new ConcurrentHashMap<>();
            resultMap = new ConcurrentHashMap<>();
            userTicketsMap = new ConcurrentHashMap<>();

            // 初始化备份管理器
            backupManager = new BackupManager();

            logger.info("========== 初始化DataManager ==========");
            PathManager.printPathInfo();

            // 检查数据目录
            String dataDir = PathManager.getDataDir();
            logger.info("数据目录: " + dataDir);

            // 尝试创建必要的目录
            File dataDirFile = new File(dataDir);
            if (!dataDirFile.exists()) {
                logger.info("创建数据目录...");
                boolean created = dataDirFile.mkdirs();
                if (created) {
                    logger.info("数据目录创建成功: " + dataDir);
                } else {
                    logger.error("无法创建数据目录: " + dataDir);
                    logger.info("尝试在当前目录工作...");
                }
            }

            // 确保Excel文件存在
            try {
                logger.info("检查Excel文件...");
                excelDao.createExcelFiles();
                logger.info("Excel文件检查完成");

                // 创建备份文件
                backupManager.createBackupFiles();

            } catch (Exception e) {
                logger.error("创建Excel文件失败: " + e.getMessage());
                logger.info("继续运行，使用空数据集...");
            }

            // 加载数据
            logger.info("加载数据...");
            try {
                loadAllData();
                logger.info("数据加载完成");

                // 初始化快速查找映射
                rebuildIndexes();

            } catch (Exception e) {
                logger.error("加载数据失败: " + e.getMessage());
                logger.info("尝试从备份恢复...");
                try {
                    recoverFromBackup();
                } catch (Exception ex) {
                    logger.error("备份恢复失败: " + ex.getMessage());
                    logger.info("使用空数据集继续...");
                }
            }

            // 启动定时保存任务
            startAutoSaveTask();

            // 启动定时备份任务
            startAutoBackupTask();

            initialized = true;
            logger.info("DataManager初始化完成");

        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 启动自动保存任务
     */
    private void startAutoSaveTask() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "DataManager-AutoSave");
            thread.setDaemon(true);
            return thread;
        });

        scheduler.scheduleAtFixedRate(() -> {
            try {
                logger.info("执行定时自动保存...");
                saveAllData();
                logger.info("定时自动保存完成");
            } catch (Exception e) {
                logger.error("定时自动保存失败: " + e.getMessage());
            }
        }, AUTO_SAVE_INTERVAL, AUTO_SAVE_INTERVAL, TimeUnit.SECONDS);

        logger.info("定时自动保存任务已启动，间隔: " + AUTO_SAVE_INTERVAL + "秒");
    }

    /**
     * 启动自动备份任务
     */
    private void startAutoBackupTask() {
        // 每天凌晨2点执行备份
        long initialDelay = calculateInitialDelay();
        long period = TimeUnit.DAYS.toSeconds(1);

        scheduler.scheduleAtFixedRate(() -> {
            try {
                logger.info("执行每日数据备份...");
                backupManager.backupAllData();
                logger.info("每日数据备份完成");
            } catch (Exception e) {
                logger.error("数据备份失败: " + e.getMessage());
            }
        }, initialDelay, period, TimeUnit.SECONDS);
    }

    /**
     * 计算到凌晨2点的初始延迟
     */
    private long calculateInitialDelay() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 2);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long delay = calendar.getTimeInMillis() - System.currentTimeMillis();
        return TimeUnit.MILLISECONDS.toSeconds(delay);
    }

    /**
     * 获取单例实例（双重检查锁）
     */
    public static DataManager getInstance() {
        if (instance == null) {
            synchronized (DataManager.class) {
                if (instance == null) {
                    instance = new DataManager();
                }
            }
        }
        return instance;
    }

    /**
     * 加载所有Excel数据到内存
     */
    public void loadAllData() {
        lock.writeLock().lock();
        try {
            // 先备份当前数据
            backupManager.createBackupBeforeLoad();

            List<User> users = excelDao.loadUsers();
            List<Ticket> tickets = excelDao.loadTickets();
            List<LotteryResult> results = excelDao.loadResults();

            userCache.clear();
            userCache.addAll(users);

            ticketCache.clear();
            ticketCache.addAll(tickets);

            resultCache.clear();
            resultCache.addAll(results);

            // 重建索引
            rebuildIndexes();

            logger.info("数据加载完成：用户" + userCache.size() + "条，彩票" + ticketCache.size() + "条，结果" + resultCache.size() + "条");

        } catch (Exception e) {
            logger.error("加载数据失败: " + e.getMessage());

            // 尝试从备份恢复
            try {
                recoverFromBackup();
                logger.info("已从备份恢复数据");
            } catch (Exception ex) {
                logger.error("备份恢复失败: " + ex.getMessage());
                throw new RuntimeException("加载数据失败且无法从备份恢复", e);
            }

        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 保存所有内存数据到Excel
     */
    public void saveAllData() {
        lock.readLock().lock(); // 使用读锁，允许多线程同时读取
        try {
            logger.info("开始保存数据...");

            // 创建备份
            backupManager.createBackupBeforeSave();

            // 保存到Excel
            excelDao.saveUsers(new ArrayList<>(userCache));
            excelDao.saveTickets(new ArrayList<>(ticketCache));
            excelDao.saveResults(new ArrayList<>(resultCache));

            logger.info("数据保存完成");

        } catch (Exception e) {
            logger.error("保存数据失败: " + e.getMessage());

            // 尝试从备份恢复
            try {
                logger.info("尝试从备份恢复...");
                recoverFromBackup();
            } catch (Exception ex) {
                logger.error("备份恢复失败: " + ex.getMessage());
            }

            throw new RuntimeException("保存数据失败，已尝试备份恢复", e);

        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 从备份恢复数据
     */
    public void recoverFromBackup() {
        lock.writeLock().lock();
        try {
            logger.info("从备份恢复数据...");

            // 从备份管理器恢复
            backupManager.recoverFromBackup();

            // 重新加载数据
            List<User> users = excelDao.loadUsers();
            List<Ticket> tickets = excelDao.loadTickets();
            List<LotteryResult> results = excelDao.loadResults();

            userCache.clear();
            userCache.addAll(users);

            ticketCache.clear();
            ticketCache.addAll(tickets);

            resultCache.clear();
            resultCache.addAll(results);

            rebuildIndexes();

            logger.info("数据恢复完成：用户" + users.size() + "条，彩票" + tickets.size() + "条，结果" + results.size() + "条");

        } catch (Exception e) {
            logger.error("数据恢复失败: " + e.getMessage());
            throw new RuntimeException("数据恢复失败", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 重建索引
     */
    private void rebuildIndexes() {
        userMap.clear();
        ticketMap.clear();
        resultMap.clear();
        userTicketsMap.clear();

        for (User user : userCache) {
            userMap.put(user.getId(), user);
        }

        for (Ticket ticket : ticketCache) {
            ticketMap.put(ticket.getId(), ticket);

            // 构建用户-彩票映射
            int userId = ticket.getUserId();
            userTicketsMap.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>())
                    .add(ticket);
        }

        for (LotteryResult result : resultCache) {
            resultMap.put(result.getId(), result);
        }
    }

    // ============ 用户数据访问方法 ============

    /**
     * 根据ID获取用户
     */
    public User getUserById(int id) {
        lock.readLock().lock();
        try {
            return userMap.get(id);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(userCache);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 添加用户（线程安全）
     */
    public void addUser(User user) {
        lock.writeLock().lock();
        try {
            // 分配ID
            if (user.getId() == 0) {
                user.setId(excelDao.getNextId("users"));
            }

            // 检查用户是否存在
            if (userMap.containsKey(user.getId())) {
                throw new IllegalArgumentException("用户ID已存在: " + user.getId());
            }

            userCache.add(user);
            userMap.put(user.getId(), user);
            excelDao.addUser(user);

        } catch (Exception e) {
            logger.error("添加用户失败: " + e.getMessage());
            throw new RuntimeException("添加用户失败", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 更新用户信息（线程安全）
     */
    public void updateUser(User updatedUser) {
        lock.writeLock().lock();
        try {
            int userId = updatedUser.getId();

            // 查找并更新
            boolean found = false;
            for (int i = 0; i < userCache.size(); i++) {
                if (userCache.get(i).getId() == userId) {
                    userCache.set(i, updatedUser);
                    userMap.put(userId, updatedUser);
                    found = true;
                    break;
                }
            }

            if (!found) {
                throw new IllegalArgumentException("用户不存在: " + userId);
            }

            // 保存到Excel
            excelDao.saveUsers(new ArrayList<>(userCache));

        } catch (Exception e) {
            logger.error("更新用户失败: " + e.getMessage());
            throw new RuntimeException("更新用户失败", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    // ============ 彩票数据访问方法 ============

    /**
     * 根据ID获取彩票
     */
    public Ticket getTicketById(int id) {
        lock.readLock().lock();
        try {
            return ticketMap.get(id);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 根据用户ID获取彩票列表
     */
    public List<Ticket> getTicketsByUserId(int userId) {
        lock.readLock().lock();
        try {
            CopyOnWriteArrayList<Ticket> tickets = userTicketsMap.get(userId);
            if (tickets == null) {
                return new ArrayList<>();
            }
            return new ArrayList<>(tickets);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 获取所有彩票
     */
    public List<Ticket> getAllTickets() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(ticketCache);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 添加彩票（线程安全）
     */
    public void addTicket(Ticket ticket) {
        lock.writeLock().lock();
        try {
            // 分配ID
            if (ticket.getId() == 0) {
                ticket.setId(excelDao.getNextId("tickets"));
            }

            // 检查彩票是否存在
            if (ticketMap.containsKey(ticket.getId())) {
                throw new IllegalArgumentException("彩票ID已存在: " + ticket.getId());
            }

            ticketCache.add(ticket);
            ticketMap.put(ticket.getId(), ticket);

            // 更新用户-彩票映射
            int userId = ticket.getUserId();
            userTicketsMap.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>())
                    .add(ticket);

            excelDao.addTicket(ticket);

        } catch (Exception e) {
            logger.error("添加彩票失败: " + e.getMessage());
            throw new RuntimeException("添加彩票失败", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    // ============ 结果数据访问方法 ============

    /**
     * 获取最新的抽奖结果
     */
    public LotteryResult getLatestResult() {
        lock.readLock().lock();
        try {
            if (resultCache.isEmpty()) {
                return null;
            }
            return resultCache.get(resultCache.size() - 1);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 获取所有结果
     */
    public List<LotteryResult> getAllResults() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(resultCache);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 添加抽奖结果（线程安全）
     */
    public void addResult(LotteryResult result) {
        lock.writeLock().lock();
        try {
            // 分配ID
            if (result.getId() == 0) {
                result.setId(excelDao.getNextId("results"));
            }

            // 检查结果是否存在
            if (resultMap.containsKey(result.getId())) {
                throw new IllegalArgumentException("结果ID已存在: " + result.getId());
            }

            resultCache.add(result);
            resultMap.put(result.getId(), result);
            excelDao.addResult(result);

        } catch (Exception e) {
            logger.error("添加抽奖结果失败: " + e.getMessage());
            throw new RuntimeException("添加抽奖结果失败", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    // ============ 其他辅助方法 ============

    /**
     * 获取用户数量
     */
    public int getUserCount() {
        lock.readLock().lock();
        try {
            return userCache.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 获取彩票数量
     */
    public int getTicketCount() {
        lock.readLock().lock();
        try {
            return ticketCache.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 获取结果数量
     */
    public int getResultCount() {
        lock.readLock().lock();
        try {
            return resultCache.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 检查是否已初始化
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * 关闭DataManager，释放资源
     */
    public void shutdown() {
        lock.writeLock().lock();
        try {
            logger.info("关闭DataManager...");

            // 保存数据
            saveAllData();

            // 关闭定时任务
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdown();
                try {
                    if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                        scheduler.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    scheduler.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }

            logger.info("DataManager已关闭");

        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 执行强制保存
     */
    public void forceSave() {
        logger.info("执行强制保存...");
        saveAllData();
        logger.info("强制保存完成");
    }

    /**
     * 执行手动备份
     */
    public void manualBackup() {
        try {
            logger.info("执行手动备份...");
            backupManager.backupAllData();
            logger.info("手动备份完成");
        } catch (Exception e) {
            logger.error("手动备份失败: " + e.getMessage());
        }
    }
}

/**
 * 备份管理器内部类
 */
class BackupManager {
    private static final String BACKUP_DIR = "backup";
    private static final String DAILY_BACKUP_DIR = "daily";

    private final Logger logger = Logger.getLogger(BackupManager.class);

    /**
     * 创建备份文件
     */
    public void createBackupFiles() throws Exception {
        String projectRoot = PathManager.getProjectRoot();
        String backupPath = projectRoot + File.separator + BACKUP_DIR;
        String dailyBackupPath = backupPath + File.separator + DAILY_BACKUP_DIR;

        File backupDir = new File(backupPath);
        if (!backupDir.exists()) {
            boolean created = backupDir.mkdirs();
            if (!created) {
                logger.warn("创建备份目录失败: " + backupPath);
            }
        }

        File dailyBackupDir = new File(dailyBackupPath);
        if (!dailyBackupDir.exists()) {
            boolean created = dailyBackupDir.mkdirs();
            if (!created) {
                logger.warn("创建每日备份目录失败: " + dailyBackupPath);
            }
        }

        logger.info("备份目录已创建: " + backupPath);
    }

    /**
     * 在加载前备份当前数据
     */
    public void createBackupBeforeLoad() {
        // 在加载前备份当前数据文件
        try {
            String timestamp = String.valueOf(System.currentTimeMillis());
            backupFile(PathManager.getUserFilePath(),
                    PathManager.getBackupDir() + File.separator +
                            "users_backup_preload_" + timestamp + ".xlsx");
            backupFile(PathManager.getTicketFilePath(),
                    PathManager.getBackupDir() + File.separator +
                            "tickets_backup_preload_" + timestamp + ".xlsx");
            backupFile(PathManager.getResultFilePath(),
                    PathManager.getBackupDir() + File.separator +
                            "results_backup_preload_" + timestamp + ".xlsx");

        } catch (Exception e) {
            logger.warn("创建加载前备份失败: " + e.getMessage());
        }
    }

    /**
     * 在保存前创建备份
     */
    public void createBackupBeforeSave() {
        // 在保存前创建备份
        try {
            String timestamp = String.valueOf(System.currentTimeMillis());
            String backupDir = PathManager.getBackupDir() + File.separator + "auto_" + timestamp;

            File dir = new File(backupDir);
            boolean created = dir.mkdirs();
            if (!created) {
                logger.warn("创建备份目录失败: " + backupDir);
                return;
            }

            // 备份源文件
            backupFile(PathManager.getUserFilePath(), backupDir + File.separator + "users.xlsx");
            backupFile(PathManager.getTicketFilePath(), backupDir + File.separator + "tickets.xlsx");
            backupFile(PathManager.getResultFilePath(), backupDir + File.separator + "results.xlsx");

            logger.info("创建保存前备份: " + backupDir);

        } catch (Exception e) {
            logger.warn("创建保存前备份失败: " + e.getMessage());
        }
    }

    /**
     * 备份所有数据
     */
    public void backupAllData() throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = dateFormat.format(new Date());
        String backupDir = PathManager.getBackupDir() + File.separator + DAILY_BACKUP_DIR +
                File.separator + "backup_" + timestamp;

        File dir = new File(backupDir);
        boolean created = dir.mkdirs();
        if (!created) {
            throw new Exception("创建备份目录失败: " + backupDir);
        }

        // 备份数据文件
        backupFile(PathManager.getUserFilePath(), backupDir + File.separator + "users.xlsx");
        backupFile(PathManager.getTicketFilePath(), backupDir + File.separator + "tickets.xlsx");
        backupFile(PathManager.getResultFilePath(), backupDir + File.separator + "results.xlsx");

        logger.info("数据已备份到: " + backupDir);
    }

    /**
     * 从备份恢复
     */
    public void recoverFromBackup() throws Exception {
        logger.info("从备份恢复数据...");

        String latestBackupDir = findLatestBackupDir();
        if (latestBackupDir == null) {
            throw new Exception("未找到备份文件");
        }

        // 恢复数据文件
        restoreFile(latestBackupDir + File.separator + "users.xlsx", PathManager.getUserFilePath());
        restoreFile(latestBackupDir + File.separator + "tickets.xlsx", PathManager.getTicketFilePath());
        restoreFile(latestBackupDir + File.separator + "results.xlsx", PathManager.getResultFilePath());

        logger.info("数据已从备份恢复: " + latestBackupDir);
    }

    /**
     * 备份文件
     */
    private void backupFile(String sourcePath, String targetPath) throws Exception {
        File source = new File(sourcePath);
        if (source.exists()) {
            Path sourcePathObj = source.toPath();
            Path targetPathObj = new File(targetPath).toPath();
            Files.copy(sourcePathObj, targetPathObj, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * 恢复文件
     */
    private void restoreFile(String sourcePath, String targetPath) throws Exception {
        File source = new File(sourcePath);
        if (source.exists()) {
            Path sourcePathObj = source.toPath();
            Path targetPathObj = new File(targetPath).toPath();
            Files.copy(sourcePathObj, targetPathObj, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * 查找最新的备份目录
     */
    private String findLatestBackupDir() {
        String backupBaseDir = PathManager.getBackupDir() + File.separator + DAILY_BACKUP_DIR;
        File baseDir = new File(backupBaseDir);

        if (!baseDir.exists()) {
            return null;
        }

        File[] backupDirs = baseDir.listFiles((dir, name) -> name.startsWith("backup_"));
        if (backupDirs == null || backupDirs.length == 0) {
            return null;
        }

        // 按修改时间排序，返回最新的
        Arrays.sort(backupDirs, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
        return backupDirs[0].getAbsolutePath();
    }
}