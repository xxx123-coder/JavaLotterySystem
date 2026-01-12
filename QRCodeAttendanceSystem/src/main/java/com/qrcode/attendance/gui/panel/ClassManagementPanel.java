package com.qrcode.attendance.gui.panel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

public class ClassManagementPanel extends JPanel {
    private JTable classTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> filterComboBox;
    private TableRowSorter<DefaultTableModel> sorter;

    public ClassManagementPanel() {
        initComponents();
        setupLayout();
        setupTable();
    }

    private void initComponents() {
        tableModel = new DefaultTableModel(new Object[]{"班级ID", "班级名称", "专业", "年级", "辅导员", "学生人数", "创建时间"}, 0);
        classTable = new JTable(tableModel);
        classTable.setRowHeight(30);

        searchField = new JTextField(20);
        filterComboBox = new JComboBox<>(new String[]{"全部", "2023级", "2024级", "2025级"});
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // 顶部标题和搜索栏
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("班级管理");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        topPanel.add(titleLabel, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.add(new JLabel("搜索:"));
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("年级筛选:"));
        searchPanel.add(filterComboBox);

        JButton btnSearch = new JButton("搜索");
        btnSearch.addActionListener(e -> searchClasses());
        searchPanel.add(btnSearch);

        topPanel.add(searchPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // 中间表格区域
        JScrollPane tableScrollPane = new JScrollPane(classTable);
        tableScrollPane.setBorder(new TitledBorder("班级列表"));
        add(tableScrollPane, BorderLayout.CENTER);

        // 底部按钮区域
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        JButton btnAdd = new JButton("添加班级");
        btnAdd.setIcon(new ImageIcon("icons/add.png"));
        btnAdd.addActionListener(e -> addClass());

        JButton btnEdit = new JButton("编辑班级");
        btnEdit.setIcon(new ImageIcon("icons/edit.png"));
        btnEdit.addActionListener(e -> editClass());

        JButton btnDelete = new JButton("删除班级");
        btnDelete.setIcon(new ImageIcon("icons/delete.png"));
        btnDelete.addActionListener(e -> deleteClass());

        JButton btnImport = new JButton("导入Excel");
        btnImport.setIcon(new ImageIcon("icons/import.png"));
        btnImport.addActionListener(e -> importFromExcel());

        JButton btnExport = new JButton("导出Excel");
        btnExport.setIcon(new ImageIcon("icons/export.png"));
        btnExport.addActionListener(e -> exportToExcel());

        JButton btnRefresh = new JButton("刷新");
        btnRefresh.setIcon(new ImageIcon("icons/refresh.png"));
        btnRefresh.addActionListener(e -> refreshTable());

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDelete);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(btnImport);
        buttonPanel.add(btnExport);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(btnRefresh);

        add(buttonPanel, BorderLayout.SOUTH);

        // 添加测试数据
        addSampleData();
    }

    private void setupTable() {
        sorter = new TableRowSorter<>(tableModel);
        classTable.setRowSorter(sorter);

        // 设置列宽
        classTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        classTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        classTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        classTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        classTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        classTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        classTable.getColumnModel().getColumn(6).setPreferredWidth(150);
    }

    private void addSampleData() {
        tableModel.addRow(new Object[]{"C001", "计算机科学与技术1班", "计算机科学", "2023", "张老师", 45, "2023-09-01"});
        tableModel.addRow(new Object[]{"C002", "软件工程2班", "软件工程", "2023", "王老师", 42, "2023-09-01"});
        tableModel.addRow(new Object[]{"C003", "数据科学1班", "数据科学", "2024", "李老师", 38, "2024-09-01"});
        tableModel.addRow(new Object[]{"C004", "人工智能1班", "人工智能", "2024", "赵老师", 40, "2024-09-01"});
        tableModel.addRow(new Object[]{"C005", "网络工程1班", "网络工程", "2025", "刘老师", 36, "2025-09-01"});
    }

    private void addClass() {
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "添加班级", true);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField txtId = new JTextField();
        JTextField txtName = new JTextField();
        JTextField txtMajor = new JTextField();
        JComboBox<String> cbGrade = new JComboBox<>(new String[]{"2023", "2024", "2025", "2026"});
        JTextField txtCounselor = new JTextField();
        JSpinner spStudentCount = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));

        formPanel.add(new JLabel("班级ID:"));
        formPanel.add(txtId);
        formPanel.add(new JLabel("班级名称:"));
        formPanel.add(txtName);
        formPanel.add(new JLabel("专业:"));
        formPanel.add(txtMajor);
        formPanel.add(new JLabel("年级:"));
        formPanel.add(cbGrade);
        formPanel.add(new JLabel("辅导员:"));
        formPanel.add(txtCounselor);
        formPanel.add(new JLabel("学生人数:"));
        formPanel.add(spStudentCount);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("保存");
        JButton btnCancel = new JButton("取消");

        btnSave.addActionListener(e -> {
            // 验证数据完整性
            if (txtId.getText().trim().isEmpty() || txtName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "班级ID和名称不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 检查班级ID是否重复
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (tableModel.getValueAt(i, 0).equals(txtId.getText().trim())) {
                    JOptionPane.showMessageDialog(dialog, "班级ID已存在！", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            tableModel.addRow(new Object[]{
                    txtId.getText().trim(),
                    txtName.getText().trim(),
                    txtMajor.getText().trim(),
                    cbGrade.getSelectedItem(),
                    txtCounselor.getText().trim(),
                    spStudentCount.getValue(),
                    new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date())
            });
            dialog.dispose();
            JOptionPane.showMessageDialog(this, "班级添加成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
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

    private void editClass() {
        int selectedRow = classTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要编辑的班级！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = classTable.convertRowIndexToModel(selectedRow);
        // 编辑逻辑类似添加，这里省略具体实现
        JOptionPane.showMessageDialog(this, "编辑班级功能", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteClass() {
        int selectedRow = classTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要删除的班级！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要删除选中的班级吗？",
                "确认删除",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            int modelRow = classTable.convertRowIndexToModel(selectedRow);
            tableModel.removeRow(modelRow);
            JOptionPane.showMessageDialog(this, "删除成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void importFromExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Excel文件 (*.xls, *.xlsx)", "xls", "xlsx"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            // 这里应该调用Excel导入逻辑
            JOptionPane.showMessageDialog(this, "从文件导入: " + file.getName(), "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void exportToExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Excel文件 (*.xlsx)", "xlsx"));
        fileChooser.setSelectedFile(new File("班级信息_" + new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date()) + ".xlsx"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            // 这里应该调用Excel导出逻辑
            JOptionPane.showMessageDialog(this, "导出到文件: " + file.getName(), "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void searchClasses() {
        String searchText = searchField.getText().trim();
        String filterValue = (String) filterComboBox.getSelectedItem();

        RowFilter<DefaultTableModel, Object> rf = RowFilter.regexFilter(".*" + searchText + ".*", 1); // 按班级名称搜索

        if (!"全部".equals(filterValue)) {
            RowFilter<DefaultTableModel, Object> gradeFilter = RowFilter.regexFilter(filterValue, 3); // 按年级筛选
            rf = RowFilter.andFilter(java.util.Arrays.asList(rf, gradeFilter));
        }

        sorter.setRowFilter(rf);
    }

    private void refreshTable() {
        searchField.setText("");
        filterComboBox.setSelectedIndex(0);
        sorter.setRowFilter(null);
    }
}