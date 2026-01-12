package com.qrcode.attendance.gui.panel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.Properties;

public class SettingsPanel extends JPanel {
    private JTabbedPane tabbedPane;
    private Properties settings;
    private JTextField txtServerPort;
    private JTextField txtDbUrl;
    private JTextField txtExcelPath;
    private JSpinner spAttendanceDuration;
    private JCheckBox cbAutoSave;
    private JCheckBox cbAutoStart;

    public SettingsPanel() {
        settings = new Properties();
        loadDefaultSettings();
        initComponents();
        setupLayout();
    }

    private void initComponents() {
        tabbedPane = new JTabbedPane();

        txtServerPort = new JTextField("8080");
        txtDbUrl = new JTextField("jdbc:mysql://localhost:3306/attendance");
        txtExcelPath = new JTextField("./exports");
        spAttendanceDuration = new JSpinner(new SpinnerNumberModel(30, 5, 120, 5));
        cbAutoSave = new JCheckBox("自动保存设置", true);
        cbAutoStart = new JCheckBox("启动时自动检查更新", false);
    }

    private void loadDefaultSettings() {
        settings.setProperty("server.port", "8080");
        settings.setProperty("db.url", "jdbc:mysql://localhost:3306/attendance");
        settings.setProperty("excel.path", "./exports");
        settings.setProperty("attendance.duration", "30");
        settings.setProperty("auto.save", "true");
        settings.setProperty("auto.start", "false");
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // 创建标签页
        createSystemTab();
        createWebTab();
        createExcelTab();
        createAttendanceTab();

        add(tabbedPane, BorderLayout.CENTER);

        // 底部按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        JButton btnSave = new JButton("保存设置");
        btnSave.setIcon(new ImageIcon("icons/save.png"));
        btnSave.addActionListener(e -> saveSettings());

        JButton btnReset = new JButton("恢复默认");
        btnReset.setIcon(new ImageIcon("icons/reset.png"));
        btnReset.addActionListener(e -> resetSettings());

        JButton btnImport = new JButton("导入设置");
        btnImport.setIcon(new ImageIcon("icons/import.png"));
        btnImport.addActionListener(e -> importSettings());

        JButton btnExport = new JButton("导出设置");
        btnExport.setIcon(new ImageIcon("icons/export.png"));
        btnExport.addActionListener(e -> exportSettings());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnReset);
        buttonPanel.add(btnImport);
        buttonPanel.add(btnExport);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void createSystemTab() {
        JPanel systemPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        systemPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        systemPanel.add(new JLabel("自动保存设置:"));
        systemPanel.add(cbAutoSave);

        systemPanel.add(new JLabel("启动检查更新:"));
        systemPanel.add(cbAutoStart);

        systemPanel.add(new JLabel("日志级别:"));
        JComboBox<String> cbLogLevel = new JComboBox<>(new String[]{"DEBUG", "INFO", "WARN", "ERROR"});
        systemPanel.add(cbLogLevel);

        systemPanel.add(new JLabel("最大日志文件数:"));
        JSpinner spMaxLogFiles = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
        systemPanel.add(spMaxLogFiles);

        systemPanel.add(new JLabel("数据备份路径:"));
        JTextField txtBackupPath = new JTextField("./backup");
        JButton btnBrowseBackup = new JButton("浏览...");
        btnBrowseBackup.addActionListener(e -> browseBackupPath(txtBackupPath));

        JPanel backupPanel = new JPanel(new BorderLayout());
        backupPanel.add(txtBackupPath, BorderLayout.CENTER);
        backupPanel.add(btnBrowseBackup, BorderLayout.EAST);
        systemPanel.add(backupPanel);

        JPanel tabPanel = new JPanel(new BorderLayout());
        tabPanel.add(systemPanel, BorderLayout.NORTH);
        tabbedPane.addTab("系统设置", new ImageIcon("icons/system.png"), tabPanel);
    }

    private void createWebTab() {
        JPanel webPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        webPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        webPanel.add(new JLabel("Web服务器端口:"));
        webPanel.add(txtServerPort);

        webPanel.add(new JLabel("数据库URL:"));
        webPanel.add(txtDbUrl);

        webPanel.add(new JLabel("数据库用户名:"));
        JTextField txtDbUser = new JTextField("root");
        webPanel.add(txtDbUser);

        webPanel.add(new JLabel("数据库密码:"));
        JPasswordField txtDbPassword = new JPasswordField();
        webPanel.add(txtDbPassword);

        webPanel.add(new JLabel("会话超时(分钟):"));
        JSpinner spSessionTimeout = new JSpinner(new SpinnerNumberModel(30, 5, 240, 5));
        webPanel.add(spSessionTimeout);

        webPanel.add(new JLabel("启用SSL:"));
        JCheckBox cbEnableSSL = new JCheckBox();
        webPanel.add(cbEnableSSL);

        JPanel tabPanel = new JPanel(new BorderLayout());
        tabPanel.add(webPanel, BorderLayout.NORTH);
        tabbedPane.addTab("Web设置", new ImageIcon("icons/web.png"), tabPanel);
    }

