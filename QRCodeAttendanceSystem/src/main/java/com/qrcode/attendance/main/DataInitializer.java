package com.qrcode.attendance.main;

import com.qrcode.attendance.config.AppConfig;
import com.qrcode.attendance.util.LoggerUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * 数据初始化器
 * 在应用程序启动时检查并创建必要的数据结构，包括目录、文件、模板等
 *
 * @author QR签到系统开发团队
 * @version 1.0.0
 * @since 2023-01-01
 */
public class DataInitializer {

    private static final Logger LOGGER = LoggerUtil.getLogger(DataInitializer.class);

    // 数据目录名称常量
    private static final String[] DATA_DIRECTORIES = {
            "attendance",    // 签到记录目录
            "backup",        // 数据备份目录
            "qrcode",        // 二维码图片目录
            "logs",          // 日志文件目录
            "cache",         // 缓存文件目录
            "templates",     // 模板文件目录
            "config",        // 配置文件目录
            "exports"        // 导出文件目录
    };

    // 初始数据文件名称
    private static final String[] INITIAL_DATA_FILES = {
            "classes.json",      // 班级数据文件
            "students.json",     // 学生数据文件
            "attendance.json",   // 签到记录文件
            "system_config.json" // 系统配置文件
    };

    // Excel模板文件名称
    private static final String[] EXCEL_TEMPLATE_FILES = {
            "student_template.xlsx",    // 学生信息模板
            "attendance_template.xlsx", // 签到记录模板
            "class_template.xlsx"       // 班级信息模板
    };

    // 数据版本信息
    private static final String DATA_VERSION = "1.0.0";
    private static final String VERSION_FILE = "data_version.txt";

    /**
     * 初始化数据结构和文件
     */
    public void initializeDataStructure() {
        try {
            LOGGER.info("开始初始化数据结构和文件...");

            // 1. 检查并创建主数据目录
            Path dataDir = createDataDirectory();

            // 2. 检查并创建所有子目录
            createSubDirectories(dataDir);

            // 3. 初始化Excel模板文件
            initializeExcelTemplates(dataDir);

            // 4. 创建初始数据文件
            createInitialDataFiles(dataDir);

            // 5. 检查数据版本兼容性
            checkDataVersion(dataDir);

            // 6. 创建默认配置文件
            createDefaultConfig(dataDir);

            LOGGER.info("数据结构和文件初始化完成");

        } catch (Exception e) {
            LOGGER.severe("初始化数据结构和文件失败: " + e.getMessage());
            throw new RuntimeException("数据初始化失败", e);
        }
    }

    /**
     * 创建主数据目录
     */
    private Path createDataDirectory() throws IOException {
        AppConfig config = AppConfig.getInstance();
        String dataPath = config.getDataDirectory();
        Path dataDir = Paths.get(dataPath);

        if (!Files.exists(dataDir)) {
            Files.createDirectories(dataDir);
            LOGGER.info("创建数据目录: " + dataDir.toAbsolutePath());
        } else {
            LOGGER.info("数据目录已存在: " + dataDir.toAbsolutePath());
        }

        return dataDir;
    }

    /**
     * 创建所有子目录
     */
    private void createSubDirectories(Path dataDir) throws IOException {
        for (String dirName : DATA_DIRECTORIES) {
            Path subDir = dataDir.resolve(dirName);
            if (!Files.exists(subDir)) {
                Files.createDirectories(subDir);
                LOGGER.info("创建子目录: " + dirName);
            }
        }
    }

    /**
     * 初始化Excel模板文件
     */
    private void initializeExcelTemplates(Path dataDir) {
        Path templatesDir = dataDir.resolve("templates");

        for (String templateName : EXCEL_TEMPLATE_FILES) {
            Path templateFile = templatesDir.resolve(templateName);

            if (!Files.exists(templateFile)) {
                try {
                    createExcelTemplate(templateFile, templateName);
                    LOGGER.info("创建Excel模板文件: " + templateName);
                } catch (IOException e) {
                    LOGGER.warning("创建Excel模板文件失败: " + templateName + " - " + e.getMessage());
                }
            }
        }
    }

    /**
     * 创建Excel模板文件
     */
    private void createExcelTemplate(Path templateFile, String templateName) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet;

        if ("student_template.xlsx".equals(templateName)) {
            sheet = workbook.createSheet("学生信息");
            createStudentTemplateSheet(sheet);
        } else if ("attendance_template.xlsx".equals(templateName)) {
            sheet = workbook.createSheet("签到记录");
            createAttendanceTemplateSheet(sheet);
        } else if ("class_template.xlsx".equals(templateName)) {
            sheet = workbook.createSheet("班级信息");
            createClassTemplateSheet(sheet);
        } else {
            sheet = workbook.createSheet("Sheet1");
            Row row = sheet.createRow(0);
            row.createCell(0).setCellValue("模板文件: " + templateName);
        }

