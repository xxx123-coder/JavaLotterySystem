package com.qrcode.attendance.gui.component;

public class Student {
    private final String studentId;
    private String name;
    private String className;
    private String phone;
    private String photoPath;

    public Student(String studentId, String name, String className, String phone, String photoPath) {
        this.studentId = studentId;
        this.name = name;
        this.className = className;
        this.phone = phone;
        this.photoPath = photoPath;
    }

    public String getStudentId() { return studentId; }
    public String getName() { return name; }
    public String getClassName() { return className; }
    public String getPhone() { return phone; }
    public String getPhotoPath() { return photoPath; }

    public void setName(String name) { this.name = name; }
    public void setClassName(String className) { this.className = className; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
}