    private void createExcelTab() {
        JPanel excelPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        excelPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        excelPanel.add(new JLabel("Excel导出路径:"));
        JPanel excelPathPanel = new JPanel(new BorderLayout());
        excelPathPanel.add(txtExcelPath, BorderLayout.CENTER);
        JButton btnBrowseExcel = new JButton("浏览...");
        btnBrowseExcel.addActionListener(e -> browseExcelPath());
        excelPathPanel.add(btnBrowseExcel, BorderLayout.EAST);
        excelPanel.add(excelPathPanel);

        excelPanel.add(new JLabel("默认Excel模板:"));
        JTextField txtTemplatePath = new JTextField("./templates/template.xlsx");
        JButton btnBrowseTemplate = new JButton("浏览...");
        btnBrowseTemplate.addActionListener(e -> browseTemplatePath(txtTemplatePath));

        JPanel templatePanel = new JPanel(new BorderLayout());
        templatePanel.add(txtTemplatePath, BorderLayout.CENTER);
        templatePanel.add(btnBrowseTemplate, BorderLayout.EAST);
        excelPanel.add(templatePanel);

        excelPanel.add(new JLabel("自动导出格式:"));
        JComboBox<String> cbExportFormat = new JComboBox<>(new String[]{"xlsx", "xls", "csv"});
        excelPanel.add(cbExportFormat);

        excelPanel.add(new JLabel("包含表头:"));
        JCheckBox cbIncludeHeader = new JCheckBox("是", true);
        excelPanel.add(cbIncludeHeader);

        excelPanel.add(new JLabel("导出时压缩:"));
        JCheckBox cbCompress = new JCheckBox("是", false);
        excelPanel.add(cbCompress);

        JPanel tabPanel = new JPanel(new BorderLayout());
        tabPanel.add(excelPanel, BorderLayout.NORTH);
        tabbedPane.addTab("Excel设置", new ImageIcon("icons/excel.png"), tabPanel);
    }

    private void createAttendanceTab() {
        JPanel attendancePanel = new JPanel(new GridLayout(6, 2, 10, 10));
        attendancePanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        attendancePanel.add(new JLabel("默认签到时长(分钟):"));
        attendancePanel.add(spAttendanceDuration);

        attendancePanel.add(new JLabel("二维码有效期(秒):"));
        JSpinner spQRValidity = new JSpinner(new SpinnerNumberModel(60, 10, 300, 10));
        attendancePanel.add(spQRValidity);

        attendancePanel.add(new JLabel("迟到时间阈值(分钟):"));
        JSpinner spLateThreshold = new JSpinner(new SpinnerNumberModel(5, 1, 30, 1));
        attendancePanel.add(spLateThreshold);

        attendancePanel.add(new JLabel("自动结束签到:"));
        JCheckBox cbAutoEnd = new JCheckBox("是", true);
        attendancePanel.add(cbAutoEnd);

        attendancePanel.add(new JLabel("签到提醒:"));
        JCheckBox cbAttendanceAlert = new JCheckBox("启用提醒", true);
        attendancePanel.add(cbAttendanceAlert);

        attendancePanel.add(new JLabel("提醒提前时间(分钟):"));
        JSpinner spAlertTime = new JSpinner(new SpinnerNumberModel(5, 1, 30, 1));
        attendancePanel.add(spAlertTime);

        JPanel tabPanel = new JPanel(new BorderLayout());
        tabPanel.add(attendancePanel, BorderLayout.NORTH);
        tabbedPane.addTab("签到设置", new ImageIcon("icons/attendance.png"), tabPanel);
    }

    private void browseBackupPath(JTextField pathField) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setSelectedFile(new File(pathField.getText()));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            pathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void browseExcelPath() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setSelectedFile(new File(txtExcelPath.getText()));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtExcelPath.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void browseTemplatePath(JTextField pathField) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Excel文件 (*.xls, *.xlsx)", "xls", "xlsx"));
        fileChooser.setSelectedFile(new File(pathField.getText()));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            pathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void saveSettings() {
        // 验证设置
        try {
            int port = Integer.parseInt(txtServerPort.getText());
            if (port < 1 || port > 65535) {
                JOptionPane.showMessageDialog(this, "端口号必须在1-65535之间", "验证错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "端口号必须是数字", "验证错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 保存设置到Properties
        settings.setProperty("server.port", txtServerPort.getText());
        settings.setProperty("db.url", txtDbUrl.getText());
        settings.setProperty("excel.path", txtExcelPath.getText());
        settings.setProperty("attendance.duration", spAttendanceDuration.getValue().toString());
        settings.setProperty("auto.save", String.valueOf(cbAutoSave.isSelected()));
        settings.setProperty("auto.start", String.valueOf(cbAutoStart.isSelected()));

        JOptionPane.showMessageDialog(this,
                "设置保存成功！部分设置需要重启应用程序才能生效。\n是否现在重启？",
                "保存成功",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void resetSettings() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要恢复默认设置吗？所有自定义设置将会丢失。",
                "恢复默认确认",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            loadDefaultSettings();
            // 更新UI
            txtServerPort.setText(settings.getProperty("server.port"));
            txtDbUrl.setText(settings.getProperty("db.url"));
            txtExcelPath.setText(settings.getProperty("excel.path"));
            spAttendanceDuration.setValue(Integer.parseInt(settings.getProperty("attendance.duration")));
            cbAutoSave.setSelected(Boolean.parseBoolean(settings.getProperty("auto.save")));
            cbAutoStart.setSelected(Boolean.parseBoolean(settings.getProperty("auto.start")));

            JOptionPane.showMessageDialog(this, "已恢复默认设置", "成功", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void importSettings() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("属性文件 (*.properties)", "properties"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            JOptionPane.showMessageDialog(this, "导入设置文件: " + file.getName(), "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void exportSettings() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("属性文件 (*.properties)", "properties"));
        fileChooser.setSelectedFile(new File("attendance_settings.properties"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            JOptionPane.showMessageDialog(this, "导出设置到: " + file.getName(), "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}