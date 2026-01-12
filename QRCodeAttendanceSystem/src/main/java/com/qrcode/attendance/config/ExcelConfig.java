package com.qrcode.attendance.config;

import java.io.File;
import java.util.*;

/**
 * Excel文件配置类
 * 专门管理Excel相关配置
 */
public class ExcelConfig {

    // 工作表名称
    private String attendanceSheetName = "考勤记录";
    private String studentSheetName = "学生信息";
    private String summarySheetName = "统计汇总";

    // 列配置
    private final Map<String, ColumnConfig> columnConfigs;

    // 数据验证规则
    private final Map<String, ValidationRule> validationRules;

    // 文件路径配置
    private String templatePath = "./templates";
    private String exportPath = "./exports";
    private String backupPath = "./backups";
    private String templateVersion = "1.0";
    private String templateName = "attendance_template.xlsx";

    public ExcelConfig() {
        this.columnConfigs = new LinkedHashMap<>();
        this.validationRules = new HashMap<>();
        initDefaultConfig();
        initValidationRules();
    }

    /**
     * 列配置类
     */
    public static class ColumnConfig {
        private final String columnName;
        private int columnIndex;
        private int width;
        private boolean required;
        private String validationRule;
        private String description;

        public ColumnConfig(String columnName, int columnIndex, int width) {
            this.columnName = columnName;
            this.columnIndex = columnIndex;
            this.width = width;
            this.required = true;
        }

        // Getter方法
        @SuppressWarnings("unused")
        public String getColumnName() { return columnName; }

        @SuppressWarnings("unused")
        public int getColumnIndex() { return columnIndex; }
        @SuppressWarnings("unused")
        public void setColumnIndex(int columnIndex) { this.columnIndex = columnIndex; }

        @SuppressWarnings("unused")
        public int getWidth() { return width; }
        @SuppressWarnings("unused")
        public void setWidth(int width) { this.width = width; }

        @SuppressWarnings("unused")
        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }

        @SuppressWarnings("unused")
        public String getValidationRule() { return validationRule; }
        public void setValidationRule(String validationRule) { this.validationRule = validationRule; }

        @SuppressWarnings("unused")
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    /**
     * 数据验证规则
     */
    public static class ValidationRule {
        private final String ruleName;
        private String regex;
        private String errorMessage;
        private int minLength;
        private int maxLength;
        private List<String> allowedValues;

        public ValidationRule(String ruleName, String regex, String errorMessage) {
            this.ruleName = ruleName;
            this.regex = regex;
            this.errorMessage = errorMessage;
        }

        public boolean validate(String value) {
            if (value == null) return false;
            if (regex != null && !value.matches(regex)) return false;
            if (minLength > 0 && value.length() < minLength) return false;
            if (maxLength > 0 && value.length() > maxLength) return false;
            if (allowedValues != null && !allowedValues.contains(value)) return false;
            return true;
        }

        // Getter和Setter方法
        @SuppressWarnings("unused")
        public String getRuleName() { return ruleName; }

        @SuppressWarnings("unused")
        public String getRegex() { return regex; }
        @SuppressWarnings("unused")
        public void setRegex(String regex) { this.regex = regex; }

        public String getErrorMessage() { return errorMessage; }
        @SuppressWarnings("unused")
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

        @SuppressWarnings("unused")
        public int getMinLength() { return minLength; }
        @SuppressWarnings("unused")
        public void setMinLength(int minLength) { this.minLength = minLength; }

        @SuppressWarnings("unused")
        public int getMaxLength() { return maxLength; }
        @SuppressWarnings("unused")
        public void setMaxLength(int maxLength) { this.maxLength = maxLength; }

        @SuppressWarnings("unused")
        public List<String> getAllowedValues() { return allowedValues; }
        public void setAllowedValues(List<String> allowedValues) { this.allowedValues = allowedValues; }
    }

    /**
     * 初始化默认配置
     */
    private void initDefaultConfig() {
        // 考勤记录表列配置
        columnConfigs.put("student_id", new ColumnConfig("学号", 0, 15));
        columnConfigs.get("student_id").setValidationRule("student_id_format");
        columnConfigs.get("student_id").setDescription("学生学号，格式：202301001");

        columnConfigs.put("student_name", new ColumnConfig("姓名", 1, 10));

        columnConfigs.put("class_name", new ColumnConfig("班级", 2, 12));

        columnConfigs.put("attendance_date", new ColumnConfig("考勤日期", 3, 12));

        columnConfigs.put("attendance_time", new ColumnConfig("考勤时间", 4, 10));

        columnConfigs.put("status", new ColumnConfig("状态", 5, 8));
        ValidationRule statusRule = getValidationRule("status_values");
        if (statusRule != null) {
            statusRule.setAllowedValues(Arrays.asList("正常", "迟到", "早退", "缺勤", "请假"));
        }

        columnConfigs.put("location", new ColumnConfig("位置", 6, 15));

        columnConfigs.put("qr_code", new ColumnConfig("二维码", 7, 20));

        columnConfigs.put("remark", new ColumnConfig("备注", 8, 20));
        columnConfigs.get("remark").setRequired(false);
    }

