package com.qrcode.attendance.gui.panel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StatisticsPanel extends JPanel {
    private JComboBox<String> cbDateRange;
    private JComboBox<String> cbClassFilter;
    private JComboBox<String> cbChartType;
    private JDateChooser dateFrom;
    private JDateChooser dateTo;

    public StatisticsPanel() {
        initComponents();
        setupLayout();
    }

    private void initComponents() {
        cbDateRange = new JComboBox<>(new String[]{"今日", "本周", "本月", "本学期", "自定义"});
        cbClassFilter = new JComboBox<>(new String[]{"所有班级", "计算机科学与技术1班", "软件工程2班", "数据科学1班"});
        cbChartType = new JComboBox<>(new String[]{"柱状图", "饼图", "折线图", "堆积图"});

        // 注意：JDateChooser需要jcalendar库，这里用JTextField代替
        dateFrom = new JDateChooser();
        dateTo = new JDateChooser();
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // 顶部筛选工具栏
        JPanel filterPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        filterPanel.setBorder(new TitledBorder("筛选条件"));

        filterPanel.add(new JLabel("时间范围:"));
        filterPanel.add(cbDateRange);
        filterPanel.add(new JLabel("班级筛选:"));
        filterPanel.add(cbClassFilter);

        filterPanel.add(new JLabel("开始日期:"));
        filterPanel.add(dateFrom);
        filterPanel.add(new JLabel("结束日期:"));
        filterPanel.add(dateTo);

        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("签到统计");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGenerate = new JButton("生成统计");
        btnGenerate.setIcon(new ImageIcon("icons/generate.png"));
        btnGenerate.addActionListener(e -> generateStatistics());

        buttonPanel.add(btnGenerate);

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
        add(filterPanel, BorderLayout.CENTER);

        // 中间图表显示区域
        JPanel chartPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        chartPanel.setBorder(new TitledBorder("统计图表"));

        // 模拟图表区域
        JPanel chart1 = createChartPanel("出勤率统计", Color.decode("#4CAF50"));
        JPanel chart2 = createChartPanel("迟到率统计", Color.decode("#FF9800"));
        JPanel chart3 = createChartPanel("班级对比", Color.decode("#2196F3"));
        JPanel chart4 = createChartPanel("时间趋势", Color.decode("#9C27B0"));

        chartPanel.add(chart1);
        chartPanel.add(chart2);
        chartPanel.add(chart3);
        chartPanel.add(chart4);

        add(chartPanel, BorderLayout.CENTER);

        // 底部数据表格和操作按钮
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));

        // 数据表格
        String[] columns = {"日期", "班级", "总人数", "出勤人数", "迟到人数", "缺勤人数", "出勤率", "迟到率"};
        Object[][] data = {
                {"2024-03-01", "计算机1班", 45, 42, 2, 1, "93.3%", "4.4%"},
                {"2024-03-01", "软件2班", 42, 40, 1, 1, "95.2%", "2.4%"},
                {"2024-03-02", "计算机1班", 45, 44, 1, 0, "97.8%", "2.2%"},
                {"2024-03-02", "软件2班", 42, 41, 0, 1, "97.6%", "0%"}
        };

        JTable dataTable = new JTable(data, columns);
        JScrollPane tableScrollPane = new JScrollPane(dataTable);
        tableScrollPane.setBorder(new TitledBorder("详细数据"));

        // 操作按钮
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton btnExportExcel = new JButton("导出Excel");
        btnExportExcel.setIcon(new ImageIcon("icons/excel.png"));
        btnExportExcel.addActionListener(e -> exportToExcel());

        JButton btnExportPDF = new JButton("导出PDF");
        btnExportPDF.setIcon(new ImageIcon("icons/pdf.png"));
        btnExportPDF.addActionListener(e -> exportToPDF());

        JButton btnPrint = new JButton("打印报告");
        btnPrint.setIcon(new ImageIcon("icons/print.png"));
        btnPrint.addActionListener(e -> printReport());

        JButton btnRefresh = new JButton("刷新");
        btnRefresh.setIcon(new ImageIcon("icons/refresh.png"));
        btnRefresh.addActionListener(e -> refreshData());

        actionPanel.add(btnExportExcel);
        actionPanel.add(btnExportPDF);
        actionPanel.add(btnPrint);
        actionPanel.add(btnRefresh);

        bottomPanel.add(tableScrollPane, BorderLayout.CENTER);
        bottomPanel.add(actionPanel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createChartPanel(String title, Color color) {
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        chartPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        titleLabel.setForeground(color);

        // 模拟图表
        JPanel chart = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();

                // 绘制简单的图表
                g2d.setColor(color);
                if (title.contains("出勤率")) {
                    // 柱状图
                    int[] values = {85, 90, 88, 92, 87, 95};
                    int barWidth = width / (values.length * 2);
                    for (int i = 0; i < values.length; i++) {
                        int barHeight = (int) (height * values[i] / 100.0);
                        g2d.fillRect(i * (barWidth * 2) + barWidth/2, height - barHeight, barWidth, barHeight);
                    }
                } else if (title.contains("饼图")) {
                    // 饼图
                    int radius = Math.min(width, height) / 2 - 10;
                    int centerX = width / 2;
                    int centerY = height / 2;

                    g2d.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2, 0, 120);
                    g2d.setColor(Color.decode("#FF9800"));
                    g2d.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2, 120, 60);
                    g2d.setColor(Color.decode("#F44336"));
                    g2d.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2, 180, 180);
                }
            }
        };

        chart.setPreferredSize(new Dimension(300, 200));

        chartPanel.add(titleLabel, BorderLayout.NORTH);
        chartPanel.add(chart, BorderLayout.CENTER);

        return chartPanel;
    }

    private void generateStatistics() {
        String dateRange = (String) cbDateRange.getSelectedItem();
        String className = (String) cbClassFilter.getSelectedItem();

        JOptionPane.showMessageDialog(this,
                "正在生成统计...\n时间范围: " + dateRange + "\n班级: " + className,
                "生成统计",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void exportToExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Excel文件 (*.xlsx)", "xlsx"));
        fileChooser.setSelectedFile(new File("签到统计_" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".xlsx"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            JOptionPane.showMessageDialog(this, "导出Excel: " + file.getName(), "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void exportToPDF() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("PDF文件 (*.pdf)", "pdf"));
        fileChooser.setSelectedFile(new File("签到统计_" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".pdf"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            JOptionPane.showMessageDialog(this, "导出PDF: " + file.getName(), "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void printReport() {
        JOptionPane.showMessageDialog(this, "打印报告功能", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshData() {
        cbDateRange.setSelectedIndex(0);
        cbClassFilter.setSelectedIndex(0);
        JOptionPane.showMessageDialog(this, "数据已刷新", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    // 内部类，用于日期选择
    class JDateChooser extends JPanel {
        private JTextField textField;

        public JDateChooser() {
            setLayout(new BorderLayout());
            textField = new JTextField(10);
            textField.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

            JButton btnCalendar = new JButton("...");
            btnCalendar.setPreferredSize(new Dimension(25, 25));
            btnCalendar.addActionListener(e -> showCalendarDialog());

            add(textField, BorderLayout.CENTER);
            add(btnCalendar, BorderLayout.EAST);
        }

        private void showCalendarDialog() {
            JOptionPane.showMessageDialog(this, "日期选择器", "提示", JOptionPane.INFORMATION_MESSAGE);
        }

        public String getDate() {
            return textField.getText();
        }

        public void setDate(String date) {
            textField.setText(date);
        }
    }
}