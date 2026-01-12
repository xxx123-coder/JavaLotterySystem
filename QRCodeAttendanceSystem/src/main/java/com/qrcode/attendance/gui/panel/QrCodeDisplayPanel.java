package com.qrcode.attendance.gui.panel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import javax.imageio.ImageIO;
import javax.swing.filechooser.FileNameExtensionFilter;

public class QrCodeDisplayPanel extends JPanel {
    private JLabel qrCodeLabel;
    private JLabel lblInfo;
    private JLabel lblCountdown;
    private JButton btnRefresh;
    private JButton btnSave;
    private JButton btnPrint;
    private JSlider zoomSlider;
    private Timer refreshTimer;
    private int countdownSeconds = 60;
    private double zoomFactor = 1.0;

    public QrCodeDisplayPanel() {
        initComponents();
        setupLayout();
        generateQRCode();
        // 这里移除错误的 setupTimer() 调用，已经在 generateQRCode() 中调用 startCountdown()
    }

    private void initComponents() {
        qrCodeLabel = new JLabel();
        qrCodeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        qrCodeLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));

        lblInfo = new JLabel("签到信息加载中...", SwingConstants.CENTER);
        lblInfo.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        lblCountdown = new JLabel("二维码有效期: 60秒", SwingConstants.CENTER);
        lblCountdown.setFont(new Font("微软雅黑", Font.BOLD, 16));
        lblCountdown.setForeground(Color.RED);

        btnRefresh = new JButton("刷新二维码");

        btnSave = new JButton("保存二维码");

        btnPrint = new JButton("打印二维码");

        zoomSlider = new JSlider(50, 200, 100);
        zoomSlider.setMajorTickSpacing(50);
        zoomSlider.setPaintTicks(true);
        zoomSlider.setPaintLabels(true);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // 顶部信息显示
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("签到二维码", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        topPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        infoPanel.add(lblInfo);
        infoPanel.add(lblCountdown);
        topPanel.add(infoPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        // 中间二维码显示区域
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new TitledBorder("二维码显示区"));

        centerPanel.add(qrCodeLabel, BorderLayout.CENTER);

        // 缩放控制
        JPanel zoomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        zoomPanel.add(new JLabel("缩放:"));
        zoomPanel.add(zoomSlider);

        centerPanel.add(zoomPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        // 底部按钮区域
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnSave);
        buttonPanel.add(btnPrint);

        add(buttonPanel, BorderLayout.SOUTH);

        // 添加事件监听器
        setupEventListeners();
    }

    private void setupEventListeners() {
        btnRefresh.addActionListener(e -> generateQRCode());

        btnSave.addActionListener(e -> saveQRCode());

        btnPrint.addActionListener(e -> printQRCode());

        zoomSlider.addChangeListener(e -> {
            zoomFactor = zoomSlider.getValue() / 100.0;
            updateQRCodeDisplay();
        });
    }

    private void generateQRCode() {
        // 这里应该生成实际的二维码
        // 现在生成一个模拟的二维码图片
        int size = (int)(300 * zoomFactor);
        BufferedImage qrImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = qrImage.createGraphics();

        // 绘制背景
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, size, size);

        // 绘制模拟二维码图案
        g2d.setColor(Color.BLACK);
        int blockSize = size / 30;
        for (int i = 0; i < 30; i++) {
            for (int j = 0; j < 30; j++) {
                if (Math.random() > 0.5) {
                    g2d.fillRect(i * blockSize, j * blockSize, blockSize, blockSize);
                }
            }
        }

        // 绘制定位标记
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, blockSize * 7, blockSize * 7);
        g2d.fillRect(size - blockSize * 7, 0, blockSize * 7, blockSize * 7);
        g2d.fillRect(0, size - blockSize * 7, blockSize * 7, blockSize * 7);

        g2d.setColor(Color.WHITE);
        g2d.fillRect(blockSize, blockSize, blockSize * 5, blockSize * 5);
        g2d.fillRect(size - blockSize * 6, blockSize, blockSize * 5, blockSize * 5);
        g2d.fillRect(blockSize, size - blockSize * 6, blockSize * 5, blockSize * 5);

        g2d.dispose();

        ImageIcon icon = new ImageIcon(qrImage);
        qrCodeLabel.setIcon(icon);

        // 更新信息
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String sessionId = "ATT" + System.currentTimeMillis() % 10000;
        lblInfo.setText("课程: 计算机科学 | 时间: " + time + " | 会话ID: " + sessionId);

        // 重置倒计时
        countdownSeconds = 60;
        lblCountdown.setText("二维码有效期: " + countdownSeconds + "秒");

        // 启动倒计时
        startCountdown();
    }

    private void updateQRCodeDisplay() {
        generateQRCode();
    }

    private void startCountdown() {
        // 如果已有计时器，先取消
        if (refreshTimer != null) {
            refreshTimer.cancel();
        }

        refreshTimer = new Timer(true);
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    countdownSeconds--;
                    lblCountdown.setText("二维码有效期: " + countdownSeconds + "秒");

                    if (countdownSeconds <= 10) {
                        lblCountdown.setForeground(Color.RED);
                    } else if (countdownSeconds <= 30) {
                        lblCountdown.setForeground(Color.ORANGE);
                    }

                    if (countdownSeconds <= 0) {
                        refreshTimer.cancel();
                        lblCountdown.setText("二维码已过期");
                        lblCountdown.setForeground(Color.GRAY);
                    }
                });
            }
        }, 1000, 1000);
    }

    private void saveQRCode() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("PNG图片 (*.png)", "png"));
        fileChooser.setSelectedFile(new File("qr_code_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".png"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                if (!file.getName().toLowerCase().endsWith(".png")) {
                    file = new File(file.getParentFile(), file.getName() + ".png");
                }

                ImageIcon icon = (ImageIcon) qrCodeLabel.getIcon();
                if (icon != null) {
                    BufferedImage image = new BufferedImage(
                            icon.getIconWidth(),
                            icon.getIconHeight(),
                            BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2d = image.createGraphics();
                    icon.paintIcon(null, g2d, 0, 0);
                    g2d.dispose();

                    ImageIO.write(image, "PNG", file);
                    JOptionPane.showMessageDialog(this, "二维码保存成功: " + file.getName(), "成功", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "保存失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void printQRCode() {
        JOptionPane.showMessageDialog(this, "打印功能", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (refreshTimer != null) {
            refreshTimer.cancel();
            refreshTimer = null;
        }
    }
}