package com.qrcode.attendance.gui.component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * 状态指示灯组件
 */
public class StatusIndicator extends JComponent {
    private Status currentStatus = Status.UNKNOWN;
    private String statusText = "未知状态";
    private final Timer blinkTimer;
    private boolean blinking = false;
    private boolean showLight = true; // 控制闪烁时是否显示灯光
    private final List<StatusChangeListener> listeners = new ArrayList<>(); // 添加final修饰符

    // 状态枚举
    public enum Status {
        OK(Color.GREEN, "正常"),
        WARNING(Color.YELLOW, "警告"),
        ERROR(Color.RED, "错误"),
        UNKNOWN(Color.GRAY, "未知");

        private final Color color;
        private final String description;

        Status(Color color, String description) {
            this.color = color;
            this.description = description;
        }

        public Color getColor() {
            return color;
        }

        public String getDescription() {
            return description;
        }
    }

    // 状态变化监听器接口 - 设为包私有
    interface StatusChangeListener {
        void onStatusChanged(Status oldStatus, Status newStatus, String message);
    }

    public StatusIndicator() {
        initComponent();
        setupToolTip();
        blinkTimer = setupBlinkTimer(); // 修改为局部变量初始化
        autoDetectStatus();
    }

    private void initComponent() {
        setPreferredSize(new Dimension(200, 60));
        setMinimumSize(new Dimension(150, 40));
        setOpaque(true);
        setBackground(Color.WHITE);

        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
    }

    private void setupToolTip() {
        ToolTipManager.sharedInstance().registerComponent(this);
        updateToolTip();
    }

    private Timer setupBlinkTimer() {
        Timer timer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (blinking) {
                    showLight = !showLight;
                    repaint();
                }
            }
        });
        return timer;
    }

    /**
     * 自动检测状态（模拟）
     */
    private void autoDetectStatus() {
        Timer detectionTimer = new Timer(5000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double random = Math.random();

                if (random < 0.7) {
                    setStatus(Status.OK, "系统运行正常");
                } else if (random < 0.9) {
                    setStatus(Status.WARNING, "系统负载较高");
                } else {
                    setStatus(Status.ERROR, "检测到异常");
                }
            }
        });

        detectionTimer.setInitialDelay(1000);
        detectionTimer.start();
    }

    /**
     * 设置状态
     */
    public void setStatus(Status status, String message) {
        if (this.currentStatus != status || !this.statusText.equals(message)) {
            Status oldStatus = this.currentStatus;
            this.currentStatus = status;
            this.statusText = message;

            updateBlinking();
            updateToolTip();
            fireStatusChange(oldStatus, status);
            repaint();
        }
    }

    /**
     * 更新闪烁状态
     */
    private void updateBlinking() {
        if (currentStatus == Status.ERROR) {
            startBlinking();
            blinkTimer.setDelay(500);
        } else if (currentStatus == Status.WARNING) {
            startBlinking();
            blinkTimer.setDelay(1000); // 慢闪
        } else {
            stopBlinking();
            showLight = true; // 确保正常状态下显示灯光
        }
    }

    /**
     * 开始闪烁
     */
    public void startBlinking() {
        blinking = true;
        showLight = true; // 重置显示状态
        if (!blinkTimer.isRunning()) {
            blinkTimer.start();
        }
    }

    /**
     * 停止闪烁
     */
    public void stopBlinking() {
        blinking = false;
        if (blinkTimer.isRunning()) {
            blinkTimer.stop();
        }
        repaint();
    }

    /**
     * 更新工具提示
     */
    private void updateToolTip() {
        setToolTipText(String.format("<html>%s<br>状态: %s<br>%s</html>",
                currentStatus.getDescription(),
                currentStatus.name(),
                statusText));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // 绘制背景
        g2d.setColor(getBackground());
        g2d.fillRect(0, 0, width, height);

        // 绘制指示灯
        int indicatorSize = Math.min(height - 20, 30);
        int indicatorX = 10;
        int indicatorY = (height - indicatorSize) / 2;

        // 修复条件判断：只有当不闪烁或者闪烁且应该显示时才绘制
        if (!blinking || showLight) {
            // 绘制指示灯外圈
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillOval(indicatorX, indicatorY, indicatorSize, indicatorSize);

            // 绘制指示灯内圈
            int innerSize = indicatorSize - 6;
            int innerX = indicatorX + 3;
            int innerY = indicatorY + 3;

            g2d.setColor(currentStatus.getColor());
            g2d.fillOval(innerX, innerY, innerSize, innerSize);

            // 添加光晕效果
            GradientPaint gradient = new GradientPaint(
                    innerX, innerY, currentStatus.getColor().brighter(),
                    innerX + innerSize, innerY + innerSize, currentStatus.getColor().darker()
            );
            g2d.setPaint(gradient);
            g2d.fillOval(innerX, innerY, innerSize, innerSize);
        }

        // 绘制状态文本
        g2d.setColor(Color.BLACK);
        g2d.setFont(getFont().deriveFont(Font.BOLD, 14f));

        int textX = indicatorX + indicatorSize + 15;
        int textY = height / 2 + 5;

        // 绘制状态描述
        g2d.drawString(currentStatus.getDescription(), textX, textY - 10);

        // 绘制详细状态
        g2d.setFont(getFont().deriveFont(Font.PLAIN, 12f));
        g2d.setColor(Color.DARK_GRAY);

        FontMetrics fm = g2d.getFontMetrics();
        String truncatedText = truncateText(statusText, width - textX - 10, fm);
        g2d.drawString(truncatedText, textX, textY + 15);
    }

    /**
     * 截断文本以适应宽度
     */
    private String truncateText(String text, int maxWidth, FontMetrics fm) {
        if (fm.stringWidth(text) <= maxWidth) {
            return text;
        }

        while (text.length() > 3 && fm.stringWidth(text + "...") > maxWidth) {
            text = text.substring(0, text.length() - 1);
        }

        return text + "...";
    }

    /**
     * 添加状态变化监听器
     */
    public void addStatusChangeListener(StatusChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * 移除状态变化监听器
     */
    public void removeStatusChangeListener(StatusChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * 触发状态变化事件
     */
    private void fireStatusChange(Status oldStatus, Status newStatus) {
        for (StatusChangeListener listener : listeners) {
            listener.onStatusChanged(oldStatus, newStatus, statusText);
        }
    }

    /**
     * 获取当前状态
     */
    public Status getCurrentStatus() {
        return currentStatus;
    }

    /**
     * 获取状态文本
     */
    public String getStatusText() {
        return statusText;
    }
}