package lottery.dao;

import lottery.model.User;
import lottery.model.Ticket;
import lottery.model.LotteryResult;
import lottery.util.PathManager;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExcelDao {
    // 日期格式化器
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // 调试标志
    private boolean debugMode = false;

    // 最大重试次数
    private static final int MAX_RETRY_COUNT = 3;

    // 备份文件后缀
    private static final String BACKUP_SUFFIX = ".backup";

    /**
     * 设置调试模式
     */
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    /**
     * 调试输出
     */
    private void debug(String message) {
        if (debugMode) {
            System.out.println("[DEBUG-ExcelDao] " + message);
        }
    }

    /**
     * 加载所有用户
     */
    public List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        String filePath = PathManager.getUserFilePath();

        debug("加载用户数据，文件路径: " + filePath);

        try {
            File file = new File(filePath);
            if (!file.exists()) {
                debug("用户文件不存在，将创建新文件");
                createExcelFiles();
                return users;
            }

            // 验证文件完整性
            if (!isExcelFileValid(filePath)) {
                debug("Excel文件可能损坏，尝试恢复...");
                if (!attemptFileRecovery(filePath)) {
                    debug("文件恢复失败，创建新文件");
                    createExcelFiles();
                    return users;
                }
            }

            try (FileInputStream fis = new FileInputStream(filePath);
                 Workbook workbook = new XSSFWorkbook(fis)) {

                // 验证工作簿
                if (workbook.getNumberOfSheets() == 0) {
                    debug("工作簿中没有工作表，创建新文件");
                    createExcelFiles();
                    return users;
                }

                Sheet sheet = workbook.getSheetAt(0);
                if (sheet == null) {
                    debug("工作表不存在，创建新文件");
                    createExcelFiles();
                    return users;
                }

                Iterator<Row> rowIterator = sheet.iterator();

                // 跳过标题行
                if (rowIterator.hasNext()) {
                    rowIterator.next();
                }

                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    User user = new User();

                    try {
                        user.setId((int) row.getCell(0).getNumericCellValue());
                        user.setUsername(getCellStringValue(row.getCell(1)));
                        user.setPassword(getCellStringValue(row.getCell(2)));
                        user.setBalance(getCellNumericValue(row.getCell(3)));
                        user.setPhone(getCellStringValue(row.getCell(4)));

                        users.add(user);
                    } catch (Exception e) {
                        debug("解析用户数据行时出错: " + e.getMessage());
                        // 继续处理其他行
                    }
                }

                debug("成功加载 " + users.size() + " 个用户");
            } catch (Exception e) {
                System.err.println("加载用户数据失败: " + e.getMessage());
                // 尝试备份恢复
                if (restoreFromBackup(filePath)) {
                    return loadUsers(); // 重试加载
                } else {
                    throw e;
                }
            }

        } catch (FileNotFoundException e) {
            debug("用户文件不存在，将创建新文件: " + filePath);
            createExcelFiles();
        } catch (IOException e) {
            System.err.println("加载用户数据失败: " + e.getMessage());
            e.printStackTrace();
        }

        return users;
    }

    /**
     * 保存用户列表到Excel
     */
    public void saveUsers(List<User> users) {
        String filePath = PathManager.getUserFilePath();
        debug("保存用户数据到: " + filePath + " (用户数: " + users.size() + ")");

        // 创建备份
        createBackup(filePath);

        File tempFile = null;
        try {
            // 创建临时文件
            tempFile = new File(filePath + ".tmp");

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Users");

                // 设置默认列宽
                for (int i = 0; i < 5; i++) {
                    sheet.setColumnWidth(i, 4000);
                }

                // 创建标题行
                Row headerRow = sheet.createRow(0);
                String[] columns = {"ID", "Username", "Password", "Balance", "Phone"};
                for (int i = 0; i < columns.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(columns[i]);

                    // 设置标题样式
                    CellStyle headerStyle = workbook.createCellStyle();
                    Font headerFont = workbook.createFont();
                    headerFont.setBold(true);
                    headerStyle.setFont(headerFont);
                    cell.setCellStyle(headerStyle);
                }

                // 填充数据
                for (int i = 0; i < users.size(); i++) {
                    User user = users.get(i);
                    Row row = sheet.createRow(i + 1);

                    row.createCell(0).setCellValue(user.getId());
                    row.createCell(1).setCellValue(user.getUsername());
                    row.createCell(2).setCellValue(user.getPassword());
                    row.createCell(3).setCellValue(user.getBalance());
                    row.createCell(4).setCellValue(user.getPhone());
                }

                // 确保目录存在
                PathManager.ensureDirectoryExists(new File(filePath).getParent());

                // 先写入临时文件
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    workbook.write(fos);
                    fos.flush();
                }

                // 验证临时文件
                if (!isExcelFileValid(tempFile.getAbsolutePath())) {
                    throw new IOException("临时文件验证失败");
                }

                // 关闭工作簿
                workbook.close();
            }

            // 删除原文件
            File originalFile = new File(filePath);
            if (originalFile.exists()) {
                if (!originalFile.delete()) {
                    System.err.println("警告: 无法删除原文件: " + filePath);
                }
            }

            // 重命名临时文件为目标文件
            if (!tempFile.renameTo(originalFile)) {
                // 如果重命名失败，尝试复制
                try (FileInputStream fis = new FileInputStream(tempFile);
                     FileOutputStream fos = new FileOutputStream(originalFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                    fos.flush();
                }
            }

            debug("用户数据保存成功: " + filePath);

        } catch (IOException e) {
            System.err.println("保存用户数据失败: " + e.getMessage());

            // 尝试恢复备份
            restoreFromBackup(filePath);

            e.printStackTrace();
            throw new RuntimeException("保存用户数据失败: " + e.getMessage(), e);
        } finally {
            // 清理临时文件
            if (tempFile != null && tempFile.exists()) {
                if (!tempFile.delete()) {
                    System.err.println("警告: 无法删除临时文件: " + tempFile.getAbsolutePath());
                }
            }
        }
    }

    /**
     * 添加单个用户到Excel
     */
    public void addUser(User user) {
        debug("添加单个用户: " + user.getUsername());
        List<User> users = loadUsers();
        users.add(user);
        saveUsers(users);
    }

    /**
     * 加载所有彩票
     */
    public List<Ticket> loadTickets() {
        List<Ticket> tickets = new ArrayList<>();
        String filePath = PathManager.getTicketFilePath();

        debug("加载彩票数据，文件路径: " + filePath);

        try {
            File file = new File(filePath);
            if (!file.exists()) {
                debug("彩票文件不存在，将创建新文件");
                createExcelFiles();
                return tickets;
            }

            // 验证文件完整性
            if (!isExcelFileValid(filePath)) {
                debug("Excel文件可能损坏，尝试恢复...");
                if (!attemptFileRecovery(filePath)) {
                    debug("文件恢复失败，创建新文件");
                    createExcelFiles();
                    return tickets;
                }
            }

            try (FileInputStream fis = new FileInputStream(filePath);
                 Workbook workbook = new XSSFWorkbook(fis)) {

                // 验证工作簿
                if (workbook.getNumberOfSheets() == 0) {
                    debug("工作簿中没有工作表，创建新文件");
                    createExcelFiles();
                    return tickets;
                }

                Sheet sheet = workbook.getSheetAt(0);
                if (sheet == null) {
                    debug("工作表不存在，创建新文件");
                    createExcelFiles();
                    return tickets;
                }

                Iterator<Row> rowIterator = sheet.iterator();

                // 跳过标题行
                if (rowIterator.hasNext()) {
                    rowIterator.next();
                }

                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    Ticket ticket = new Ticket();

                    try {
                        ticket.setId((int) row.getCell(0).getNumericCellValue());
                        ticket.setUserId((int) row.getCell(1).getNumericCellValue());
                        ticket.setNumbers(getCellStringValue(row.getCell(2)));
                        ticket.setBetCount((int) row.getCell(3).getNumericCellValue());

                        // 解析日期
                        String dateStr = getCellStringValue(row.getCell(4));
                        if (dateStr != null && !dateStr.isEmpty()) {
                            try {
                                ticket.setPurchaseTime(dateFormat.parse(dateStr));
                            } catch (Exception e) {
                                debug("解析购买时间失败: " + dateStr);
                            }
                        }

                        // 解析布尔值
                        String manualStr = getCellStringValue(row.getCell(5));
                        ticket.setManual("true".equalsIgnoreCase(manualStr) || "1".equals(manualStr));

                        tickets.add(ticket);
                    } catch (Exception e) {
                        debug("解析彩票数据行时出错: " + e.getMessage());
                        // 继续处理其他行
                    }
                }

                debug("成功加载 " + tickets.size() + " 张彩票");
            } catch (Exception e) {
                System.err.println("加载彩票数据失败: " + e.getMessage());
                // 尝试备份恢复
                if (restoreFromBackup(filePath)) {
                    return loadTickets(); // 重试加载
                } else {
                    throw e;
                }
            }

        } catch (FileNotFoundException e) {
            debug("彩票文件不存在，将创建新文件: " + filePath);
            createExcelFiles();
        } catch (IOException e) {
            System.err.println("加载彩票数据失败: " + e.getMessage());
            e.printStackTrace();
        }

        return tickets;
    }

    /**
     * 保存彩票列表到Excel
     */
    public void saveTickets(List<Ticket> tickets) {
        String filePath = PathManager.getTicketFilePath();
        debug("保存彩票数据到: " + filePath + " (彩票数: " + tickets.size() + ")");

        // 创建备份
        createBackup(filePath);

        File tempFile = null;
        try {
            // 创建临时文件
            tempFile = new File(filePath + ".tmp");

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Tickets");

                // 设置默认列宽
                for (int i = 0; i < 6; i++) {
                    sheet.setColumnWidth(i, 4000);
                }

                // 创建标题行
                Row headerRow = sheet.createRow(0);
                String[] columns = {"ID", "UserID", "Numbers", "BetCount", "PurchaseTime", "IsManual"};
                for (int i = 0; i < columns.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(columns[i]);

                    // 设置标题样式
                    CellStyle headerStyle = workbook.createCellStyle();
                    Font headerFont = workbook.createFont();
                    headerFont.setBold(true);
                    headerStyle.setFont(headerFont);
                    cell.setCellStyle(headerStyle);
                }

                // 填充数据
                for (int i = 0; i < tickets.size(); i++) {
                    Ticket ticket = tickets.get(i);
                    Row row = sheet.createRow(i + 1);

                    row.createCell(0).setCellValue(ticket.getId());
                    row.createCell(1).setCellValue(ticket.getUserId());
                    row.createCell(2).setCellValue(ticket.getNumbers());
                    row.createCell(3).setCellValue(ticket.getBetCount());

                    // 格式化日期
                    String dateStr = "";
                    if (ticket.getPurchaseTime() != null) {
                        dateStr = dateFormat.format(ticket.getPurchaseTime());
                    }
                    row.createCell(4).setCellValue(dateStr);

                    row.createCell(5).setCellValue(ticket.isManual());
                }

                // 确保目录存在
                PathManager.ensureDirectoryExists(new File(filePath).getParent());

                // 先写入临时文件
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    workbook.write(fos);
                    fos.flush();
                }

                // 验证临时文件
                if (!isExcelFileValid(tempFile.getAbsolutePath())) {
                    throw new IOException("临时文件验证失败");
                }
            }

            // 删除原文件
            File originalFile = new File(filePath);
            if (originalFile.exists()) {
                if (!originalFile.delete()) {
                    System.err.println("警告: 无法删除原文件: " + filePath);
                }
            }

            // 重命名临时文件为目标文件
            if (!tempFile.renameTo(originalFile)) {
                // 如果重命名失败，尝试复制
                try (FileInputStream fis = new FileInputStream(tempFile);
                     FileOutputStream fos = new FileOutputStream(originalFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                    fos.flush();
                }
            }

            debug("彩票数据保存成功: " + filePath);

        } catch (IOException e) {
            System.err.println("保存彩票数据失败: " + e.getMessage());

            // 尝试恢复备份
            restoreFromBackup(filePath);

            e.printStackTrace();
            throw new RuntimeException("保存彩票数据失败: " + e.getMessage(), e);
        } finally {
            // 清理临时文件
            if (tempFile != null && tempFile.exists()) {
                if (!tempFile.delete()) {
                    System.err.println("警告: 无法删除临时文件: " + tempFile.getAbsolutePath());
                }
            }
        }
    }

    /**
     * 添加单个彩票到Excel
     */
    public void addTicket(Ticket ticket) {
        debug("添加单个彩票: ID=" + ticket.getId() + ", 用户ID=" + ticket.getUserId());
        List<Ticket> tickets = loadTickets();
        tickets.add(ticket);
        saveTickets(tickets);
    }

    /**
     * 加载所有抽奖结果
     */
    public List<LotteryResult> loadResults() {
        List<LotteryResult> results = new ArrayList<>();
        String filePath = PathManager.getResultFilePath();

        debug("加载结果数据，文件路径: " + filePath);

        try {
            File file = new File(filePath);
            if (!file.exists()) {
                debug("结果文件不存在，将创建新文件");
                createExcelFiles();
                return results;
            }

            // 验证文件完整性
            if (!isExcelFileValid(filePath)) {
                debug("Excel文件可能损坏，尝试恢复...");
                if (!attemptFileRecovery(filePath)) {
                    debug("文件恢复失败，创建新文件");
                    createExcelFiles();
                    return results;
                }
            }

            try (FileInputStream fis = new FileInputStream(filePath);
                 Workbook workbook = new XSSFWorkbook(fis)) {

                // 验证工作簿
                if (workbook.getNumberOfSheets() == 0) {
                    debug("工作簿中没有工作表，创建新文件");
                    createExcelFiles();
                    return results;
                }

                Sheet sheet = workbook.getSheetAt(0);
                if (sheet == null) {
                    debug("工作表不存在，创建新文件");
                    createExcelFiles();
                    return results;
                }

                Iterator<Row> rowIterator = sheet.iterator();

                // 跳过标题行
                if (rowIterator.hasNext()) {
                    rowIterator.next();
                }

                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    LotteryResult result = new LotteryResult();

                    try {
                        result.setId((int) row.getCell(0).getNumericCellValue());
                        result.setWinningNumbers(getCellStringValue(row.getCell(1)));

                        // 解析日期
                        String dateStr = getCellStringValue(row.getCell(2));
                        if (dateStr != null && !dateStr.isEmpty()) {
                            try {
                                result.setDrawTime(dateFormat.parse(dateStr));
                            } catch (Exception e) {
                                debug("解析抽奖时间失败: " + dateStr);
                            }
                        }

                        result.setWinnerUserId((int) row.getCell(3).getNumericCellValue());
                        result.setPrizeLevel(getCellStringValue(row.getCell(4)));
                        result.setMultiplier((int) row.getCell(5).getNumericCellValue());

                        results.add(result);
                    } catch (Exception e) {
                        debug("解析结果数据行时出错: " + e.getMessage());
                        // 继续处理其他行
                    }
                }

                debug("成功加载 " + results.size() + " 个结果");
            } catch (Exception e) {
                System.err.println("加载抽奖结果失败: " + e.getMessage());
                // 尝试备份恢复
                if (restoreFromBackup(filePath)) {
                    return loadResults(); // 重试加载
                } else {
                    throw e;
                }
            }

        } catch (FileNotFoundException e) {
            debug("结果文件不存在，将创建新文件: " + filePath);
            createExcelFiles();
        } catch (IOException e) {
            System.err.println("加载抽奖结果失败: " + e.getMessage());
            e.printStackTrace();
        }

        return results;
    }

    /**
     * 保存结果列表到Excel
     */
    public void saveResults(List<LotteryResult> results) {
        String filePath = PathManager.getResultFilePath();
        debug("保存结果数据到: " + filePath + " (结果数: " + results.size() + ")");

        // 创建备份
        createBackup(filePath);

        File tempFile = null;
        try {
            // 创建临时文件
            tempFile = new File(filePath + ".tmp");

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Results");

                // 设置默认列宽
                for (int i = 0; i < 6; i++) {
                    sheet.setColumnWidth(i, 4000);
                }

                // 创建标题行
                Row headerRow = sheet.createRow(0);
                String[] columns = {"ID", "WinningNumbers", "DrawTime", "WinnerUserId", "PrizeLevel", "Multiplier"};
                for (int i = 0; i < columns.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(columns[i]);

                    // 设置标题样式
                    CellStyle headerStyle = workbook.createCellStyle();
                    Font headerFont = workbook.createFont();
                    headerFont.setBold(true);
                    headerStyle.setFont(headerFont);
                    cell.setCellStyle(headerStyle);
                }

                // 填充数据
                for (int i = 0; i < results.size(); i++) {
                    LotteryResult result = results.get(i);
                    Row row = sheet.createRow(i + 1);

                    row.createCell(0).setCellValue(result.getId());
                    row.createCell(1).setCellValue(result.getWinningNumbers());

                    // 格式化日期
                    String dateStr = "";
                    if (result.getDrawTime() != null) {
                        dateStr = dateFormat.format(result.getDrawTime());
                    }
                    row.createCell(2).setCellValue(dateStr);

                    row.createCell(3).setCellValue(result.getWinnerUserId());
                    row.createCell(4).setCellValue(result.getPrizeLevel());
                    row.createCell(5).setCellValue(result.getMultiplier());
                }

                // 确保目录存在
                PathManager.ensureDirectoryExists(new File(filePath).getParent());

                // 先写入临时文件
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    workbook.write(fos);
                    fos.flush();
                }

                // 验证临时文件
                if (!isExcelFileValid(tempFile.getAbsolutePath())) {
                    throw new IOException("临时文件验证失败");
                }
            }

            // 删除原文件
            File originalFile = new File(filePath);
            if (originalFile.exists()) {
                if (!originalFile.delete()) {
                    System.err.println("警告: 无法删除原文件: " + filePath);
                }
            }

            // 重命名临时文件为目标文件
            if (!tempFile.renameTo(originalFile)) {
                // 如果重命名失败，尝试复制
                try (FileInputStream fis = new FileInputStream(tempFile);
                     FileOutputStream fos = new FileOutputStream(originalFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                    fos.flush();
                }
            }

            debug("结果数据保存成功: " + filePath);

        } catch (IOException e) {
            System.err.println("保存抽奖结果失败: " + e.getMessage());

            // 尝试恢复备份
            restoreFromBackup(filePath);

            e.printStackTrace();
            throw new RuntimeException("保存抽奖结果失败: " + e.getMessage(), e);
        } finally {
            // 清理临时文件
            if (tempFile != null && tempFile.exists()) {
                if (!tempFile.delete()) {
                    System.err.println("警告: 无法删除临时文件: " + tempFile.getAbsolutePath());
                }
            }
        }
    }

    /**
     * 添加单个结果到Excel
     */
    public void addResult(LotteryResult result) {
        debug("添加单个结果: ID=" + result.getId() + ", 中奖号码=" + result.getWinningNumbers());
        List<LotteryResult> results = loadResults();
        results.add(result);
        saveResults(results);
    }

    /**
     * 如果Excel文件不存在则创建
     */
    public void createExcelFiles() {
        debug("开始创建Excel文件");

        try {
            // 首先确保数据目录存在
            String dataDir = PathManager.getDataDir();
            debug("数据目录: " + dataDir);

            File dataDirFile = new File(dataDir);
            if (!dataDirFile.exists()) {
                boolean created = dataDirFile.mkdirs();
                if (created) {
                    debug("创建数据目录: " + dataDir);
                } else {
                    debug("无法创建数据目录: " + dataDir);
                }
            }

            String userFilePath = PathManager.getUserFilePath();
            String ticketFilePath = PathManager.getTicketFilePath();
            String resultFilePath = PathManager.getResultFilePath();

            debug("用户文件路径: " + userFilePath);
            debug("彩票文件路径: " + ticketFilePath);
            debug("结果文件路径: " + resultFilePath);

            // 创建文件（如果不存在）
            createFileIfNotExists(userFilePath, "Users",
                    new String[]{"ID", "Username", "Password", "Balance", "Phone"});

            createFileIfNotExists(ticketFilePath, "Tickets",
                    new String[]{"ID", "UserID", "Numbers", "BetCount", "PurchaseTime", "IsManual"});

            createFileIfNotExists(resultFilePath, "Results",
                    new String[]{"ID", "WinningNumbers", "DrawTime", "WinnerUserId", "PrizeLevel", "Multiplier"});

            debug("Excel文件创建完成");

        } catch (Exception e) {
            System.err.println("创建Excel文件时发生错误: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("无法创建Excel文件，请检查目录权限");
        }
    }

    /**
     * 辅助方法：创建Excel文件（如果不存在）
     */
    private void createFileIfNotExists(String filePath, String sheetName, String[] headers) {
        File file = new File(filePath);

        if (file.exists()) {
            debug("Excel文件已存在: " + filePath);
            // 验证文件是否损坏
            if (!isExcelFileValid(filePath)) {
                debug("文件已损坏，重新创建: " + filePath);
                file.delete(); // 删除损坏的文件
            } else {
                return;
            }
        }

        debug("创建Excel文件: " + filePath);

        // 确保父目录存在
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            boolean dirsCreated = parentDir.mkdirs();
            if (!dirsCreated) {
                debug("无法创建父目录: " + parentDir.getAbsolutePath());
                return;
            }
        }

        File tempFile = null;
        try {
            // 创建临时文件
            tempFile = new File(filePath + ".tmp");

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet(sheetName);
                Row headerRow = sheet.createRow(0);

                // 设置默认列宽
                for (int i = 0; i < headers.length; i++) {
                    sheet.setColumnWidth(i, 4000);
                }

                // 创建表头
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);

                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                // 先写入临时文件
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    workbook.write(fos);
                    fos.flush();
                }

                // 验证临时文件
                if (!isExcelFileValid(tempFile.getAbsolutePath())) {
                    throw new IOException("临时文件验证失败");
                }
            }

            // 重命名临时文件为目标文件
            if (!tempFile.renameTo(file)) {
                // 如果重命名失败，尝试复制
                try (FileInputStream fis = new FileInputStream(tempFile);
                     FileOutputStream fos = new FileOutputStream(file)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                    fos.flush();
                }
            }

            System.out.println("[INFO] 已创建Excel文件: " + filePath);

        } catch (IOException e) {
            System.err.println("创建Excel文件失败: " + filePath + ", 错误: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 清理临时文件
            if (tempFile != null && tempFile.exists()) {
                if (!tempFile.delete()) {
                    System.err.println("警告: 无法删除临时文件: " + tempFile.getAbsolutePath());
                }
            }
        }
    }

    /**
     * 获取下一个可用的ID
     */
    public int getNextId(String fileType) {
        int maxId = 0;

        try {
            switch (fileType.toLowerCase()) {
                case "users":
                    List<User> users = loadUsers();
                    for (User user : users) {
                        maxId = Math.max(maxId, user.getId());
                    }
                    break;
                case "tickets":
                    List<Ticket> tickets = loadTickets();
                    for (Ticket ticket : tickets) {
                        maxId = Math.max(maxId, ticket.getId());
                    }
                    break;
                case "results":
                    List<LotteryResult> results = loadResults();
                    for (LotteryResult result : results) {
                        maxId = Math.max(maxId, result.getId());
                    }
                    break;
                default:
                    throw new IllegalArgumentException("未知的文件类型: " + fileType);
            }
        } catch (Exception e) {
            debug("获取下一个ID时发生异常，返回默认值1: " + e.getMessage());
            return 1;
        }

        int nextId = maxId + 1;
        debug("获取下一个ID: 类型=" + fileType + ", 最大ID=" + maxId + ", 下一个ID=" + nextId);
        return nextId;
    }

    /**
     * 辅助方法：安全获取单元格字符串值
     */
    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return "";
        }

        try {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    // 如果是日期格式
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return dateFormat.format(cell.getDateCellValue());
                    } else {
                        // 数字转换为字符串，避免科学计数法
                        double num = cell.getNumericCellValue();
                        if (num == (long) num) {
                            return String.valueOf((long) num);
                        } else {
                            return String.valueOf(num);
                        }
                    }
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                case FORMULA:
                    return cell.getCellFormula();
                default:
                    return "";
            }
        } catch (Exception e) {
            debug("获取单元格值时出错: " + e.getMessage());
            return "";
        }
    }

    /**
     * 辅助方法：安全获取单元格数字值
     */
    private double getCellNumericValue(Cell cell) {
        if (cell == null) {
            return 0.0;
        }

        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return cell.getNumericCellValue();
                case STRING:
                    try {
                        return Double.parseDouble(cell.getStringCellValue().trim());
                    } catch (NumberFormatException e) {
                        return 0.0;
                    }
                case BOOLEAN:
                    return cell.getBooleanCellValue() ? 1.0 : 0.0;
                default:
                    return 0.0;
            }
        } catch (Exception e) {
            debug("获取单元格数字值时出错: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * 验证Excel文件是否有效
     */
    private boolean isExcelFileValid(String filePath) {
        if (filePath == null || !filePath.toLowerCase().endsWith(".xlsx")) {
            return false;
        }

        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) {
            return false;
        }

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            // 尝试读取一些基本信息
            int sheetCount = workbook.getNumberOfSheets();
            if (sheetCount == 0) {
                return false;
            }

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return false;
            }

            return true;
        } catch (Exception e) {
            debug("Excel文件验证失败: " + filePath + ", 错误: " + e.getMessage());
            return false;
        }
    }

    /**
     * 尝试恢复损坏的文件
     */
    private boolean attemptFileRecovery(String filePath) {
        debug("尝试恢复文件: " + filePath);

        // 方法1: 尝试从备份恢复
        if (restoreFromBackup(filePath)) {
            return true;
        }

        // 方法2: 尝试读取并修复文件
        try {
            File file = new File(filePath);
            File backupFile = new File(filePath + BACKUP_SUFFIX);

            // 如果文件太小（可能损坏），尝试从备份恢复
            if (file.length() < 100) { // 小于100字节可能不完整
                if (backupFile.exists() && backupFile.length() > 100) {
                    return restoreFromBackup(filePath);
                }
            }

            return false;
        } catch (Exception e) {
            debug("文件恢复尝试失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 创建文件备份
     */
    private void createBackup(String filePath) {
        try {
            File originalFile = new File(filePath);
            if (!originalFile.exists()) {
                return; // 文件不存在，不需要备份
            }

            File backupFile = new File(filePath + BACKUP_SUFFIX);

            // 如果备份文件存在且比原文件新，不需要重新备份
            if (backupFile.exists() &&
                    backupFile.lastModified() > originalFile.lastModified() - 60000) { // 1分钟内
                return;
            }

            // 复制文件
            try (FileInputStream fis = new FileInputStream(originalFile);
                 FileOutputStream fos = new FileOutputStream(backupFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
                fos.flush();
            }

            debug("已创建备份文件: " + backupFile.getAbsolutePath());
        } catch (Exception e) {
            debug("创建备份文件失败: " + e.getMessage());
        }
    }

    /**
     * 从备份恢复文件
     */
    private boolean restoreFromBackup(String filePath) {
        try {
            File backupFile = new File(filePath + BACKUP_SUFFIX);
            if (!backupFile.exists()) {
                debug("备份文件不存在: " + backupFile.getAbsolutePath());
                return false;
            }

            File originalFile = new File(filePath);

            // 验证备份文件
            if (!isExcelFileValid(backupFile.getAbsolutePath())) {
                debug("备份文件无效: " + backupFile.getAbsolutePath());
                return false;
            }

            // 复制备份文件到原文件
            try (FileInputStream fis = new FileInputStream(backupFile);
                 FileOutputStream fos = new FileOutputStream(originalFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
                fos.flush();
            }

            debug("已从备份恢复文件: " + filePath);
            return true;
        } catch (Exception e) {
            debug("从备份恢复文件失败: " + e.getMessage());
            return false;
        }
    }
}