package lottery.dao;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Excel数据访问层
 */
public class ExcelDao {
    private static final String DATA_DIR = "data";

    // Excel文件路径
    private String usersFile;
    private String ticketsFile;
    private String resultsFile;

    public ExcelDao() {
        createDataDirectory();
        usersFile = DATA_DIR + "/users.xlsx";
        ticketsFile = DATA_DIR + "/tickets.xlsx";
        resultsFile = DATA_DIR + "/results.xlsx";

        // 初始化Excel文件（如果不存在则创建）
        initializeExcelFiles();
    }

    /**
     * 创建数据目录
     */
    private void createDataDirectory() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            System.out.println("创建数据目录: " + DATA_DIR);
            boolean created = dir.mkdirs();
            if (!created) {
                System.err.println("无法创建数据目录: " + DATA_DIR);
            }
        }
    }

    /**
     * 初始化Excel文件
     */
    private void initializeExcelFiles() {
        try {
            // 创建空的Excel文件
            createEmptyExcel(usersFile,
                    Arrays.asList("id", "username", "password", "balance", "phone"));

            createEmptyExcel(ticketsFile,
                    Arrays.asList("id", "userId", "numbers", "betCount", "purchaseTime", "isManual"));

            createEmptyExcel(resultsFile,
                    Arrays.asList("id", "period", "winningNumbers", "drawTime"));

            System.out.println("Excel文件初始化完成");

        } catch (Exception e) {
            System.err.println("初始化Excel文件失败: " + e.getMessage());
        }
    }

    /**
     * 创建空的Excel文件
     */
    private void createEmptyExcel(String filePath, List<String> headers) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("创建文件: " + filePath);

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Data");

            // 创建表头
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
            }

            // 写入文件
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
            workbook.close();
        }
    }

    /**
     * 保存用户数据
     */
    public void saveUsers(List<Map<String, Object>> users) throws IOException {
        saveToExcel(usersFile, users, Arrays.asList("id", "username", "password", "balance", "phone"));
    }

    /**
     * 读取用户数据
     */
    public List<Map<String, Object>> loadUsers() throws IOException {
        return loadFromExcel(usersFile);
    }

    /**
     * 保存彩票数据
     */
    public void saveTickets(List<Map<String, Object>> tickets) throws IOException {
        saveToExcel(ticketsFile, tickets, Arrays.asList("id", "userId", "numbers", "betCount", "purchaseTime", "isManual"));
    }

    /**
     * 读取彩票数据
     */
    public List<Map<String, Object>> loadTickets() throws IOException {
        return loadFromExcel(ticketsFile);
    }

    /**
     * 保存开奖结果
     */
    public void saveResults(List<Map<String, Object>> results) throws IOException {
        saveToExcel(resultsFile, results, Arrays.asList("id", "period", "winningNumbers", "drawTime"));
    }

    /**
     * 读取开奖结果
     */
    public List<Map<String, Object>> loadResults() throws IOException {
        return loadFromExcel(resultsFile);
    }

    /**
     * 通用Excel保存方法
     */
    private void saveToExcel(String filePath, List<Map<String, Object>> data, List<String> headers)
            throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");

        // 创建表头
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
        }

        // 填充数据
        int rowNum = 1;
        for (Map<String, Object> rowData : data) {
            Row row = sheet.createRow(rowNum++);
            int colNum = 0;
            for (String header : headers) {
                Object value = rowData.get(header);
                Cell cell = row.createCell(colNum++);
                setCellValue(cell, value);
            }
        }

        // 写入文件
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            workbook.write(fos);
        }
        workbook.close();
    }

    /**
     * 通用Excel读取方法
     */
    private List<Map<String, Object>> loadFromExcel(String filePath) throws IOException {
        File file = new File(filePath);

        // 如果文件不存在，返回空列表
        if (!file.exists()) {
            System.out.println("文件不存在: " + filePath);
            return new ArrayList<>();
        }

        // 检查文件大小
        if (file.length() == 0) {
            System.out.println("文件为空: " + filePath);
            return new ArrayList<>();
        }

        List<Map<String, Object>> data = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                return new ArrayList<>();
            }

            // 读取表头
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return new ArrayList<>();
            }

            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue());
            }

            // 读取数据行
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, Object> rowData = new HashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j);
                    rowData.put(headers.get(j), getCellValue(cell));
                }
                data.add(rowData);
            }

        } catch (Exception e) {
            System.err.println("读取Excel文件出错: " + filePath + " - " + e.getMessage());
            return new ArrayList<>();
        }

        return data;
    }

    /**
     * 设置单元格值
     */
    private void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue(value.toString());
        }
    }

    /**
     * 获取单元格值
     */
    private Object getCellValue(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                }
                return cell.getNumericCellValue();
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }
}