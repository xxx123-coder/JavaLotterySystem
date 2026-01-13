package lottery.dao;

import lottery.model.User;
import lottery.model.Ticket;
import lottery.model.LotteryResult;
import lottery.util.FileUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExcelDao {
    // Excel文件路径 - 使用相对路径
    private static final String USER_FILE = "data/users.xlsx";
    private static final String TICKET_FILE = "data/tickets.xlsx";
    private static final String RESULT_FILE = "data/results.xlsx";

    // 日期格式化器
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取用户文件完整路径
     */
    private String getUserFilePath() {
        return FileUtils.getDataFilePath("users.xlsx");
    }

    /**
     * 获取彩票文件完整路径
     */
    private String getTicketFilePath() {
        return FileUtils.getDataFilePath("tickets.xlsx");
    }

    /**
     * 获取结果文件完整路径
     */
    private String getResultFilePath() {
        return FileUtils.getDataFilePath("results.xlsx");
    }

    /**
     * 加载所有用户
     */
    public List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        String filePath = getUserFilePath();

        try {
            File file = new File(filePath);
            if (!file.exists()) {
                System.out.println("用户文件不存在，将创建新文件");
                createExcelFiles();
                return users;
            }

            try (FileInputStream fis = new FileInputStream(filePath);
                 Workbook workbook = new XSSFWorkbook(fis)) {
                Sheet sheet = workbook.getSheetAt(0);
                Iterator<Row> rowIterator = sheet.iterator();

                // 跳过标题行
                if (rowIterator.hasNext()) {
                    rowIterator.next();
                }

                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    User user = new User();

                    user.setId((int) row.getCell(0).getNumericCellValue());
                    user.setUsername(getCellStringValue(row.getCell(1)));
                    user.setPassword(getCellStringValue(row.getCell(2)));
                    user.setBalance(getCellNumericValue(row.getCell(3)));
                    user.setPhone(getCellStringValue(row.getCell(4)));

                    users.add(user);
                }
            }
        } catch (IOException e) {
            System.err.println("加载用户数据失败: " + e.getMessage());
        }

        return users;
    }

    /**
     * 保存用户列表到Excel
     */
    public void saveUsers(List<User> users) {
        String filePath = getUserFilePath();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Users");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Username", "Password", "Balance", "Phone"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
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
            FileUtils.ensureFileDirectory(filePath);

            // 写入文件
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        } catch (IOException e) {
            System.err.println("保存用户数据失败: " + e.getMessage());
        }
    }

    /**
     * 添加单个用户到Excel
     */
    public void addUser(User user) {
        List<User> users = loadUsers();
        users.add(user);
        saveUsers(users);
    }

    /**
     * 加载所有彩票
     */
    public List<Ticket> loadTickets() {
        List<Ticket> tickets = new ArrayList<>();
        String filePath = getTicketFilePath();

        try {
            File file = new File(filePath);
            if (!file.exists()) {
                System.out.println("彩票文件不存在，将创建新文件");
                createExcelFiles();
                return tickets;
            }

            try (FileInputStream fis = new FileInputStream(filePath);
                 Workbook workbook = new XSSFWorkbook(fis)) {
                Sheet sheet = workbook.getSheetAt(0);
                Iterator<Row> rowIterator = sheet.iterator();

                // 跳过标题行
                if (rowIterator.hasNext()) {
                    rowIterator.next();
                }

                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    Ticket ticket = new Ticket();

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
                            System.err.println("解析购买时间失败: " + dateStr);
                        }
                    }

                    // 解析布尔值
                    String manualStr = getCellStringValue(row.getCell(5));
                    ticket.setManual("true".equalsIgnoreCase(manualStr) || "1".equals(manualStr));

                    tickets.add(ticket);
                }
            }
        } catch (IOException e) {
            System.err.println("加载彩票数据失败: " + e.getMessage());
        }

        return tickets;
    }

    /**
     * 保存彩票列表到Excel
     */
    public void saveTickets(List<Ticket> tickets) {
        String filePath = getTicketFilePath();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Tickets");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "UserID", "Numbers", "BetCount", "PurchaseTime", "IsManual"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
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
            FileUtils.ensureFileDirectory(filePath);

            // 写入文件
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        } catch (IOException e) {
            System.err.println("保存彩票数据失败: " + e.getMessage());
        }
    }

    /**
     * 添加单个彩票到Excel
     */
    public void addTicket(Ticket ticket) {
        List<Ticket> tickets = loadTickets();
        tickets.add(ticket);
        saveTickets(tickets);
    }

    /**
     * 加载所有抽奖结果
     */
    public List<LotteryResult> loadResults() {
        List<LotteryResult> results = new ArrayList<>();
        String filePath = getResultFilePath();

        try {
            File file = new File(filePath);
            if (!file.exists()) {
                System.out.println("结果文件不存在，将创建新文件");
                createExcelFiles();
                return results;
            }

            try (FileInputStream fis = new FileInputStream(filePath);
                 Workbook workbook = new XSSFWorkbook(fis)) {
                Sheet sheet = workbook.getSheetAt(0);
                Iterator<Row> rowIterator = sheet.iterator();

                // 跳过标题行
                if (rowIterator.hasNext()) {
                    rowIterator.next();
                }

                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    LotteryResult result = new LotteryResult();

                    result.setId((int) row.getCell(0).getNumericCellValue());
                    result.setWinningNumbers(getCellStringValue(row.getCell(1)));

                    // 解析日期
                    String dateStr = getCellStringValue(row.getCell(2));
                    if (dateStr != null && !dateStr.isEmpty()) {
                        try {
                            result.setDrawTime(dateFormat.parse(dateStr));
                        } catch (Exception e) {
                            System.err.println("解析抽奖时间失败: " + dateStr);
                        }
                    }

                    result.setWinnerUserId((int) row.getCell(3).getNumericCellValue());
                    result.setPrizeLevel(getCellStringValue(row.getCell(4)));
                    result.setMultiplier((int) row.getCell(5).getNumericCellValue());

                    results.add(result);
                }
            }
        } catch (IOException e) {
            System.err.println("加载抽奖结果失败: " + e.getMessage());
        }

        return results;
    }

    /**
     * 保存结果列表到Excel
     */
    public void saveResults(List<LotteryResult> results) {
        String filePath = getResultFilePath();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Results");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "WinningNumbers", "DrawTime", "WinnerUserId", "PrizeLevel", "Multiplier"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
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
            FileUtils.ensureFileDirectory(filePath);

            // 写入文件
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        } catch (IOException e) {
            System.err.println("保存抽奖结果失败: " + e.getMessage());
        }
    }

    /**
     * 添加单个结果到Excel
     */
    public void addResult(LotteryResult result) {
        List<LotteryResult> results = loadResults();
        results.add(result);
        saveResults(results);
    }

    /**
     * 如果Excel文件不存在则创建
     */
    public void createExcelFiles() {
        String userFilePath = getUserFilePath();
        String ticketFilePath = getTicketFilePath();
        String resultFilePath = getResultFilePath();

        createFileIfNotExists(userFilePath, "Users",
                new String[]{"ID", "Username", "Password", "Balance", "Phone"});
        createFileIfNotExists(ticketFilePath, "Tickets",
                new String[]{"ID", "UserID", "Numbers", "BetCount", "PurchaseTime", "IsManual"});
        createFileIfNotExists(resultFilePath, "Results",
                new String[]{"ID", "WinningNumbers", "DrawTime", "WinnerUserId", "PrizeLevel", "Multiplier"});
    }

    /**
     * 辅助方法：创建Excel文件
     */
    private void createFileIfNotExists(String filePath, String sheetName, String[] headers) {
        File file = new File(filePath);
        if (!file.exists()) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet(sheetName);
                Row headerRow = sheet.createRow(0);

                for (int i = 0; i < headers.length; i++) {
                    headerRow.createCell(i).setCellValue(headers[i]);
                }

                // 确保目录存在
                FileUtils.ensureFileDirectory(filePath);

                try (FileOutputStream fos = new FileOutputStream(file)) {
                    workbook.write(fos);
                }
                System.out.println("已创建文件: " + filePath);
            } catch (IOException e) {
                System.err.println("创建文件失败: " + filePath + ", 错误: " + e.getMessage());
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
            // 如果文件为空或不存在，从0开始
            return 1;
        }
        return maxId + 1;
    }

    /**
     * 辅助方法：安全获取单元格字符串值
     */
    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return "";
        }
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
    }

    /**
     * 辅助方法：安全获取单元格数字值
     */
    private double getCellNumericValue(Cell cell) {
        if (cell == null) {
            return 0.0;
        }
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
    }
}