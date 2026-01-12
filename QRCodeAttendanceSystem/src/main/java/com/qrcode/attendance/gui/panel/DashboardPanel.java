package com.qrcode.attendance.gui.panel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class DashboardPanel extends JPanel {
    private JLabel lblTodayAttendance;
    private JLabel lblPresentCount;
    private JLabel lblAbsentCount;
    private JLabel lblLateCount;
    private JLabel lblSystemStatus;
    private JLabel lblWebServerStatus;
    private JTextArea notificationArea;
    private Timer refreshTimer;

    public DashboardPanel() {
        initComponents();
        setupLayout();
        setupTimer();
    }

    private void initComponents() {
        lblTodayAttendance = new JLabel("今日签到统计");
        lblTodayAttendance.setFont(new Font("微软雅黑", Font.BOLD, 16));

        lblPresentCount = new JLabel("已签到: 0");
        lblAbsentCount = new JLabel("未签到: 0");
        lblLateCount = new JLabel("迟到: 0");

        lblSystemStatus = new JLabel("系统状态: 正常");
        lblSystemStatus.setForeground(Color.GREEN);

        lblWebServerStatus = new JLabel("Web服务器: 运行中");
        lblWebServerStatus.setForeground(Color.GREEN);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // 顶部标题
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("系统仪表盘");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        topPanel.add(titleLabel, BorderLayout.WEST);

        JLabel timeLabel = new JLabel(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        timeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        topPanel.add(timeLabel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // 中间主内容区
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 15, 0));

        // 左侧卡片区域
        JPanel cardPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        cardPanel.setBorder(new TitledBorder("今日统计卡片"));

        JPanel card1 = createStatCard("已签到人数", "0", Color.decode("#4CAF50"));
        JPanel card2 = createStatCard("未签到人数", "0", Color.decode("#F44336"));
        JPanel card3 = createStatCard("迟到人数", "0", Color.decode("#FF9800"));
        JPanel card4 = createStatCard("出勤率", "0%", Color.decode("#2196F3"));

        cardPanel.add(card1);
        cardPanel.add(card2);
        cardPanel.add(card3);
        cardPanel.add(card4);

        // 右侧快速操作区域
        JPanel actionPanel = new JPanel(new BorderLayout(0, 10));
        actionPanel.setBorder(new TitledBorder("快速操作"));

        JPanel buttonPanel = new JPanel(new GridLayout(3, 2, 10, 10));

        JButton btnStartAttendance = new JButton("开始签到");
        btnStartAttendance.setIcon(new ImageIcon("icons/start.png"));
        btnStartAttendance.addActionListener(e -> startAttendance());

        JButton btnViewStats = new JButton("查看统计");
        btnViewStats.setIcon(new ImageIcon("icons/stats.png"));
        btnViewStats.addActionListener(e -> viewStatistics());

        JButton btnManageClasses = new JButton("班级管理");
        btnManageClasses.setIcon(new ImageIcon("icons/class.png"));

        JButton btnManageStudents = new JButton("学生管理");
        btnManageStudents.setIcon(new ImageIcon("icons/student.png"));

        JButton btnExportData = new JButton("导出数据");
        btnExportData.setIcon(new ImageIcon("icons/export.png"));

        JButton btnSettings = new JButton("系统设置");
        btnSettings.setIcon(new ImageIcon("icons/settings.png"));

        buttonPanel.add(btnStartAttendance);
        buttonPanel.add(btnViewStats);
        buttonPanel.add(btnManageClasses);
        buttonPanel.add(btnManageStudents);
        buttonPanel.add(btnExportData);
        buttonPanel.add(btnSettings);

        actionPanel.add(buttonPanel, BorderLayout.CENTER);

        centerPanel.add(cardPanel);
        centerPanel.add(actionPanel);

        add(centerPanel, BorderLayout.CENTER);

        // 底部状态和通知区域
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 15, 0));

        // 系统状态区域
        JPanel statusPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        statusPanel.setBorder(new TitledBorder("系统状态"));

        statusPanel.add(lblSystemStatus);
        statusPanel.add(lblWebServerStatus);

        JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRefresh = new JButton("刷新状态");
        btnRefresh.addActionListener(e -> refreshStatus());
        refreshPanel.add(btnRefresh);
        statusPanel.add(refreshPanel);

        // 通知区域
        JPanel notificationPanel = new JPanel(new BorderLayout());
        notificationPanel.setBorder(new TitledBorder("通知和提醒"));

        notificationArea = new JTextArea(5, 20);
        notificationArea.setEditable(false);
        notificationArea.setText("1. 今日签到将于10分钟后开始\n2. 有3个班级需要导入学生数据\n3. 系统需要更新到最新版本");
        JScrollPane scrollPane = new JScrollPane(notificationArea);

        JPanel notiButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnClearNoti = new JButton("清空通知");
        btnClearNoti.addActionListener(e -> notificationArea.setText(""));
        notiButtonPanel.add(btnClearNoti);

        notificationPanel.add(scrollPane, BorderLayout.CENTER);
        notificationPanel.add(notiButtonPanel, BorderLayout.SOUTH);

        bottomPanel.add(statusPanel);
        bottomPanel.add(notificationPanel);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private void startAttendance() {
        JOptionPane.showMessageDialog(this, "开始签到功能", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    private void viewStatistics() {
        JOptionPane.showMessageDialog(this, "查看统计功能", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshStatus() {
        // 模拟状态更新
        lblSystemStatus.setText("系统状态: 正常 (" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + ")");
        lblWebServerStatus.setText("Web服务器: 运行中");
    }

    private void setupTimer() {
        refreshTimer = new Timer(true);
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    // 更新数据
                    updateDashboardData();
                });
            }
        }, 0, 30000); // 每30秒更新一次
    }

    private void updateDashboardData() {
        // 这里应该调用业务逻辑获取实际数据
        // 现在使用模拟数据
        lblPresentCount.setText("已签到: " + (int)(Math.random() * 100));
        lblAbsentCount.setText("未签到: " + (int)(Math.random() * 50));
        lblLateCount.setText("迟到: " + (int)(Math.random() * 10));
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (refreshTimer != null) {
            refreshTimer.cancel();
        }
    }
}