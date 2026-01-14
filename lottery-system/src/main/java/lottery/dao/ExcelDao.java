package lottery.dao; // 定义包路径，表明该类属于lottery.dao包，负责Excel数据访问操作

// 导入必要的类和包
import lottery.util.FileUtils; // 导入自定义文件工具类
import org.apache.poi.ss.usermodel.*; // 导入Apache POI库的用户模型类，用于操作Excel
import org.apache.poi.xssf.usermodel.XSSFWorkbook; // 导入XSSFWorkbook类，用于处理.xlsx格式的Excel文件
import java.io.File; // 导入Java文件类，用于文件操作
import java.io.FileInputStream; // 导入文件输入流类，用于读取文件
import java.io.FileOutputStream; // 导入文件输出流类，用于写入文件
import java.io.IOException; // 导入IOException异常类，用于处理输入输出异常
import java.util.*; // 导入Java常用工具类，包括List、Map、ArrayList、HashMap等

/**
 * Excel数据访问层
 * 负责所有Excel文件的读写操作，使用Apache POI库
 */
public class ExcelDao {
    // 使用FileUtils获取数据目录，FileUtils是自定义工具类，用于管理文件路径
    private static final String DATA_DIR = FileUtils.getDataDir();

    // Excel文件路径变量，存储各个数据文件的完整路径
    private String usersFile; // 用户数据文件路径
    private String ticketsFile; // 彩票数据文件路径
    private String resultsFile; // 开奖结果文件路径
    private String winningsFile; // 新增：中奖记录文件路径

    /**
     * 构造函数
     * 初始化数据目录和文件路径，并创建Excel文件（如果不存在）
     */
    public ExcelDao() {
        createDataDirectory(); // 调用方法创建数据目录

        // 使用FileUtils获取完整文件路径
        usersFile = FileUtils.getUserFilePath(); // 获取用户文件路径
        ticketsFile = FileUtils.getTicketFilePath(); // 获取彩票文件路径
        resultsFile = FileUtils.getResultFilePath(); // 获取开奖结果文件路径
        winningsFile = FileUtils.getWinningFilePath(); // 使用新增的方法获取中奖记录文件路径

        // 初始化Excel文件（如果不存在则创建）
        initializeExcelFiles(); // 调用方法创建空的Excel文件
    }

    /**
     * 创建数据目录
     * 调用FileUtils工具类确保数据目录存在
     */
    private void createDataDirectory() {
        // 使用FileUtils确保目录存在
        FileUtils.ensureAllDirectories(); // 调用工具类方法创建所有必要的目录
    }

    /**
     * 初始化Excel文件
     * 为每个数据文件创建空的Excel文件并设置表头
     */
    private void initializeExcelFiles() {
        try {
            // 创建空的Excel文件，参数：文件路径和表头列表
            createEmptyExcel(usersFile, // 创建用户数据文件
                    Arrays.asList("id", "username", "password", "balance", "phone")); // 用户文件表头

            createEmptyExcel(ticketsFile, // 创建彩票数据文件
                    Arrays.asList("id", "userId", "numbers", "betCount", "purchaseTime", "isManual")); // 彩票文件表头

            createEmptyExcel(resultsFile, // 创建开奖结果数据文件
                    Arrays.asList("id", "period", "winningNumbers", "drawTime")); // 开奖结果文件表头

            // 新增：创建中奖记录文件
            createEmptyExcel(winningsFile, // 创建中奖记录数据文件
                    Arrays.asList("id", "userId", "ticketId", "resultId", "matchCount",
                            "prizeLevel", "prizeAmount", "winTime", "isNotified")); // 中奖记录文件表头

            System.out.println("Excel文件初始化完成"); // 输出初始化完成日志
            System.out.println("用户文件位置: " + usersFile); // 输出用户文件路径
            System.out.println("彩票文件位置: " + ticketsFile); // 输出彩票文件路径
            System.out.println("开奖结果文件位置: " + resultsFile); // 输出开奖结果文件路径
            System.out.println("中奖记录文件位置: " + winningsFile); // 输出中奖记录文件路径

        } catch (Exception e) { // 捕获初始化过程中的异常
            System.err.println("初始化Excel文件失败: " + e.getMessage()); // 输出错误信息
            e.printStackTrace(); // 打印异常堆栈信息
        }
    }

