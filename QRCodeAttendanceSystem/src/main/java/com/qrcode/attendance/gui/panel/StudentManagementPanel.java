package com.qrcode.attendance.gui.panel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class StudentManagementPanel extends JPanel {
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> classFilterCombo;
    private JTextField searchField;
    private Set<String> studentIdSet;

    public StudentManagementPanel() {
        studentIdSet = new HashSet<>();
        initComponents();
        setupLayout();
        setupTable();
    }

    private void initComponents() {
        tableModel = new DefaultTableModel(new Object[]{"学号", "姓名", "性别", "班级", "专业", "电话", "邮箱", "入学时间"}, 0);
        studentTable = new JTable(tableModel);
        studentTable.setRowHeight(30);

        classFilterCombo = new JComboBox<>(new String[]{"所有班级", "计算机科学与技术1班", "软件工程2班", "数据科学1班", "人工智能1班"});
        searchField = new JTextField(20);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // 顶部工具栏
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("学生管理");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        topPanel.add(titleLabel, BorderLayout.WEST);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.add(new JLabel("班级筛选:"));
        filterPanel.add(classFilterCombo);
        filterPanel.add(Box.createHorizontalStrut(20));
        filterPanel.add(new JLabel("搜索:"));
        filterPanel.add(searchField);

        JButton btnSearch = new JButton("搜索");
        btnSearch.addActionListener(e -> filterStudents());
        filterPanel.add(btnSearch);

        topPanel.add(filterPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // 中间表格区域
        JScrollPane tableScrollPane = new JScrollPane(studentTable);
        tableScrollPane.setBorder(new TitledBorder("学生列表"));
        add(tableScrollPane, BorderLayout.CENTER);

        // 底部按钮区域
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        JButton btnAdd = new JButton("添加学生");
        btnAdd.setIcon(new ImageIcon("icons/add.png"));
        btnAdd.addActionListener(e -> addStudent());

        JButton btnEdit = new JButton("编辑学生");
        btnEdit.setIcon(new ImageIcon("icons/edit.png"));
        btnEdit.addActionListener(e -> editStudent());

        JButton btnDelete = new JButton("删除学生");
        btnDelete.setIcon(new ImageIcon("icons/delete.png"));
        btnDelete.addActionListener(e -> deleteStudent());

        JButton btnBatchImport = new JButton("批量导入");
        btnBatchImport.setIcon(new ImageIcon("icons/import.png"));
        btnBatchImport.addActionListener(e -> batchImport());

        JButton btnExport = new JButton("导出Excel");
        btnExport.setIcon(new ImageIcon("icons/export.png"));
        btnExport.addActionListener(e -> exportStudents());

        JButton btnPrint = new JButton("打印");
        btnPrint.setIcon(new ImageIcon("icons/print.png"));
        btnPrint.addActionListener(e -> printStudents());

        JButton btnRefresh = new JButton("刷新");
        btnRefresh.setIcon(new ImageIcon("icons/refresh.png"));
        btnRefresh.addActionListener(e -> refreshTable());

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDelete);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(btnBatchImport);
        buttonPanel.add(btnExport);
        buttonPanel.add(btnPrint);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(btnRefresh);

        add(buttonPanel, BorderLayout.SOUTH);

        // 添加测试数据
        addSampleData();
    }

    private void setupTable() {
        // 设置列宽
        studentTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        studentTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        studentTable.getColumnModel().getColumn(2).setPreferredWidth(60);
        studentTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        studentTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        studentTable.getColumnModel().getColumn(5).setPreferredWidth(120);
        studentTable.getColumnModel().getColumn(6).setPreferredWidth(150);
        studentTable.getColumnModel().getColumn(7).setPreferredWidth(100);
    }

    private void addSampleData() {
        tableModel.addRow(new Object[]{"2023001", "张三", "男", "计算机科学与技术1班", "计算机科学", "13800138001", "zhangsan@example.com", "2023-09-01"});
        tableModel.addRow(new Object[]{"2023002", "李四", "女", "计算机科学与技术1班", "计算机科学", "13800138002", "lisi@example.com", "2023-09-01"});
        tableModel.addRow(new Object[]{"2023003", "王五", "男", "软件工程2班", "软件工程", "13800138003", "wangwu@example.com", "2023-09-01"});
        tableModel.addRow(new Object[]{"2024001", "赵六", "女", "数据科学1班", "数据科学", "13800138004", "zhaoliu@example.com", "2024-09-01"});
        tableModel.addRow(new Object[]{"2024002", "钱七", "男", "人工智能1班", "人工智能", "13800138005", "qianqi@example.com", "2024-09-01"});

        // 添加到学号集合
        studentIdSet.add("2023001");
        studentIdSet.add("2023002");
        studentIdSet.add("2023003");
        studentIdSet.add("2024001");
        studentIdSet.add("2024002");
    }

    private void addStudent() {
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "添加学生", true);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField txtStudentId = new JTextField();
        JTextField txtName = new JTextField();
        JComboBox<String> cbGender = new JComboBox<>(new String[]{"男", "女"});
        JComboBox<String> cbClass = new JComboBox<>(new String[]{"计算机科学与技术1班", "软件工程2班", "数据科学1班", "人工智能1班"});
        JTextField txtMajor = new JTextField();
        JTextField txtPhone = new JTextField();
        JTextField txtEmail = new JTextField();
        JTextField txtEnrollmentDate = new JTextField(new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));

        formPanel.add(new JLabel("学号*:"));
        formPanel.add(txtStudentId);
        formPanel.add(new JLabel("姓名*:"));
        formPanel.add(txtName);
        formPanel.add(new JLabel("性别:"));
        formPanel.add(cbGender);
        formPanel.add(new JLabel("班级*:"));
        formPanel.add(cbClass);
        formPanel.add(new JLabel("专业:"));
        formPanel.add(txtMajor);
        formPanel.add(new JLabel("电话:"));
        formPanel.add(txtPhone);
        formPanel.add(new JLabel("邮箱:"));
        formPanel.add(txtEmail);
        formPanel.add(new JLabel("入学时间:"));
        formPanel.add(txtEnrollmentDate);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("保存");
        JButton btnCancel = new JButton("取消");

        btnSave.addActionListener(e -> {
            // 验证数据
            if (txtStudentId.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "学号不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 验证学号唯一性
            if (studentIdSet.contains(txtStudentId.getText().trim())) {
                JOptionPane.showMessageDialog(dialog, "学号已存在！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (txtName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "姓名不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            tableModel.addRow(new Object[]{
                    txtStudentId.getText().trim(),
                    txtName.getText().trim(),
                    cbGender.getSelectedItem(),
                    cbClass.getSelectedItem(),
                    txtMajor.getText().trim(),
                    txtPhone.getText().trim(),
                    txtEmail.getText().trim(),
                    txtEnrollmentDate.getText().trim()
            });

            studentIdSet.add(txtStudentId.getText().trim());
            dialog.dispose();
            JOptionPane.showMessageDialog(this, "学生添加成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void editStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要编辑的学生！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = studentTable.convertRowIndexToModel(selectedRow);
        String studentId = (String) tableModel.getValueAt(modelRow, 0);

        // 编辑逻辑类似添加，这里显示简单编辑
        JOptionPane.showMessageDialog(this, "编辑学生: " + studentId, "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要删除的学生！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要删除选中的学生吗？",
                "确认删除",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            int modelRow = studentTable.convertRowIndexToModel(selectedRow);
            String studentId = (String) tableModel.getValueAt(modelRow, 0);
            tableModel.removeRow(modelRow);
            studentIdSet.remove(studentId);
            JOptionPane.showMessageDialog(this, "删除成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void batchImport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Excel文件 (*.xls, *.xlsx)", "xls", "xlsx"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            // 这里应该实现Excel导入逻辑
            JOptionPane.showMessageDialog(this, "从Excel批量导入学生信息: " + file.getName(), "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void exportStudents() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Excel文件 (*.xlsx)", "xlsx"));
        fileChooser.setSelectedFile(new File("学生信息_" + new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date()) + ".xlsx"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            // 这里应该实现Excel导出逻辑
            JOptionPane.showMessageDialog(this, "导出学生信息到: " + file.getName(), "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void printStudents() {
        JOptionPane.showMessageDialog(this, "打印功能", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    private void filterStudents() {
        String selectedClass = (String) classFilterCombo.getSelectedItem();
        String searchText = searchField.getText().trim().toLowerCase();

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String className = (String) tableModel.getValueAt(i, 3);
            String studentName = (String) tableModel.getValueAt(i, 1);
            String studentId = (String) tableModel.getValueAt(i, 0);

            boolean classMatch = "所有班级".equals(selectedClass) || className.equals(selectedClass);
            boolean searchMatch = searchText.isEmpty() ||
                    studentName.toLowerCase().contains(searchText) ||
                    studentId.contains(searchText);

            // 这里应该更新表格的显示/隐藏逻辑
            // 由于使用的是JTable，更好的方式是使用RowFilter
        }

        JOptionPane.showMessageDialog(this, "筛选: " + selectedClass + ", 搜索: " + searchText, "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshTable() {
        classFilterCombo.setSelectedIndex(0);
        searchField.setText("");
        // 重置筛选状态
    }
}