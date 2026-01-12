package com.qrcode.attendance.main;

import com.qrcode.attendance.gui.MainWindow;
import com.qrcode.attendance.util.LoggerUtil;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.logging.Logger;

/**
 * 系统托盘管理类
 * 为Windows系统添加托盘图标功能，提供最小化到托盘、系统通知等功能
 *
 * @author QR签到系统开发团队
 * @version 1.0.0
 * @since 2023-01-01
 */
public class SystemTrayManager {

    // 日志记录器
    private static final Logger LOGGER = LoggerUtil.getLogger(SystemTrayManager.class);

    // 系统托盘图标相关对象
    private TrayIcon trayIcon;      // 托盘图标
    private SystemTray systemTray;  // 系统托盘
    private final MainWindow mainWindow;  // 主窗口引用 - 添加final修饰符

    // 托盘图标图像
    private Image trayImage;        // 默认图标
    private Image trayImageNew;     // 新消息图标

    // 托盘状态
    private boolean isInTray = false;      // 是否在托盘区域
    private int notificationCount = 0;     // 未读通知数量

    /**
     * 构造函数
     *
     * @param mainWindow 主窗口实例，用于最小化和恢复操作
     */
    public SystemTrayManager(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        loadTrayImages(); // 加载托盘图标
    }

    /**
     * 加载托盘图标图片
     * 从资源文件加载或创建默认图标
     */
    private void loadTrayImages() {
        try {
            ClassLoader classLoader = getClass().getClassLoader();

            // 尝试从资源文件加载默认图标
            URL defaultIconUrl = classLoader.getResource("icons/tray_icon.png");
            if (defaultIconUrl != null) {
                trayImage = Toolkit.getDefaultToolkit().getImage(defaultIconUrl);
            } else {
                // 资源文件不存在时，创建程序绘制的图标
                trayImage = createDefaultTrayImage();
            }

            // 尝试从资源文件加载新消息图标
            URL newIconUrl = classLoader.getResource("icons/tray_icon_new.png");
            if (newIconUrl != null) {
                trayImageNew = Toolkit.getDefaultToolkit().getImage(newIconUrl);
            } else {
                // 创建红色的新消息图标
                trayImageNew = createDefaultTrayImage(Color.RED);
            }

        } catch (Exception e) {
            LOGGER.warning("加载托盘图标失败: " + e.getMessage());

            // 使用程序绘制的图标作为后备
            trayImage = createDefaultTrayImage();
            trayImageNew = createDefaultTrayImage(Color.RED);
        }
    }

    /**
     * 创建默认托盘图标（钢蓝色）
     *
     * @return 生成的图标图像
     */
    private Image createDefaultTrayImage() {
        return createDefaultTrayImage(new Color(70, 130, 180)); // 钢蓝色
    }

