package com.qrcode.attendance.gui.component;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 自定义签到记录表格组件
 */
public class AttendanceTable extends JTable {
    private AttendanceTableModel model;
    private final TableRowSorter<AttendanceTableModel> sorter;
    private List<AttendanceRecord> attendanceData;

    // 状态颜色定义
    private static final Color NORMAL_COLOR = new Color(220, 255, 220);
    private static final Color LATE_COLOR = new Color(255, 255, 200);
    private static final Color ABSENT_COLOR = new Color(255, 220, 220);

    public AttendanceTable() {
        // 直接初始化 model，而不是在 initTable() 中
        model = new AttendanceTableModel();
        sorter = new TableRowSorter<>(model);
        initTable();
        setupRenderer();
    }

    private void initTable() {
        setModel(model);
        setRowSorter(sorter);

        // 设置列宽
        getColumnModel().getColumn(0).setPreferredWidth(80);  // 学号
        getColumnModel().getColumn(1).setPreferredWidth(100); // 姓名
        getColumnModel().getColumn(2).setPreferredWidth(100); // 班级
        getColumnModel().getColumn(3).setPreferredWidth(150); // 签到时间
        getColumnModel().getColumn(4).setPreferredWidth(80);  // 状态

        // 启用排序
        setAutoCreateRowSorter(true);

        // 启用多选
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // 添加行选择监听
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        viewAttendanceDetails(row);
                    }
                }
            }
        });
    }

    private void setupRenderer() {
        // 设置自定义渲染器
        setDefaultRenderer(Object.class, new AttendanceTableCellRenderer());
        setDefaultRenderer(Date.class, new DateCellRenderer());
    }

    /**
     * 加载签到数据
     */
    public void loadAttendanceData(List<AttendanceRecord> data) {
        this.attendanceData = data;
        model.setData(data);
    }

    /**
     * 筛选数据
     */
    public void filterData(String filterText) {
        if (filterText.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            RowFilter<AttendanceTableModel, Object> rf = RowFilter.regexFilter("(?i)" + filterText);
            sorter.setRowFilter(rf);
        }
    }

    /**
     * 获取选中的签到记录
     */
    public List<AttendanceRecord> getSelectedAttendanceRecords() {
        int[] selectedRows = getSelectedRows();
        List<AttendanceRecord> selected = new ArrayList<>();

        if (attendanceData == null) {
            return selected;
        }

        for (int row : selectedRows) {
            int modelRow = convertRowIndexToModel(row);
            if (modelRow >= 0 && modelRow < attendanceData.size()) {
                selected.add(attendanceData.get(modelRow));
            }
        }

        return selected;
    }

    private void viewAttendanceDetails(int row) {
        if (attendanceData == null || row < 0 || row >= attendanceData.size()) {
            return;
        }

        AttendanceRecord record = attendanceData.get(row);
        JOptionPane.showMessageDialog(this,
                "签到详情：\n" +
                        "学号：" + record.getStudentId() + "\n" +
                        "姓名：" + record.getStudentName() + "\n" +
                        "签到时间：" + (record.getCheckInTime() != null ?
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(record.getCheckInTime()) : "未签到") + "\n" +
                        "状态：" + record.getStatus(),
                "签到详情", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 获取表格模型（供外部使用）
     */
    public AttendanceTableModel getTableModel() {
        return model;
    }

    /**
     * 获取表格数据（供外部使用）
     */
    public List<AttendanceRecord> getAttendanceData() {
        return attendanceData;
    }

    /**
     * 表格模型
     */
    public class AttendanceTableModel extends AbstractTableModel {
        private List<AttendanceRecord> data;
        private final String[] columnNames = {"学号", "姓名", "班级", "签到时间", "状态"};

        public void setData(List<AttendanceRecord> data) {
            this.data = data;
            fireTableDataChanged();
        }

        public List<AttendanceRecord> getData() {
            return data;
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

            AttendanceRecord record = data.get(rowIndex);

            // 使用 switch 语句替代 if-else
            switch (columnIndex) {
                case 0:
                    return record.getStudentId();
                case 1:
                    return record.getStudentName();
                case 2:
                    return record.getClassName();
                case 3:
                    return record.getCheckInTime();
                case 4:
                    return record.getStatus();
                default:
                    return null;
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 3:
                    return Date.class;
                default:
                    return String.class;
            }
        }
    }

    /**
     * 自定义单元格渲染器
     */
    private class AttendanceTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);

            // 根据状态设置背景色
            if (column == 4 && value instanceof String) {
                String status = (String) value;

                // 使用 switch 语句
                switch (status) {
                    case "正常":
                        c.setBackground(NORMAL_COLOR);
                        break;
                    case "迟到":
                        c.setBackground(LATE_COLOR);
                        break;
                    case "缺勤":
                        c.setBackground(ABSENT_COLOR);
                        break;
                    default:
                        c.setBackground(Color.WHITE);
                }

                // 添加状态图标
                if (c instanceof JLabel) {
                    JLabel label = (JLabel) c;
                    switch (status) {
                        case "正常":
                            label.setIcon(new StatusIcon(Color.GREEN));
                            break;
                        case "迟到":
                            label.setIcon(new StatusIcon(Color.YELLOW));
                            break;
                        case "缺勤":
                            label.setIcon(new StatusIcon(Color.RED));
                            break;
                    }
                }
            } else {
                c.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            }

            return c;
        }
    }

    /**
     * 日期单元格渲染器
     */
    private class DateCellRenderer extends DefaultTableCellRenderer {
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        @Override
        protected void setValue(Object value) {
            if (value instanceof Date) {
                Date date = (Date) value;
                setText(dateFormat.format(date));
            } else {
                super.setValue(value);
            }
        }
    }

    /**
     * 状态图标
     */
    private class StatusIcon implements Icon {
        private final Color color;

        public StatusIcon(Color color) {
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(color);
            g.fillOval(x, y, 10, 10);
            g.setColor(Color.BLACK);
            g.drawOval(x, y, 10, 10);
        }

        @Override
        public int getIconWidth() {
            return 12;
        }

        @Override
        public int getIconHeight() {
            return 12;
        }
    }
}