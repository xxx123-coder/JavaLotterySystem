package com.qrcode.attendance.main;

import com.qrcode.attendance.gui.MainWindow;
import com.qrcode.attendance.util.LoggerUtil;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.logging.Logger;

/**
 * 系统托盘管理类
 */
public class SystemTrayManager {

    private static final Logger LOGGER = LoggerUtil.getLogger(SystemTrayManager.class);
    private TrayIcon trayIcon;
    private SystemTray systemTray;
    private final MainWindow mainWindow;
    private Image trayImage;
    private Image trayImageNew;
    private boolean isInTray = false;
    private int notificationCount = 0;

    /**
     * 构造函数
     */
    public SystemTrayManager(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        loadTrayImages();
    }

    /**
     * 加载托盘图标图片
     */
    private void loadTrayImages() {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            URL defaultIconUrl = classLoader.getResource("icons/tray_icon.png");

            if (defaultIconUrl != null) {
                trayImage = Toolkit.getDefaultToolkit().getImage(defaultIconUrl);
            } else {
                trayImage = createDefaultTrayImage();
            }

            URL newIconUrl = classLoader.getResource("icons/tray_icon_new.png");
            if (newIconUrl != null) {
                trayImageNew = Toolkit.getDefaultToolkit().getImage(newIconUrl);
            } else {
                trayImageNew = createDefaultTrayImage(Color.RED);
            }

        } catch (Exception e) {
            LOGGER.warning("加载托盘图标失败: " + e.getMessage());
            trayImage = createDefaultTrayImage();
            trayImageNew = createDefaultTrayImage(Color.RED);
        }
    }

    /**
     * 创建默认托盘图标
     */
    private Image createDefaultTrayImage() {
        return createDefaultTrayImage(new Color(70, 130, 180));
    }

    /**
     * 创建默认托盘图标（指定颜色）
     */
    private Image createDefaultTrayImage(Color color) {
        int size = 16;
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(
                size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(color);
        g2d.fillOval(0, 0, size, size);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "Q";
        int x = (size - fm.stringWidth(text)) / 2;
        int y = (size - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(text, x, y);
        g2d.dispose();

        return image;
    }

    /**
     * 创建并显示系统托盘图标
     */
    public void createTrayIcon() {
        if (!SystemTray.isSupported()) {
            LOGGER.warning("系统托盘不支持");
            return;
        }

        try {
            systemTray = SystemTray.getSystemTray();
            PopupMenu popupMenu = createPopupMenu();
            trayIcon = new TrayIcon(trayImage, "QR签到系统", popupMenu);
            trayIcon.setImageAutoSize(true);
            addTrayIconListeners();
            systemTray.add(trayIcon);
            LOGGER.info("系统托盘图标已创建");

        } catch (Exception e) {
            LOGGER.severe("创建系统托盘图标失败: " + e.getMessage());
        }
    }

    /**
     * 创建托盘图标右键菜单
     */
    private PopupMenu createPopupMenu() {
        PopupMenu popupMenu = new PopupMenu();

        MenuItem showItem = new MenuItem("显示主窗口");
        showItem.addActionListener(e -> restoreWindow());
        popupMenu.add(showItem);

        popupMenu.addSeparator();

        MenuItem toggleItem = new MenuItem("暂停签到");
        toggleItem.addActionListener(e -> toggleAttendance());
        popupMenu.add(toggleItem);

        MenuItem qrItem = new MenuItem("生成签到二维码");
        qrItem.addActionListener(e -> generateQRCode());
        popupMenu.add(qrItem);

        popupMenu.addSeparator();

        MenuItem notificationsItem = new MenuItem("查看通知 (" + notificationCount + ")");
        notificationsItem.setEnabled(notificationCount > 0);
        notificationsItem.addActionListener(e -> showNotifications());
        popupMenu.add(notificationsItem);

        popupMenu.addSeparator();

        MenuItem exitItem = new MenuItem("退出");
        exitItem.addActionListener(e -> exitApplication());
        popupMenu.add(exitItem);

        return popupMenu;
    }

    /**
     * 为托盘图标添加事件监听器
     */
    private void addTrayIconListeners() {
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (mainWindow != null && mainWindow.isVisible()) {
                        minimizeToTray();
                    } else {
                        restoreWindow();
                    }
                } else if (e.getButton() == MouseEvent.BUTTON2) {
                    showNotification("QR签到系统", "双击显示主窗口，右键查看更多选项");
                }
            }
        });

        trayIcon.addActionListener(e -> restoreWindow());
    }

    /**
     * 最小化窗口到系统托盘
     */
    public void minimizeToTray() {
        if (mainWindow != null) {
            mainWindow.setVisible(false);
            isInTray = true;
            showNotification("QR签到系统", "程序已最小化到系统托盘");
            LOGGER.info("窗口已最小化到系统托盘");
        }
    }

    /**
     * 从系统托盘恢复窗口
     */
    public void restoreWindow() {
        if (mainWindow != null) {
            mainWindow.setVisible(true);
            mainWindow.setExtendedState(JFrame.NORMAL);
            mainWindow.toFront();
            mainWindow.requestFocus();
            isInTray = false;
            restoreDefaultTrayIcon();
            LOGGER.info("窗口已从系统托盘恢复");
        }
    }

    /**
     * 显示通知
     */
    public void showNotification(String title, String message) {
        if (trayIcon != null) {
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
        }
    }

    /**
     * 显示警告通知
     */
    public void showWarning(String title, String message) {
        if (trayIcon != null) {
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.WARNING);
        }
    }

    /**
     * 显示错误通知
     */
    public void showError(String title, String message) {
        if (trayIcon != null) {
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.ERROR);
        }
    }

    /**
     * 更新托盘图标为有新消息的状态
     */
    public void updateTrayIconForNewNotification() {
        if (trayIcon != null && trayImageNew != null) {
            trayIcon.setImage(trayImageNew);
            notificationCount++;
            updateNotificationMenu();
        }
    }

    /**
     * 恢复默认托盘图标
     */
    public void restoreDefaultTrayIcon() {
        if (trayIcon != null && trayImage != null) {
            trayIcon.setImage(trayImage);
            notificationCount = 0;
            updateNotificationMenu();
        }
    }

    /**
     * 更新通知菜单项
     */
    private void updateNotificationMenu() {
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

    /**
     * 切换签到状态
     */
    private void toggleAttendance() {
        AttendanceApplication app = AttendanceApplication.getInstance();
        if (app.isPaused()) {
            app.resume();
            showNotification("签到恢复", "签到功能已恢复");
        } else {
            app.pause();
            showNotification("签到暂停", "签到功能已暂停");
        }
    }

    /**
     * 生成签到二维码
     */
    private void generateQRCode() {
        if (mainWindow != null) {
            // 假设MainWindow有generateQRCode方法
            showNotification("二维码生成", "正在生成签到二维码...");
        }
    }

    /**
     * 显示通知列表
     */
    private void showNotifications() {
        JOptionPane.showMessageDialog(null,
                "您有 " + notificationCount + " 条未读通知",
                "系统通知",
                JOptionPane.INFORMATION_MESSAGE);
        restoreDefaultTrayIcon();
    }

    /**
     * 退出应用程序
     */
    private void exitApplication() {
        AttendanceApplication.getInstance().handleWindowClosing();
    }

    /**
     * 检查窗口是否在托盘区域
     */
    public boolean isInTray() {
        return isInTray;
    }

    /**
     * 清理托盘图标资源
     */
    public void cleanup() {
        if (systemTray != null && trayIcon != null) {
            systemTray.remove(trayIcon);
            LOGGER.info("系统托盘图标已清理");
        }
    }
}