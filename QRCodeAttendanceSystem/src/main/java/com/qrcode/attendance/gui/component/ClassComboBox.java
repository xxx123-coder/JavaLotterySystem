package com.qrcode.attendance.gui.component;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.ArrayList;

/**
 * 班级选择下拉框组件
 */
public class ClassComboBox extends JComboBox<ClassInfo> {
    private List<ClassInfo> allClasses;
    private final JTextField editorField;
    private boolean loading = false;

    public ClassComboBox() {
        setRenderer(new ClassInfoRenderer());
        setEditable(true);

        editorField = (JTextField) getEditor().getEditorComponent();
        setupSearch();
        loadClasses();
    }

    /**
     * 加载所有班级
     */
    private void loadClasses() {
        loading = true;
        allClasses = new ArrayList<>();

        allClasses.add(new ClassInfo("CS101", "计算机科学1班", "张老师", 45));
        allClasses.add(new ClassInfo("CS102", "计算机科学2班", "李老师", 42));
        allClasses.add(new ClassInfo("MATH201", "高等数学A班", "王老师", 50));
        allClasses.add(new ClassInfo("ENG101", "英语1班", "刘老师", 38));

        updateComboBox(allClasses);
        loading = false;
    }

    /**
     * 设置搜索功能
     */
    private void setupSearch() {
        editorField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!loading) filterClasses();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!loading) filterClasses();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (!loading) filterClasses();
            }
        });
    }

    /**
     * 过滤班级
     */
    private void filterClasses() {
        String searchText = editorField.getText().toLowerCase();
        List<ClassInfo> filtered = new ArrayList<>();

        for (ClassInfo classInfo : allClasses) {
            if (classInfo.getClassCode().toLowerCase().contains(searchText) ||
                    classInfo.getClassName().toLowerCase().contains(searchText) ||
                    classInfo.getTeacher().toLowerCase().contains(searchText)) {
                filtered.add(classInfo);
            }
        }

        updateComboBox(filtered);

        if (!searchText.isEmpty()) {
            showPopup();
        }
    }

    /**
     * 更新下拉框数据
     */
    private void updateComboBox(List<ClassInfo> classes) {
        loading = true;
        removeAllItems();

        addItem(new ClassInfo("", "--- 已存在班级 ---", "", 0));

        for (ClassInfo classInfo : classes) {
            addItem(classInfo);
        }

        addItem(new ClassInfo("", "--- 快速添加 ---", "", 0));
        loading = false;
    }

    /**
     * 获取选中的班级信息
     */
    public ClassInfo getSelectedClassInfo() {
        Object selected = getSelectedItem();
        return (selected instanceof ClassInfo) ? (ClassInfo) selected : null;
    }

    /**
     * 自定义渲染器 - 改为静态内部类
     */
    private static class ClassInfoRenderer extends BasicComboBoxRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {

            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof ClassInfo) {
                ClassInfo classInfo = (ClassInfo) value;
                if (classInfo.getClassCode().isEmpty()) {
                    setText(classInfo.getClassName());
                    setFont(getFont().deriveFont(Font.BOLD));
                    setForeground(Color.GRAY);
                    setBackground(isSelected ? list.getSelectionBackground() : Color.LIGHT_GRAY);
                } else {
                    setText(String.format("%s - %s (%s, %d人)",
                            classInfo.getClassCode(),
                            classInfo.getClassName(),
                            classInfo.getTeacher(),
                            classInfo.getClassSize()));

                    if (classInfo.getClassSize() > 45) {
                        setIcon(new ClassSizeIcon(Color.RED));
                    } else if (classInfo.getClassSize() > 35) {
                        setIcon(new ClassSizeIcon(Color.ORANGE));
                    } else {
                        setIcon(new ClassSizeIcon(Color.GREEN));
                    }
                }
            }

            return this;
        }
    }

    /**
     * 班级人数图标 - 改为静态内部类
     */
    private static class ClassSizeIcon implements Icon {
        private final Color color;

        public ClassSizeIcon(Color color) {
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(color);
            g.fillRect(x, y, 8, 8);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, 8, 8);
        }

        @Override
        public int getIconWidth() {
            return 10;
        }

        @Override
        public int getIconHeight() {
            return 10;
        }
    }
}

/**
 * 班级信息数据类
 */
class ClassInfo {
    private final String classCode;
    private final String className;
    private final String teacher;
    private final int classSize;

    public ClassInfo(String classCode, String className, String teacher, int classSize) {
        this.classCode = classCode;
        this.className = className;
        this.teacher = teacher;
        this.classSize = classSize;
    }

    public String getClassCode() { return classCode; }
    public String getClassName() { return className; }
    public String getTeacher() { return teacher; }
    public int getClassSize() { return classSize; }

    @Override
    public String toString() {
        return className;
    }
}