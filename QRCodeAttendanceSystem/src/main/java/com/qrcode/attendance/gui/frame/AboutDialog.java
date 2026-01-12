package com.qrcode.attendance.gui.frame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AboutDialog extends JDialog {
    private static final String VERSION = "1.0.0";
    private static final String COPYRIGHT = "© 2024 二维码考勤系统 版权所有";

    public AboutDialog(JFrame parent) {
        super(parent, "关于", true);
        initComponents();
    }

    private void initComponents() {
        setSize(500, 400);
        setLocationRelativeTo(getOwner());
        setResizable(false);

        JPanel mainPanel = createMainPanel();
        setContentPane(mainPanel);
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel infoPanel = createInfoPanel();
        mainPanel.add(infoPanel, BorderLayout.CENTER);

        JPanel systemPanel = createSystemInfoPanel();
        JPanel buttonPanel = createButtonPanel();

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(systemPanel, BorderLayout.NORTH);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(southPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        // 应用程序图标和名称
        JLabel appNameLabel = new JLabel("二维码考勤系统");
        appNameLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        appNameLabel.setForeground(new Color(0, 102, 204));
        appNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel versionLabel = new JLabel("版本: " + VERSION);
        versionLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel copyrightLabel = new JLabel(COPYRIGHT);
        copyrightLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        copyrightLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        infoPanel.add(appNameLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        infoPanel.add(versionLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(copyrightLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JSeparator separator = new JSeparator();
        separator.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.add(separator);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        infoPanel.add(createTeamSection());
        infoPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        infoPanel.add(createContactSection());

        return infoPanel;
    }

    private Component createTeamSection() {
        JPanel teamPanel = new JPanel();
        teamPanel.setLayout(new BoxLayout(teamPanel, BoxLayout.Y_AXIS));

        JLabel teamLabel = new JLabel("开发团队");
        teamLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        teamLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        teamPanel.add(teamLabel);
        teamPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        String[] teamMembers = {
                "项目经理: 张三",
                "前端开发: 李四",
                "后端开发: 王五",
                "测试工程师: 赵六"
        };

        for (String member : teamMembers) {
            JLabel memberLabel = new JLabel(member);
            memberLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            memberLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            teamPanel.add(memberLabel);
        }

        return teamPanel;
    }

    private Component createContactSection() {
        JPanel contactPanel = new JPanel();
        contactPanel.setLayout(new BoxLayout(contactPanel, BoxLayout.Y_AXIS));

        JLabel contactLabel = new JLabel("联系方式");
        contactLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        contactLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        contactPanel.add(contactLabel);
        contactPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JLabel emailLabel = new JLabel("邮箱: support@qrcode-attendance.com");
        emailLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        emailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel websiteLabel = new JLabel("网站: https://www.qrcode-attendance.com");
        websiteLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        websiteLabel.setForeground(Color.BLUE);
        websiteLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        websiteLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                try {
                    Desktop.getDesktop().browse(new URI("https://www.qrcode-attendance.com"));
                } catch (Exception ex) {
                    Logger.getLogger(AboutDialog.class.getName()).log(Level.WARNING, "无法打开浏览器", ex);
                    JOptionPane.showMessageDialog(AboutDialog.this,
                            "无法打开浏览器，请手动访问: https://www.qrcode-attendance.com",
                            "错误",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        websiteLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        contactPanel.add(emailLabel);
        contactPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        contactPanel.add(websiteLabel);

        return contactPanel;
    }

    private JPanel createSystemInfoPanel() {
        JPanel systemPanel = new JPanel(new BorderLayout());
        systemPanel.setBorder(BorderFactory.createTitledBorder("系统信息"));

        JTextArea systemInfoArea = new JTextArea();
        systemInfoArea.setEditable(false);
        systemInfoArea.setFont(new Font("宋体", Font.PLAIN, 11));

        StringBuilder sysInfo = new StringBuilder();
        sysInfo.append("操作系统: ").append(System.getProperty("os.name")).append("\n");
        sysInfo.append("系统版本: ").append(System.getProperty("os.version")).append("\n");
        sysInfo.append("系统架构: ").append(System.getProperty("os.arch")).append("\n");
        sysInfo.append("Java版本: ").append(System.getProperty("java.version")).append("\n");
        sysInfo.append("Java虚拟机: ").append(System.getProperty("java.vm.name")).append("\n");
        sysInfo.append("Java供应商: ").append(System.getProperty("java.vendor")).append("\n");
        sysInfo.append("用户目录: ").append(System.getProperty("user.home")).append("\n");
        sysInfo.append("工作目录: ").append(System.getProperty("user.dir")).append("\n");

        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long usedMemory = totalMemory - freeMemory;

        sysInfo.append("内存使用: ").append(usedMemory).append("MB / ").append(totalMemory).append("MB\n");
        sysInfo.append("可用处理器: ").append(runtime.availableProcessors()).append("\n");

        systemInfoArea.setText(sysInfo.toString());

        JScrollPane scrollPane = new JScrollPane(systemInfoArea);
        scrollPane.setPreferredSize(new Dimension(450, 120));
        systemPanel.add(scrollPane, BorderLayout.CENTER);

        return systemPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton updateButton = new JButton("检查更新");
        updateButton.addActionListener(this::checkForUpdates);

        JButton licenseButton = new JButton("许可证");
        licenseButton.addActionListener(this::showLicense);

        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(updateButton);
        buttonPanel.add(licenseButton);
        buttonPanel.add(closeButton);

        return buttonPanel;
    }

    private void checkForUpdates(ActionEvent e) {
        JOptionPane.showMessageDialog(this,
                "正在检查更新...\n" +
                        "当前版本: " + VERSION + "\n" +
                        "已是最新版本！",
                "检查更新",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showLicense(ActionEvent e) {
        JDialog licenseDialog = new JDialog(this, "许可证和使用条款", true);
        licenseDialog.setSize(600, 400);
        licenseDialog.setLocationRelativeTo(this);

        JTextArea licenseText = new JTextArea();
        licenseText.setEditable(false);
        licenseText.setFont(new Font("宋体", Font.PLAIN, 12));
        licenseText.setText(
                "二维码考勤系统 软件许可证协议\n\n" +
                        "1. 许可授予\n" +
                        "   本软件授权给最终用户使用，仅供教育和研究目的。\n\n" +
                        "2. 使用限制\n" +
                        "   不得对本软件进行反向工程、反编译或反汇编。\n" +
                        "   不得将本软件用于商业用途。\n\n" +
                        "3. 免责声明\n" +
                        "   本软件按\"原样\"提供，不提供任何明示或暗示的保证。\n" +
                        "   作者不对因使用本软件而产生的任何损害负责。\n\n" +
                        "4. 版权声明\n" +
                        "   本软件及其相关文档受版权法保护。\n" +
                        "   未经授权，不得复制、修改或分发本软件。\n\n" +
                        "5. 条款修改\n" +
                        "   保留随时修改本协议条款的权利。\n"
        );

        JScrollPane scrollPane = new JScrollPane(licenseText);
        licenseDialog.add(scrollPane);
        licenseDialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                Logger.getLogger(AboutDialog.class.getName()).log(Level.SEVERE, "设置外观失败", e);
            }

            JFrame testFrame = new JFrame();
            testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            testFrame.setSize(300, 200);

            JButton showAboutButton = new JButton("显示关于对话框");
            showAboutButton.addActionListener(e -> {
                AboutDialog aboutDialog = new AboutDialog(testFrame);
                aboutDialog.setVisible(true);
            });

            testFrame.add(showAboutButton);
            testFrame.setVisible(true);
        });
    }
}