    /**
     * 初始化验证规则
     */
    private void initValidationRules() {
        // 学号格式验证（示例：202301001）
        ValidationRule studentIdRule = new ValidationRule(
                "student_id_format",
                "^\\d{9}$",
                "学号必须是9位数字"
        );
        validationRules.put("student_id_format", studentIdRule);

        // 姓名验证（2-10个汉字）
        ValidationRule nameRule = new ValidationRule(
                "chinese_name",
                "^[\\u4e00-\\u9fa5]{2,10}$",
                "姓名必须是2-10个汉字"
        );
        validationRules.put("chinese_name", nameRule);

        // 时间格式验证
        ValidationRule timeRule = new ValidationRule(
                "time_format",
                "^([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$",
                "时间格式必须为HH:MM:SS"
        );
        validationRules.put("time_format", timeRule);

        // 日期格式验证
        ValidationRule dateRule = new ValidationRule(
                "date_format",
                "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$",
                "日期格式必须为YYYY-MM-DD"
        );
        validationRules.put("date_format", dateRule);

        // 状态值验证
        ValidationRule statusRule = new ValidationRule(
                "status_values",
                null,
                "状态必须是：正常、迟到、早退、缺勤、请假"
        );
        validationRules.put("status_values", statusRule);
    }

    /**
     * 获取工作表名称
     */
    public String getAttendanceSheetName() {
        return attendanceSheetName;
    }

    @SuppressWarnings("unused")
    public void setAttendanceSheetName(String attendanceSheetName) {
        this.attendanceSheetName = attendanceSheetName;
    }

    @SuppressWarnings("unused")
    public String getStudentSheetName() {
        return studentSheetName;
    }

    @SuppressWarnings("unused")
    public void setStudentSheetName(String studentSheetName) {
        this.studentSheetName = studentSheetName;
    }

    @SuppressWarnings("unused")
    public String getSummarySheetName() {
        return summarySheetName;
    }

    @SuppressWarnings("unused")
    public void setSummarySheetName(String summarySheetName) {
        this.summarySheetName = summarySheetName;
    }

    /**
     * 获取列配置
     */
    @SuppressWarnings("unused")
    public ColumnConfig getColumnConfig(String columnKey) {
        return columnConfigs.get(columnKey);
    }

    @SuppressWarnings("unused")
    public Map<String, ColumnConfig> getAllColumnConfigs() {
        return Collections.unmodifiableMap(columnConfigs);
    }

    @SuppressWarnings("unused")
    public void addColumnConfig(String key, ColumnConfig config) {
        columnConfigs.put(key, config);
    }

    /**
     * 获取验证规则
     */
    public ValidationRule getValidationRule(String ruleName) {
        return validationRules.get(ruleName);
    }

    @SuppressWarnings("unused")
    public void addValidationRule(String name, ValidationRule rule) {
        validationRules.put(name, rule);
    }

    /**
     * 文件路径管理
     */
    @SuppressWarnings("unused")
    public String getTemplatePath() {
        return templatePath;
    }

    @SuppressWarnings("unused")
    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }

    @SuppressWarnings("unused")
    public String getExportPath() {
        return exportPath;
    }

    @SuppressWarnings("unused")
    public void setExportPath(String exportPath) {
        this.exportPath = exportPath;
    }

    @SuppressWarnings("unused")
    public String getBackupPath() {
        return backupPath;
    }

    @SuppressWarnings("unused")
    public void setBackupPath(String backupPath) {
        this.backupPath = backupPath;
    }

    @SuppressWarnings("unused")
    public String getTemplateVersion() {
        return templateVersion;
    }

    @SuppressWarnings("unused")
    public void setTemplateVersion(String templateVersion) {
        this.templateVersion = templateVersion;
    }

    @SuppressWarnings("unused")
    public String getTemplateName() {
        return templateName;
    }

    @SuppressWarnings("unused")
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    /**
     * 获取完整模板路径
     */
    public String getTemplateFilePath() {
        return templatePath + File.separator + templateName;
    }

    /**
     * 获取导出文件名
     */
    @SuppressWarnings("unused")
    public String generateExportFileName() {
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return "attendance_" + timestamp + ".xlsx";
    }

    /**
     * 获取所有配置信息
     */
    @SuppressWarnings("unused")
    public Map<String, Object> getAllConfig() {
        Map<String, Object> config = new LinkedHashMap<>();

        config.put("attendanceSheetName", attendanceSheetName);
        config.put("studentSheetName", studentSheetName);
        config.put("summarySheetName", summarySheetName);

        config.put("templatePath", templatePath);
        config.put("exportPath", exportPath);
        config.put("backupPath", backupPath);
        config.put("templateVersion", templateVersion);
        config.put("templateName", templateName);

        config.put("columnConfigs", columnConfigs);
        config.put("validationRules", validationRules);

        return config;
    }

    /**
     * 验证数据
     */
    public boolean validateData(String columnKey, String value) {
        ColumnConfig columnConfig = columnConfigs.get(columnKey);
        if (columnConfig == null) {
            return true; // 如果没有配置验证规则，则通过
        }

        String ruleName = columnConfig.getValidationRule();
        if (ruleName == null || ruleName.isEmpty()) {
            return true; // 如果没有配置验证规则，则通过
        }

        ValidationRule rule = validationRules.get(ruleName);
        if (rule == null) {
            return true; // 如果规则不存在，则通过
        }

        return rule.validate(value);
    }

    /**
     * 获取验证错误信息
     */
    @SuppressWarnings("unused")
    public String getValidationErrorMessage(String columnKey, String value) {
        ColumnConfig columnConfig = columnConfigs.get(columnKey);
        if (columnConfig == null) {
            return null;
        }

        String ruleName = columnConfig.getValidationRule();
        if (ruleName == null || ruleName.isEmpty()) {
            return null;
        }

        ValidationRule rule = validationRules.get(ruleName);
        if (rule == null) {
            return null;
        }

        if (!rule.validate(value)) {
            return rule.getErrorMessage();
        }

        return null;
    }

    /**
     * 释放资源
     */
    public void dispose() {
        // 如果有需要清理的资源，在这里清理
    }

    /**
     * 显式资源清理方法
     */
    public void cleanup() {
        dispose();
    }
}