    /**
     * 创建空的Excel文件
     * @param filePath 文件路径
     * @param headers 表头列表
     * @throws IOException 输入输出异常
     */
    private void createEmptyExcel(String filePath, List<String> headers) throws IOException {
        File file = new File(filePath); // 创建File对象
        if (!file.exists()) { // 如果文件不存在
            System.out.println("创建文件: " + filePath); // 输出创建文件日志

            // 确保父目录存在
            file.getParentFile().mkdirs(); // 创建父目录（如果不存在）

            Workbook workbook = new XSSFWorkbook(); // 创建新的工作簿
            Sheet sheet = workbook.createSheet("Data"); // 创建工作表，命名为"Data"

            // 创建表头行
            Row headerRow = sheet.createRow(0); // 创建第一行作为表头行
            for (int i = 0; i < headers.size(); i++) { // 遍历表头列表
                Cell cell = headerRow.createCell(i); // 创建单元格
                cell.setCellValue(headers.get(i)); // 设置单元格值为表头内容
            }

            // 写入文件
            try (FileOutputStream fos = new FileOutputStream(filePath)) { // 创建文件输出流
                workbook.write(fos); // 将工作簿写入文件
            }
            workbook.close(); // 关闭工作簿，释放资源
        }
    }

    /**
     * 保存用户数据
     * @param users 用户数据列表
     * @throws IOException 输入输出异常
     */
    public void saveUsers(List<Map<String, Object>> users) throws IOException {
        saveToExcel(usersFile, users, Arrays.asList("id", "username", "password", "balance", "phone")); // 调用通用保存方法
    }

    /**
     * 读取用户数据
     * @return 用户数据列表
     * @throws IOException 输入输出异常
     */
    public List<Map<String, Object>> loadUsers() throws IOException {
        return loadFromExcel(usersFile); // 调用通用读取方法
    }

    /**
     * 保存彩票数据
     * @param tickets 彩票数据列表
     * @throws IOException 输入输出异常
     */
    public void saveTickets(List<Map<String, Object>> tickets) throws IOException {
        saveToExcel(ticketsFile, tickets, Arrays.asList("id", "userId", "numbers", "betCount", "purchaseTime", "isManual")); // 调用通用保存方法
    }

    /**
     * 读取彩票数据
     * @return 彩票数据列表
     * @throws IOException 输入输出异常
     */
    public List<Map<String, Object>> loadTickets() throws IOException {
        return loadFromExcel(ticketsFile); // 调用通用读取方法
    }

    /**
     * 保存开奖结果
     * @param results 开奖结果数据列表
     * @throws IOException 输入输出异常
     */
    public void saveResults(List<Map<String, Object>> results) throws IOException {
        saveToExcel(resultsFile, results, Arrays.asList("id", "period", "winningNumbers", "drawTime")); // 调用通用保存方法
    }

    /**
     * 读取开奖结果
     * @return 开奖结果数据列表
     * @throws IOException 输入输出异常
     */
    public List<Map<String, Object>> loadResults() throws IOException {
        return loadFromExcel(resultsFile); // 调用通用读取方法
    }

    /**
     * 保存中奖记录（新增）
     * @param winnings 中奖记录数据列表
     * @throws IOException 输入输出异常
     */
    public void saveWinnings(List<Map<String, Object>> winnings) throws IOException {
        saveToExcel(winningsFile, winnings, Arrays.asList("id", "userId", "ticketId", "resultId",
                "matchCount", "prizeLevel", "prizeAmount", "winTime", "isNotified")); // 调用通用保存方法
    }

    /**
     * 读取中奖记录（新增）
     * @return 中奖记录数据列表
     * @throws IOException 输入输出异常
     */
    public List<Map<String, Object>> loadWinnings() throws IOException {
        return loadFromExcel(winningsFile); // 调用通用读取方法
    }

    /**
     * 通用Excel保存方法
     * @param filePath 文件路径
     * @param data 要保存的数据列表
     * @param headers 表头列表
     * @throws IOException 输入输出异常
     */
    private void saveToExcel(String filePath, List<Map<String, Object>> data, List<String> headers)
            throws IOException {
        // 确保目录存在
        new File(filePath).getParentFile().mkdirs(); // 创建父目录（如果不存在）

        Workbook workbook = new XSSFWorkbook(); // 创建新的工作簿
        Sheet sheet = workbook.createSheet("Sheet1"); // 创建工作表，命名为"Sheet1"

        // 创建表头行
        Row headerRow = sheet.createRow(0); // 创建第一行作为表头行
        for (int i = 0; i < headers.size(); i++) { // 遍历表头列表
            Cell cell = headerRow.createCell(i); // 创建单元格
            cell.setCellValue(headers.get(i)); // 设置单元格值为表头内容
        }

        // 填充数据行
        int rowNum = 1; // 从第二行开始填充数据（第一行是表头）
        for (Map<String, Object> rowData : data) { // 遍历数据列表
            Row row = sheet.createRow(rowNum++); // 创建新行
            int colNum = 0; // 列索引从0开始
            for (String header : headers) { // 遍历表头
                Object value = rowData.get(header); // 根据表头获取数据值
                Cell cell = row.createCell(colNum++); // 创建单元格
                setCellValue(cell, value); // 调用方法设置单元格值
            }
        }

        // 写入文件
        try (FileOutputStream fos = new FileOutputStream(filePath)) { // 创建文件输出流
            workbook.write(fos); // 将工作簿写入文件
        }
        workbook.close(); // 关闭工作簿，释放资源
    }