    /**
     * 创建默认托盘图标（指定颜色）
     *
     * @param color 图标背景色
     * @return 生成的图标图像
     */
    private Image createDefaultTrayImage(Color color) {
        int size = 16; // 托盘图标标准大小

        // 创建ARGB格式的图像
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(
                size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = image.createGraphics();

        // 启用抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 绘制圆形背景
        g2d.setColor(color);
        g2d.fillOval(0, 0, size, size);

        // 绘制字母Q（表示QR签到）
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "Q";

        // 居中绘制文本
        int x = (size - fm.stringWidth(text)) / 2;
        int y = (size - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(text, x, y);

        g2d.dispose(); // 释放图形资源
        return image;
    }

    /**
     * 创建并显示系统托盘图标
     * 如果系统不支持托盘，则记录警告日志
     */
    public void createTrayIcon() {
        // 检查系统是否支持托盘
        if (!SystemTray.isSupported()) {
            LOGGER.warning("系统托盘不支持");
            return;
        }

        try {
            // 获取系统托盘实例
            systemTray = SystemTray.getSystemTray();

            // 创建右键弹出菜单
            PopupMenu popupMenu = createPopupMenu();

            // 创建托盘图标
            trayIcon = new TrayIcon(trayImage, "QR签到系统", popupMenu);
            trayIcon.setImageAutoSize(true); // 自动调整图标大小

            // 添加事件监听器
            addTrayIconListeners();

            // 将图标添加到系统托盘
            systemTray.add(trayIcon);

            LOGGER.info("系统托盘图标已创建");

        } catch (Exception e) {
            LOGGER.severe("创建系统托盘图标失败: " + e.getMessage());
        }
    }

    /**
     * 创建托盘图标右键菜单
     *
     * @return 配置好的弹出菜单
     */
    private PopupMenu createPopupMenu() {
        PopupMenu popupMenu = new PopupMenu();

        // 1. 显示主窗口菜单项
        MenuItem showItem = new MenuItem("显示主窗口");
        showItem.addActionListener(e -> restoreWindow());
        popupMenu.add(showItem);

        popupMenu.addSeparator(); // 分隔线

        // 2. 暂停/恢复签到菜单项
        MenuItem toggleItem = new MenuItem("暂停签到");
        toggleItem.addActionListener(e -> toggleAttendance());
        popupMenu.add(toggleItem);

        // 3. 生成二维码菜单项
        MenuItem qrItem = new MenuItem("生成签到二维码");
        qrItem.addActionListener(e -> generateQRCode());
        popupMenu.add(qrItem);

        popupMenu.addSeparator();

        // 4. 通知菜单项（显示通知计数）
        MenuItem notificationsItem = new MenuItem("查看通知 (" + notificationCount + ")");
        notificationsItem.setEnabled(notificationCount > 0);
        notificationsItem.addActionListener(e -> showNotifications());
        popupMenu.add(notificationsItem);

        popupMenu.addSeparator();

        // 5. 退出程序菜单项
        MenuItem exitItem = new MenuItem("退出");
        exitItem.addActionListener(e -> exitApplication());
        popupMenu.add(exitItem);

        return popupMenu;
    }

    /**
     * 为托盘图标添加事件监听器
     * 处理鼠标点击和双击事件
     */
    private void addTrayIconListeners() {
        // 鼠标点击事件监听器
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 左键单击：切换窗口显示状态
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (mainWindow != null && mainWindow.isVisible()) {
                        minimizeToTray(); // 最小化到托盘
                    } else {
                        restoreWindow(); // 恢复窗口
                    }
                }
                // 中键单击：显示帮助提示
                else if (e.getButton() == MouseEvent.BUTTON2) {
                    showNotification("QR签到系统", "双击显示主窗口，右键查看更多选项");
                }
            }
        });

        // 双击事件监听器（直接恢复窗口）
        trayIcon.addActionListener(e -> restoreWindow());
    }

    /***************************** 窗口管理 *****************************/

    /**
     * 最小化窗口到系统托盘
     */
    public void minimizeToTray() {
        if (mainWindow != null) {
            mainWindow.setVisible(false); // 隐藏窗口
            isInTray = true;             // 更新状态

            // 显示通知提示
            showNotification("QR签到系统", "程序已最小化到系统托盘");

            LOGGER.info("窗口已最小化到系统托盘");
        }
    }

    /**
     * 从系统托盘恢复窗口
     */
    public void restoreWindow() {
        if (mainWindow != null) {
            // 显示并激活窗口
            mainWindow.setVisible(true);
            mainWindow.setExtendedState(JFrame.NORMAL); // 恢复默认状态
            mainWindow.toFront();    // 置于最前
            mainWindow.requestFocus(); // 获取焦点

            isInTray = false; // 更新状态

            // 恢复默认托盘图标
            restoreDefaultTrayIcon();

            LOGGER.info("窗口已从系统托盘恢复");
        }
    }

    /***************************** 通知功能 *****************************/

    /**
     * 显示信息类型的气泡通知
     *
     * @param title 通知标题
     * @param message 通知内容
     */
    public void showNotification(String title, String message) {
        if (trayIcon != null) {
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
        }
    }

    /**
     * 显示警告类型的气泡通知
     *
     * @param title 通知标题
     * @param message 通知内容
     */
    public void showWarning(String title, String message) {
        if (trayIcon != null) {
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.WARNING);
        }
    }

    /**
     * 显示错误类型的气泡通知
     *
     * @param title 通知标题
     * @param message 通知内容
     */
    public void showError(String title, String message) {
        if (trayIcon != null) {
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.ERROR);
        }
    }

    /***************************** 图标状态管理 *****************************/

    /**
     * 更新托盘图标为有新消息的状态
     */
    public void updateTrayIconForNewNotification() {
        if (trayIcon != null && trayImageNew != null) {
            trayIcon.setImage(trayImageNew); // 切换图标
            notificationCount++;             // 增加计数
            updateNotificationMenu();        // 更新菜单
        }
    }

    /**
     * 恢复默认托盘图标
     */
    public void restoreDefaultTrayIcon() {
        if (trayIcon != null && trayImage != null) {
            trayIcon.setImage(trayImage); // 恢复默认图标
            notificationCount = 0;        // 重置计数
            updateNotificationMenu();     // 更新菜单
        }
    }

    /**
     * 更新通知菜单项
     */
    private void updateNotificationMenu() {
        // 重新创建菜单以更新通知计数
        if (systemTray != null && trayIcon != null) {
            try {
                systemTray.remove(trayIcon);
                PopupMenu popupMenu = createPopupMenu();
                trayIcon.setPopupMenu(popupMenu);
                systemTray.add(trayIcon);
            } catch (Exception e) {
                LOGGER.warning("更新通知菜单失败: " + e.getMessage());
            }
        }
    }

    /***************************** 菜单项动作 *****************************/

    /**
     * 切换签到状态（暂停/恢复）
     */
    private void toggleAttendance() {
        AttendanceApplication app = AttendanceApplication.getInstance();

        if (app.isPaused()) {
            app.resume(); // 恢复签到
            showNotification("签到恢复", "签到功能已恢复");
        } else {
            app.pause(); // 暂停签到
            showNotification("签到暂停", "签到功能已暂停");
        }
    }

    /**
     * 生成签到二维码
     */
    private void generateQRCode() {
        if (mainWindow != null) {
            // 调用主窗口的二维码生成方法
            mainWindow.generateQRCode();
            showNotification("二维码生成", "签到二维码已生成");
        }
    }

    /**
     * 显示通知列表
     */
    private void showNotifications() {
        // 这里可以实现显示通知列表的逻辑
        JOptionPane.showMessageDialog(null,
                "您有 " + notificationCount + " 条未读通知",
                "系统通知",
                JOptionPane.INFORMATION_MESSAGE);

        // 查看通知后恢复默认图标
        restoreDefaultTrayIcon();
    }

    /**
     * 退出应用程序
     */
    private void exitApplication() {
        // 调用应用程序主控制器的退出处理
        AttendanceApplication.getInstance().handleWindowClosing();
    }

    /***************************** 状态查询和清理 *****************************/

    /**
     * 检查窗口是否在托盘区域
     *
     * @return true 如果窗口已最小化到托盘
     */
    public boolean isInTray() {
        return isInTray;
    }

    /**
     * 清理托盘图标资源
     * 应用程序退出前调用
     */
    public void cleanup() {
        if (systemTray != null && trayIcon != null) {
            systemTray.remove(trayIcon); // 从托盘移除图标
            LOGGER.info("系统托盘图标已清理");
        }
    }
}