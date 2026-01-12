package com.qrcode.attendance.gui.frame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.prefs.Preferences;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainFrame extends JFrame {
    private JTabbedPane tabbedPane;
    private JLabel statusLabel;
    private Preferences prefs;

    public MainFrame() {
        initComponents();
        loadWindowState();
    }

    private void initComponents() {
        // 窗口基本设置
        setTitle("二维码考勤系统");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1024, 768));

        // 创建首选项存储
        prefs = Preferences.userNodeForPackage(MainFrame.class);

        // 创建菜单栏
        createMenuBar();

        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 创建选项卡面板
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("考勤管理", createAttendancePanel());
        tabbedPane.addTab("学生管理", createStudentPanel());
        tabbedPane.addTab("统计报表", createReportPanel());
        tabbedPane.addTab("系统设置", createSettingsPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // 创建状态栏
        statusLabel = new JLabel("就绪 | 用户: 未登录 | 时间: " + new java.util.Date());
        JPanel statusPanel = createStatusPanel();

        mainPanel.add(statusPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        // 添加窗口监听器保存状态
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveWindowState();
            }
        });

        // 添加组件监听器保存状态
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                saveWindowState();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                saveWindowState();
            }
        });
    }

    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusPanel.add(statusLabel, BorderLayout.WEST);

        // 添加内存使用情况
        JLabel memoryLabel = new JLabel();
        Timer memoryTimer = new Timer(5000, e -> {
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
            long totalMemory = runtime.totalMemory() / (1024 * 1024);
            memoryLabel.setText(String.format("内存: %dM/%dM", usedMemory, totalMemory));
        });
        memoryTimer.start();
        statusPanel.add(memoryLabel, BorderLayout.EAST);

        return statusPanel;
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // 文件菜单
        JMenu fileMenu = createFileMenu();
        menuBar.add(fileMenu);

        // 视图菜单
        JMenu viewMenu = createViewMenu();
        menuBar.add(viewMenu);

        // 窗口菜单
        JMenu windowMenu = createWindowMenu();
        menuBar.add(windowMenu);

        // 帮助菜单
        JMenu helpMenu = createHelpMenu();
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private JMenu createFileMenu() {
        JMenu fileMenu = new JMenu("文件");
        JMenuItem exitItem = new JMenuItem("退出");
        exitItem.addActionListener(e -> {
            saveWindowState();
            System.exit(0);
        });
        fileMenu.add(exitItem);
        return fileMenu;
    }

    private JMenu createViewMenu() {
        JMenu viewMenu = new JMenu("视图");
        JCheckBoxMenuItem statusBarItem = new JCheckBoxMenuItem("显示状态栏", true);
        statusBarItem.addActionListener(e -> {
            Container contentPane = getContentPane();
            // 修复BorderLayout方法调用问题
            BorderLayout layout = (BorderLayout) contentPane.getLayout();
            Component southComponent = layout.getLayoutComponent(BorderLayout.SOUTH);
            if (southComponent != null) {
                southComponent.setVisible(statusBarItem.isSelected());
            }
        });
        viewMenu.add(statusBarItem);
        return viewMenu;
    }

    private JMenu createWindowMenu() {
        JMenu windowMenu = new JMenu("窗口");
        JMenuItem maximizeItem = new JMenuItem("最大化");
        maximizeItem.addActionListener(e -> setExtendedState(JFrame.MAXIMIZED_BOTH));

        JMenuItem minimizeItem = new JMenuItem("最小化");
        minimizeItem.addActionListener(e -> setExtendedState(JFrame.ICONIFIED));

        JMenuItem restoreItem = new JMenuItem("恢复");
        restoreItem.addActionListener(e -> setExtendedState(JFrame.NORMAL));

        windowMenu.add(maximizeItem);
        windowMenu.add(minimizeItem);
        windowMenu.add(restoreItem);

        return windowMenu;
    }

    private JMenu createHelpMenu() {
        JMenu helpMenu = new JMenu("帮助");
        JMenuItem aboutItem = new JMenuItem("关于");
        aboutItem.addActionListener(e -> {
            AboutDialog aboutDialog = new AboutDialog(MainFrame.this);
            aboutDialog.setVisible(true);
        });
        helpMenu.add(aboutItem);
        return helpMenu;
    }

    private JPanel createAttendancePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("考勤管理功能面板", SwingConstants.CENTER), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStudentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("学生管理功能面板", SwingConstants.CENTER), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("统计报表功能面板", SwingConstants.CENTER), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("系统设置功能面板", SwingConstants.CENTER), BorderLayout.CENTER);
        return panel;
    }

    private void saveWindowState() {
        int width = getWidth();
        int height = getHeight();
        int x = getX();
        int y = getY();
        int state = getExtendedState();

        prefs.putInt("windowWidth", width);
        prefs.putInt("windowHeight", height);
        prefs.putInt("windowX", x);
        prefs.putInt("windowY", y);
        prefs.putInt("windowState", state);

        // 保存选项卡选择
        if (tabbedPane != null) {
            prefs.putInt("selectedTab", tabbedPane.getSelectedIndex());
        }
    }

    private void loadWindowState() {
        int width = prefs.getInt("windowWidth", 1024);
        int height = prefs.getInt("windowHeight", 768);
        int x = prefs.getInt("windowX", 100);
        int y = prefs.getInt("windowY", 100);
        int state = prefs.getInt("windowState", JFrame.NORMAL);

        setSize(width, height);
        setLocation(x, y);
        setExtendedState(state);

        // 恢复选项卡选择，确保tabbedPane不为空
        if (tabbedPane != null) {
            int selectedTab = prefs.getInt("selectedTab", 0);
            tabbedPane.setSelectedIndex(selectedTab);
        }
    }

    public void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message + " | 时间: " + new java.util.Date());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, "设置外观失败", e);
            }

            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}