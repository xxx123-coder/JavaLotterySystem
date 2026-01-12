package com.qrcode.attendance.gui.component;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * 自定义学生信息表格组件
 */
public class StudentTable extends JTable {
    private StudentTableModel model;
    private final JTextField searchField;
    private final JButton exportButton;
    private List<Student> studentData;

    public StudentTable() {
        // 在构造函数中直接初始化 model
        model = new StudentTableModel();
        searchField = new JTextField(20);
        exportButton = new JButton("导出数据");

        initTable();
        setupSearchPanel();
        setupCellEditor();
    }

    private void initTable() {
        setModel(model);

        // 设置列宽
        getColumnModel().getColumn(0).setPreferredWidth(100); // 学号
        getColumnModel().getColumn(1).setPreferredWidth(80);  // 姓名
        getColumnModel().getColumn(2).setPreferredWidth(100); // 班级
        getColumnModel().getColumn(3).setPreferredWidth(120); // 联系电话
        getColumnModel().getColumn(4).setPreferredWidth(80);  // 照片

        // 启用表格排序
        setAutoCreateRowSorter(true);

        // 设置自定义渲染器
        setDefaultRenderer(Object.class, new StudentTableCellRenderer());
    }

    private void setupSearchPanel() {
        // 搜索面板
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterStudents(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterStudents(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterStudents(); }
        });

        // 使用lambda表达式替换匿名内部类
        exportButton.addActionListener(e -> exportData());
    }

    private void setupCellEditor() {
        // 设置学号列编辑器（不可编辑）
        getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(new JTextField()) {
            @Override
            public boolean isCellEditable(java.util.EventObject e) {
                return false; // 学号不可编辑
            }
        });

        // 设置电话验证编辑器
        TableColumn phoneColumn = getColumnModel().getColumn(3);
        phoneColumn.setCellEditor(new PhoneNumberEditor());
    }

    /**
     * 加载学生数据
     */
    public void loadStudentData(List<Student> students) {
        this.studentData = students;
        model.setData(students);
    }

    /**
     * 过滤学生数据
     */
    private void filterStudents() {
        String searchText = searchField.getText().toLowerCase();
        List<Student> filtered = new ArrayList<>();

        if (studentData != null) {
            for (Student student : studentData) {
                // 添加空值检查
                String studentId = student.getStudentId() != null ? student.getStudentId().toLowerCase() : "";
                String name = student.getName() != null ? student.getName().toLowerCase() : "";
                String className = student.getClassName() != null ? student.getClassName().toLowerCase() : "";

                if (studentId.contains(searchText) ||
                        name.contains(searchText) ||
                        className.contains(searchText)) {
                    filtered.add(student);
                }
            }
        }

        model.setData(filtered);
    }

    /**
     * 导出数据 - 移除未使用的参数
     */
    private void exportData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("导出学生数据");
        fileChooser.setSelectedFile(new File("students.csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(file)) {
                // 写入表头
                writer.write("学号,姓名,班级,联系电话\n");

                // 写入数据
                for (int i = 0; i < model.getRowCount(); i++) {
                    Student student = model.getStudentAt(i);
                    if (student != null) {
                        writer.write(String.format("%s,%s,%s,%s\n",
                                student.getStudentId() != null ? student.getStudentId() : "",
                                student.getName() != null ? student.getName() : "",
                                student.getClassName() != null ? student.getClassName() : "",
                                student.getPhone() != null ? student.getPhone() : ""));
                    }
                }

                JOptionPane.showMessageDialog(this,
                        "数据导出成功！\n文件保存至：" + file.getAbsolutePath(),
                        "导出成功",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "导出失败：" + ex.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 获取搜索面板
     */
    public JPanel getSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("搜索:"), BorderLayout.WEST);
        panel.add(searchField, BorderLayout.CENTER);
        panel.add(exportButton, BorderLayout.EAST);
        return panel;
    }

    /**
     * 获取表格模型（供外部使用）
     */
    public StudentTableModel getTableModel() {
        return model;
    }

    /**
     * 获取学生数据（供外部使用）
     */
    public List<Student> getStudentData() {
        return studentData;
    }

    /**
     * 清空搜索框
     */
    public void clearSearch() {
        searchField.setText("");
    }

    /**
     * 表格模型
     */
    public class StudentTableModel extends AbstractTableModel {
        private List<Student> data;
        private final String[] columnNames = {"学号", "姓名", "班级", "联系电话", "照片"};

        public void setData(List<Student> data) {
            this.data = data;
            fireTableDataChanged();
        }

        public List<Student> getData() {
            return data;
        }

        public Student getStudentAt(int row) {
            if (data == null || row < 0 || row >= data.size()) {
                return null;
            }
            return data.get(row);
        }

        @Override
        public int getRowCount() {
            return data != null ? data.size() : 0;
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (data == null || rowIndex < 0 || rowIndex >= data.size()) {
                return null;
            }

            Student student = data.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return student.getStudentId();
                case 1:
                    return student.getName();
                case 2:
                    return student.getClassName();
                case 3:
                    return student.getPhone();
                case 4:
                    return student.getPhotoPath();
                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (data == null || rowIndex < 0 || rowIndex >= data.size()) {
                return;
            }

            Student student = data.get(rowIndex);
            switch (columnIndex) {
                case 1:
                    student.setName((String) aValue);
                    break;
                case 2:
                    student.setClassName((String) aValue);
                    break;
                case 3:
                    student.setPhone((String) aValue);
                    break;
            }
            fireTableCellUpdated(rowIndex, columnIndex);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex != 0 && columnIndex != 4; // 学号和照片列不可编辑
        }
    }

    /**
     * 自定义单元格渲染器
     */
    private class StudentTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);

            if (column == 4 && value instanceof String) {
                String photoPath = (String) value;
                if (!photoPath.isEmpty() && c instanceof JLabel) {
                    JLabel label = (JLabel) c;
                    try {
                        ImageIcon icon = new ImageIcon(photoPath);
                        if (icon.getIconWidth() > 0) {
                            Image img = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                            label.setIcon(new ImageIcon(img));
                            label.setText(""); // 清除文本
                        }
                    } catch (Exception e) {
                        // 如果图片加载失败，显示默认文本
                        label.setIcon(null);
                        label.setText("图片");
                    }
                }
            }

            return c;
        }
    }

    /**
     * 电话号码编辑器（带验证）
     */
    private class PhoneNumberEditor extends DefaultCellEditor {
        private final JTextField textField;

        public PhoneNumberEditor() {
            super(new JTextField());
            textField = (JTextField) getComponent();
        }

        @Override
        public boolean stopCellEditing() {
            String phone = textField.getText().trim();

            // 验证电话号码格式
            if (!phone.matches("^1[3-9]\\d{9}$") && !phone.isEmpty()) {
                JOptionPane.showMessageDialog(textField,
                        "请输入有效的手机号码（11位数字）",
                        "输入错误",
                        JOptionPane.ERROR_MESSAGE);
                textField.requestFocus();
                return false;
            }

            return super.stopCellEditing();
        }
    }
}