package com.qrcode.attendance.gui.panel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class AttendanceControlPanel extends JPanel {
    private JButton btnStartAttendance;
    private JButton btnStopAttendance;
    private JButton btnForceStop;
    private JLabel lblAttendanceStatus;
    private JLabel lblTotalStudents;
    private JLabel lblSignedStudents;
    private JLabel lblRemainingTime;
    private JProgressBar progressBar;
    private JSpinner spDuration;
    private JSpinner spQRValidity;
    private JTextArea logArea;
    private Timer attendanceTimer;
    private int remainingSeconds = 0;

    public AttendanceControlPanel() {
        initComponents();
        setupLayout();
    }

    private void initComponents() {
        btnStartAttendance = new JButton("开始签到");
        btnStartAttendance.setIcon(new ImageIcon("icons/start.png"));
        btnStartAttendance.setBackground(new Color(76, 175, 80));
        btnStartAttendance.setForeground(Color.WHITE);

        btnStopAttendance = new JButton("结束签到");
        btnStopAttendance.setIcon(new ImageIcon("icons/stop.png"));
        btnStopAttendance.setBackground(new Color(244, 67, 54));
        btnStopAttendance.setForeground(Color.WHITE);
        btnStopAttendance.setEnabled(false);

        btnForceStop = new JButton("强制结束");
        btnForceStop.setIcon(new ImageIcon("icons/force_stop.png"));
        btnForceStop.setBackground(new Color(255, 152, 0));
        btnForceStop.setForeground(Color.WHITE);
        btnForceStop.setEnabled(false);

        lblAttendanceStatus = new JLabel("签到状态: 未开始");
        lblAttendanceStatus.setFont(new Font("微软雅黑", Font.BOLD, 16));

        lblTotalStudents = new JLabel("总人数: 0");
        lblSignedStudents = new JLabel("已签到: 0");
        lblRemainingTime = new JLabel("剩余时间: --:--");

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);

        spDuration = new JSpinner(new SpinnerNumberModel(30, 5, 120, 5));
        spQRValidity = new JSpinner(new SpinnerNumberModel(60, 10, 300, 10));

        logArea = new JTextArea(10, 40);
        logArea.setEditable(false);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // 顶部控制面板
        JPanel controlPanel = new JPanel(new BorderLayout(10, 10));
        controlPanel.setBorder(new TitledBorder("签到控制"));

        // 控制按钮区域
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.add(btnStartAttendance);
        buttonPanel.add(btnStopAttendance);
        buttonPanel.add(btnForceStop);

        // 参数设置区域
        JPanel paramPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        paramPanel.setBorder(new TitledBorder("参数设置"));

        paramPanel.add(new JLabel("签到时长(分钟):"));
        paramPanel.add(spDuration);
        paramPanel.add(new JLabel("二维码有效期(秒):"));
        paramPanel.add(spQRValidity);

        controlPanel.add(buttonPanel, BorderLayout.NORTH);
        controlPanel.add(paramPanel, BorderLayout.CENTER);

        add(controlPanel, BorderLayout.NORTH);

        // 中间状态显示区域
        JPanel statusPanel = new JPanel(new BorderLayout(10, 10));
        statusPanel.setBorder(new TitledBorder("签到状态"));

        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        statsPanel.add(lblAttendanceStatus);
        statsPanel.add(lblRemainingTime);
        statsPanel.add(lblTotalStudents);
        statsPanel.add(lblSignedStudents);

        statusPanel.add(statsPanel, BorderLayout.NORTH);
        statusPanel.add(progressBar, BorderLayout.CENTER);

        add(statusPanel, BorderLayout.CENTER);

        // 底部日志区域
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(new TitledBorder("签到日志"));

        JScrollPane scrollPane = new JScrollPane(logArea);
        JPanel logButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnViewRecords = new JButton("查看记录");
        btnViewRecords.addActionListener(e -> viewAttendanceRecords());
        JButton btnClearLog = new JButton("清空日志");
        btnClearLog.addActionListener(e -> logArea.setText(""));

        logButtonPanel.add(btnViewRecords);
        logButtonPanel.add(btnClearLog);

        logPanel.add(scrollPane, BorderLayout.CENTER);
        logPanel.add(logButtonPanel, BorderLayout.SOUTH);

        add(logPanel, BorderLayout.SOUTH);

        // 添加事件监听器
        setupEventListeners();
    }

    private void setupEventListeners() {
        btnStartAttendance.addActionListener(e -> startAttendance());
        btnStopAttendance.addActionListener(e -> stopAttendance());
        btnForceStop.addActionListener(e -> forceStopAttendance());
    }

    private void startAttendance() {
        int duration = (Integer) spDuration.getValue();
        int qrValidity = (Integer) spQRValidity.getValue();

        remainingSeconds = duration * 60;

        lblAttendanceStatus.setText("签到状态: 进行中");
        lblAttendanceStatus.setForeground(Color.GREEN);
        lblTotalStudents.setText("总人数: 50"); // 模拟数据
        lblSignedStudents.setText("已签到: 0");

        btnStartAttendance.setEnabled(false);
        btnStopAttendance.setEnabled(true);
        btnForceStop.setEnabled(true);

        // 启动计时器
        if (attendanceTimer != null) {
            attendanceTimer.cancel();
        }

        attendanceTimer = new Timer(true);
        attendanceTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    remainingSeconds--;
                    updateRemainingTime();

                    if (remainingSeconds <= 0) {
                        stopAttendance();
                    }

                    // 模拟签到进度
                    int signed = (int)(Math.random() * 50);
                    lblSignedStudents.setText("已签到: " + signed);
                    progressBar.setValue((int)((signed / 50.0) * 100));

                    // 添加日志
                    if (Math.random() > 0.7) {
                        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
                        logArea.append("[" + time + "] 学生" + (int)(Math.random() * 1000) + "签到成功\n");
                    }
                });
            }
        }, 1000, 1000);

        logArea.append("[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] 签到开始，时长" + duration + "分钟\n");
    }

    private void stopAttendance() {
        if (attendanceTimer != null) {
            attendanceTimer.cancel();
            attendanceTimer = null;
        }

        lblAttendanceStatus.setText("签到状态: 已结束");
        lblAttendanceStatus.setForeground(Color.RED);
        lblRemainingTime.setText("剩余时间: 00:00");

        btnStartAttendance.setEnabled(true);
        btnStopAttendance.setEnabled(false);
        btnForceStop.setEnabled(false);

        logArea.append("[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] 签到正常结束\n");
    }

    private void forceStopAttendance() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要强制结束签到吗？未签到的学生将标记为缺勤。",
                "强制结束确认",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (attendanceTimer != null) {
                attendanceTimer.cancel();
                attendanceTimer = null;
            }

            lblAttendanceStatus.setText("签到状态: 强制结束");
            lblAttendanceStatus.setForeground(Color.ORANGE);
            lblRemainingTime.setText("剩余时间: --:--");

            btnStartAttendance.setEnabled(true);
            btnStopAttendance.setEnabled(false);
            btnForceStop.setEnabled(false);

            logArea.append("[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] 签到被强制结束\n");
        }
    }

    private void updateRemainingTime() {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        lblRemainingTime.setText(String.format("剩余时间: %02d:%02d", minutes, seconds));
    }

    private void viewAttendanceRecords() {
        JOptionPane.showMessageDialog(this, "查看签到记录功能", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (attendanceTimer != null) {
            attendanceTimer.cancel();
        }
    }
}