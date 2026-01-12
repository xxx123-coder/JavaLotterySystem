package com.qrcode.attendance.gui.component;

import java.util.Date;

public class AttendanceRecord {
    private final String studentId;
    private String studentName;
    private String className;
    private Date checkInTime;
    private String status;

    public AttendanceRecord(String studentId, String studentName, String className,
                            Date checkInTime, String status) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.className = className;
        this.checkInTime = checkInTime;
        this.status = status;
    }

    public String getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public String getClassName() { return className; }
    public Date getCheckInTime() { return checkInTime; }
    public String getStatus() { return status; }

    public void setStudentName(String studentName) { this.studentName = studentName; }
    public void setClassName(String className) { this.className = className; }
    public void setCheckInTime(Date checkInTime) { this.checkInTime = checkInTime; }
    public void setStatus(String status) { this.status = status; }
}