        try (FileOutputStream fileOut = new FileOutputStream(templateFile.toFile())) {
            workbook.write(fileOut);
        } finally {
            workbook.close();
        }
    }

    /**
     * 创建学生信息模板Sheet
     */
    private void createStudentTemplateSheet(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        String[] headers = {"学号", "姓名", "性别", "班级", "专业", "手机号", "邮箱", "入学日期"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);

            // 设置标题样式
            CellStyle style = sheet.getWorkbook().createCellStyle();
            Font font = sheet.getWorkbook().createFont();
            font.setBold(true);
            style.setFont(font);
            style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cell.setCellStyle(style);
        }

        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // 添加示例数据行
        Row exampleRow = sheet.createRow(1);
        exampleRow.createCell(0).setCellValue("20230001");
        exampleRow.createCell(1).setCellValue("张三");
        exampleRow.createCell(2).setCellValue("男");
        exampleRow.createCell(3).setCellValue("计算机1班");
        exampleRow.createCell(4).setCellValue("计算机科学与技术");
        exampleRow.createCell(5).setCellValue("13800138000");
        exampleRow.createCell(6).setCellValue("zhangsan@example.com");
        exampleRow.createCell(7).setCellValue("2023-09-01");
    }

    /**
     * 创建签到记录模板Sheet
     */
    private void createAttendanceTemplateSheet(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        String[] headers = {"签到ID", "学号", "姓名", "签到时间", "签到类型", "课程名称", "教师", "签到状态", "备注"};

        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * 创建班级信息模板Sheet
     */
    private void createClassTemplateSheet(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        String[] headers = {"班级ID", "班级名称", "专业", "年级", "辅导员", "学生人数", "创建时间"};

        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * 创建初始数据文件
     */
    private void createInitialDataFiles(Path dataDir) throws IOException {
        for (String fileName : INITIAL_DATA_FILES) {
            Path dataFile = dataDir.resolve(fileName);
            if (!Files.exists(dataFile)) {
                try (FileWriter writer = new FileWriter(dataFile.toFile())) {
                    writer.write("[]");
                    LOGGER.info("创建初始数据文件: " + fileName);
                }
            }
        }
    }

    /**
     * 检查数据版本兼容性
     */
    private void checkDataVersion(Path dataDir) throws IOException {
        Path versionFile = dataDir.resolve(VERSION_FILE);

        if (Files.exists(versionFile)) {
            String existingVersion = new String(Files.readAllBytes(versionFile)).trim();

            if (isVersionCompatible(existingVersion)) {
                LOGGER.info("数据版本兼容: " + existingVersion + " -> " + DATA_VERSION);
            } else {
                LOGGER.warning("数据版本不兼容: " + existingVersion + " -> " + DATA_VERSION);
                handleVersionIncompatibility(dataDir, existingVersion);
            }
        } else {
            try (FileWriter writer = new FileWriter(versionFile.toFile())) {
                writer.write(DATA_VERSION);
                LOGGER.info("创建数据版本文件: " + DATA_VERSION);
            }
        }
    }

    /**
     * 检查版本兼容性
     */
    private boolean isVersionCompatible(String existingVersion) {
        try {
            String[] existingParts = existingVersion.split("\\.");
            String[] currentParts = DATA_VERSION.split("\\.");
            return existingParts[0].equals(currentParts[0]);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 处理版本不兼容情况
     */
    private void handleVersionIncompatibility(Path dataDir, String existingVersion) throws IOException {
        LOGGER.warning("检测到数据版本不兼容，执行数据迁移...");

        createDataBackup(dataDir, "version_upgrade_" + existingVersion + "_to_" + DATA_VERSION);

        Path versionFile = dataDir.resolve(VERSION_FILE);
        try (FileWriter writer = new FileWriter(versionFile.toFile())) {
            writer.write(DATA_VERSION);
        }

        LOGGER.info("数据版本已更新: " + existingVersion + " -> " + DATA_VERSION);
    }

    /**
     * 创建默认配置文件
     */
    private void createDefaultConfig(Path dataDir) throws IOException {
        Path configDir = dataDir.resolve("config");
        Path configFile = configDir.resolve("app_config.json");

        if (!Files.exists(configFile)) {
            try (FileWriter writer = new FileWriter(configFile.toFile())) {
                String defaultConfig = "{\n" +
                        "  \"app_name\": \"QR签到系统\",\n" +
                        "  \"version\": \"" + DATA_VERSION + "\",\n" +
                        "  \"qr_code_expiry_minutes\": 5,\n" +
                        "  \"auto_backup\": true,\n" +
                        "  \"backup_interval_hours\": 24,\n" +
                        "  \"max_log_files\": 30,\n" +
                        "  \"default_class_size\": 50,\n" +
                        "  \"export_format\": \"excel\",\n" +
                        "  \"server_port\": 8080\n" +
                        "}";

                writer.write(defaultConfig);
                LOGGER.info("创建默认配置文件");
            }
        }
    }

    /***************************** 数据备份和恢复 *****************************/

    /**
     * 创建数据备份
     */
    public void createDataBackup(Path dataDir, String backupNote) throws IOException {
        Path backupDir = dataDir.resolve("backup");
        if (!Files.exists(backupDir)) {
            Files.createDirectories(backupDir);
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = dateFormat.format(new Date());
        String backupFileName = String.format("backup_%s_%s.txt", timestamp, backupNote);

        Path backupInfoFile = backupDir.resolve("latest_backup.txt");
        try (FileWriter writer = new FileWriter(backupInfoFile.toFile())) {
            writer.write("备份时间: " + timestamp + "\n");
            writer.write("备份说明: " + backupNote + "\n");
            writer.write("备份文件: " + backupFileName + "\n");
        }

        LOGGER.info("数据备份已创建: " + backupFileName);
    }

    /**
     * 重置数据
     */
    public void resetData(boolean backupFirst) throws IOException {
        AppConfig config = AppConfig.getInstance();
        Path dataDir = Paths.get(config.getDataDirectory());

        if (backupFirst) {
            createDataBackup(dataDir, "reset_backup");
        }

        for (String fileName : INITIAL_DATA_FILES) {
            Path dataFile = dataDir.resolve(fileName);
            if (Files.exists(dataFile)) {
                Files.delete(dataFile);
                LOGGER.info("删除数据文件: " + fileName);
            }
        }

        initializeDataStructure();
        LOGGER.warning("数据已重置");
    }

    /***************************** 数据完整性检查 *****************************/

    /**
     * 检查数据文件完整性
     */
    public boolean checkDataIntegrity() {
        try {
            AppConfig config = AppConfig.getInstance();
            Path dataDir = Paths.get(config.getDataDirectory());

            for (String dirName : DATA_DIRECTORIES) {
                Path subDir = dataDir.resolve(dirName);
                if (!Files.exists(subDir)) {
                    LOGGER.warning("缺失目录: " + dirName);
                    return false;
                }
            }

            for (String fileName : INITIAL_DATA_FILES) {
                Path dataFile = dataDir.resolve(fileName);
                if (!Files.exists(dataFile)) {
                    LOGGER.warning("缺失数据文件: " + fileName);
                    return false;
                }
            }

            Path versionFile = dataDir.resolve(VERSION_FILE);
            if (!Files.exists(versionFile)) {
                LOGGER.warning("缺失版本文件");
                return false;
            }

            LOGGER.info("数据完整性检查通过");
            return true;

        } catch (Exception e) {
            LOGGER.severe("数据完整性检查失败: " + e.getMessage());
            return false;
        }
    }

    /***************************** 工具方法 *****************************/

    /**
     * 获取数据目录信息
     */
    public String getDataDirectoryInfo() {
        try {
            AppConfig config = AppConfig.getInstance();
            Path dataDir = Paths.get(config.getDataDirectory());

            if (!Files.exists(dataDir)) {
                return "数据目录不存在";
            }

            StringBuilder info = new StringBuilder();
            info.append("数据目录: ").append(dataDir.toAbsolutePath()).append("\n");

            for (String dirName : DATA_DIRECTORIES) {
                Path subDir = dataDir.resolve(dirName);
                if (Files.exists(subDir)) {
                    long size = calculateDirectorySize(subDir);
                    long fileCount = countFiles(subDir);
                    info.append(String.format("  %-15s: %d 文件，%d KB\n",
                            dirName, fileCount, size / 1024));
                }
            }

            return info.toString();

        } catch (Exception e) {
            return "获取数据目录信息失败: " + e.getMessage();
        }
    }

    /**
     * 计算目录大小
     */
    private long calculateDirectorySize(Path directory) throws IOException {
        try (var pathStream = Files.walk(directory)) {
            return pathStream
                    .filter(p -> p.toFile().isFile())
                    .mapToLong(p -> p.toFile().length())
                    .sum();
        }
    }

    /**
     * 计算目录中的文件数量
     */
    private long countFiles(Path directory) throws IOException {
        try (var pathStream = Files.walk(directory)) {
            return pathStream
                    .filter(p -> p.toFile().isFile())
                    .count();
        }
    }
}