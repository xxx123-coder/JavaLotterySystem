package com.qrcode.attendance.gui.frame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.prefs.Preferences;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox rememberPasswordCheck;
    private JCheckBox autoLoginCheck;
    private final Preferences prefs;
    private JButton loginButton;
    private int retryCount = 0;

    // 模拟的用户数据
    private static final String[][] TEACHER_ACCOUNTS = {
            {"admin", "admin123", "管理员"},
            {"teacher1", "teacher123", "张老师"},
            {"teacher2", "teacher456", "李老师"}
    };

    public LoginFrame() {
        prefs = Preferences.userNodeForPackage(LoginFrame.class);
        initComponents();
        loadSavedCredentials();
    }

    private void initComponents() {
        setTitle("教师登录 - 二维码考勤系统");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 350);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = createMainPanel();
        setContentPane(mainPanel);

        // 检查自动登录
        if (prefs.getBoolean("autoLogin", false)) {
            autoLoginCheck.setSelected(true);
            SwingUtilities.invokeLater(this::performLogin);
        }
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 标题
        JLabel titleLabel = new JLabel("教师登录", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 102, 204));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // 表单面板
        JPanel formPanel = createFormPanel();
        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 用户名
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("用户名:"), gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(15);
        formPanel.add(usernameField, gbc);

        // 密码
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("密码:"), gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        formPanel.add(passwordField, gbc);

        // 记住密码
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        rememberPasswordCheck = new JCheckBox("记住密码");
        formPanel.add(rememberPasswordCheck, gbc);

        // 自动登录
        gbc.gridy = 3;
        autoLoginCheck = new JCheckBox("自动登录");
        autoLoginCheck.addActionListener(e -> {
            if (autoLoginCheck.isSelected()) {
                rememberPasswordCheck.setSelected(true);
            }
        });
        formPanel.add(autoLoginCheck, gbc);

        return formPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        loginButton = new JButton("登录");
        loginButton.setPreferredSize(new Dimension(100, 30));
        loginButton.addActionListener(e -> performLogin());

        JButton forgotPasswordButton = new JButton("忘记密码");
        forgotPasswordButton.setPreferredSize(new Dimension(100, 30));
        forgotPasswordButton.addActionListener(e -> showForgotPasswordDialog());

        JButton exitButton = new JButton("退出");
        exitButton.setPreferredSize(new Dimension(100, 30));
        exitButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(loginButton);
        buttonPanel.add(forgotPasswordButton);
        buttonPanel.add(exitButton);

        // 添加回车键登录支持
        getRootPane().setDefaultButton(loginButton);
        usernameField.addActionListener(e -> passwordField.requestFocus());
        passwordField.addActionListener(e -> performLogin());

        return buttonPanel;
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "用户名和密码不能为空！", "输入错误", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 验证用户凭据
        boolean authenticated = false;
        String teacherName = "";

        for (String[] account : TEACHER_ACCOUNTS) {
            if (account[0].equals(username) && account[1].equals(password)) {
                authenticated = true;
                teacherName = account[2];
                break;
            }
        }

        if (authenticated) {
            handleSuccessfulLogin(username, password, teacherName);
        } else {
            handleFailedLogin();
        }
    }

    private void handleSuccessfulLogin(String username, String password, String teacherName) {
        // 保存凭证
        if (rememberPasswordCheck.isSelected()) {
            prefs.put("username", username);
            prefs.put("password", password);
            prefs.putBoolean("rememberPassword", true);
            prefs.putBoolean("autoLogin", autoLoginCheck.isSelected());
        } else {
            prefs.remove("username");
            prefs.remove("password");
            prefs.putBoolean("rememberPassword", false);
            prefs.putBoolean("autoLogin", false);
        }

        // 登录成功
        JOptionPane.showMessageDialog(this,
                "欢迎回来，" + teacherName + "！",
                "登录成功",
                JOptionPane.INFORMATION_MESSAGE);

        // 启动主窗口
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.updateStatus("已登录用户: " + teacherName);
            mainFrame.setVisible(true);
        });

        dispose();
    }

    private void handleFailedLogin() {
        retryCount++;
        int MAX_RETRY = 3;
        int remainingAttempts = MAX_RETRY - retryCount;

        if (retryCount >= MAX_RETRY) {
            JOptionPane.showMessageDialog(this,
                    "登录失败次数过多！系统将在5秒后退出。",
                    "登录失败",
                    JOptionPane.ERROR_MESSAGE);

            Timer timer = new Timer(5000, e -> System.exit(0));
            timer.setRepeats(false);
            timer.start();

            if (loginButton != null) {
                loginButton.setEnabled(false);
            }
            usernameField.setEnabled(false);
            passwordField.setEnabled(false);

        } else {
            JOptionPane.showMessageDialog(this,
                    "用户名或密码错误！剩余尝试次数: " + remainingAttempts,
                    "登录失败",
                    JOptionPane.ERROR_MESSAGE);

            passwordField.setText("");
            passwordField.requestFocus();
        }
    }

    private void loadSavedCredentials() {
        boolean rememberPassword = prefs.getBoolean("rememberPassword", false);
        rememberPasswordCheck.setSelected(rememberPassword);

        if (rememberPassword) {
            String savedUsername = prefs.get("username", "");
            String savedPassword = prefs.get("password", "");

            usernameField.setText(savedUsername);
            passwordField.setText(savedPassword);

            if (!savedUsername.isEmpty()) {
                passwordField.requestFocus();
            }
        }
    }

    private void showForgotPasswordDialog() {
        JDialog dialog = new JDialog(this, "找回密码", true);
        dialog.setSize(350, 200);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.add(new JLabel("用户名:"));
        JTextField usernameField = new JTextField();
        formPanel.add(usernameField);

        formPanel.add(new JLabel("注册邮箱:"));
        JTextField emailField = new JTextField();
        formPanel.add(emailField);

        panel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton submitButton = new JButton("提交");
        submitButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(dialog,
                    "重置链接已发送到您的邮箱（模拟）。\n请查收邮件并按照指引重置密码。",
                    "操作成功",
                    JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });

        JButton cancelButton = new JButton("取消");
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                Logger.getLogger(LoginFrame.class.getName()).log(Level.SEVERE, "设置外观失败", e);
            }

            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}