    /**
     * 通用Excel读取方法
     * @param filePath 文件路径
     * @return 读取的数据列表
     * @throws IOException 输入输出异常
     */
    private List<Map<String, Object>> loadFromExcel(String filePath) throws IOException {
        File file = new File(filePath); // 创建File对象

        // 如果文件不存在，返回空列表
        if (!file.exists()) { // 检查文件是否存在
            System.out.println("文件不存在: " + filePath); // 输出文件不存在日志
            return new ArrayList<>(); // 返回空列表
        }

        // 检查文件大小
        if (file.length() == 0) { // 如果文件大小为0
            System.out.println("文件为空: " + filePath); // 输出文件为空日志
            return new ArrayList<>(); // 返回空列表
        }

        List<Map<String, Object>> data = new ArrayList<>(); // 创建列表存储读取的数据

        try (FileInputStream fis = new FileInputStream(file); // 创建文件输入流
             Workbook workbook = new XSSFWorkbook(fis)) { // 创建工作簿对象

            Sheet sheet = workbook.getSheetAt(0); // 获取第一个工作表
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) { // 如果工作表不存在或为空
                return new ArrayList<>(); // 返回空列表
            }

            // 读取表头行
            Row headerRow = sheet.getRow(0); // 获取第一行（表头行）
            if (headerRow == null) { // 如果表头行为空
                return new ArrayList<>(); // 返回空列表
            }

            List<String> headers = new ArrayList<>(); // 创建列表存储表头
            for (Cell cell : headerRow) { // 遍历表头行的所有单元格
                headers.add(cell.getStringCellValue()); // 获取单元格字符串值并添加到表头列表
            }

            // 读取数据行（从第二行开始）
            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // 遍历所有数据行
                Row row = sheet.getRow(i); // 获取当前行
                if (row == null) continue; // 如果行为空，跳过

                Map<String, Object> rowData = new HashMap<>(); // 创建Map存储当前行数据
                for (int j = 0; j < headers.size(); j++) { // 遍历表头
                    Cell cell = row.getCell(j); // 获取单元格
                    rowData.put(headers.get(j), getCellValue(cell)); // 将表头和数据值放入Map
                }
                data.add(rowData); // 将当前行数据添加到数据列表
            }

        } catch (Exception e) { // 捕获读取过程中的异常
            System.err.println("读取Excel文件出错: " + filePath + " - " + e.getMessage()); // 输出错误信息
            return new ArrayList<>(); // 返回空列表
        }

        return data; // 返回读取的数据列表
    }

    /**
     * 设置单元格值
     * 根据值的数据类型设置相应的单元格类型
     * @param cell 单元格对象
     * @param value 要设置的值
     */
    private void setCellValue(Cell cell, Object value) {
        if (value == null) { // 如果值为null
            cell.setCellValue(""); // 设置单元格为空字符串
        } else if (value instanceof String) { // 如果值是字符串类型
            cell.setCellValue((String) value); // 设置单元格为字符串值
        } else if (value instanceof Integer) { // 如果值是整数类型
            cell.setCellValue((Integer) value); // 设置单元格为整数值
        } else if (value instanceof Double) { // 如果值是双精度浮点数类型
            cell.setCellValue((Double) value); // 设置单元格为双精度浮点数值
        } else if (value instanceof Boolean) { // 如果值是布尔类型
            cell.setCellValue((Boolean) value); // 设置单元格为布尔值
        } else { // 其他类型
            cell.setCellValue(value.toString()); // 转换为字符串后设置
        }
    }

    /**
     * 获取单元格值
     * 根据单元格类型获取相应的Java对象
     * @param cell 单元格对象
     * @return 单元格的值（转换为相应的Java对象）
     */
    private Object getCellValue(Cell cell) {
        if (cell == null) return null; // 如果单元格为null，返回null

        switch (cell.getCellType()) { // 根据单元格类型处理
            case STRING: // 字符串类型
                return cell.getStringCellValue(); // 返回字符串值
            case NUMERIC: // 数字类型
                if (DateUtil.isCellDateFormatted(cell)) { // 如果单元格格式化为日期
                    return cell.getDateCellValue(); // 返回日期值
                }
                return cell.getNumericCellValue(); // 否则返回数字值
            case BOOLEAN: // 布尔类型
                return cell.getBooleanCellValue(); // 返回布尔值
            case FORMULA: // 公式类型
                return cell.getCellFormula(); // 返回公式字符串
            default: // 其他类型
                return null; // 返回null
        }
    }
}