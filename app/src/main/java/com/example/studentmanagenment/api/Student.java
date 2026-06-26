package com.example.studentmanagenment.api;

public class Student {
    private String id;
    private String name;
    private String mssv;
    private String email;
    private String className; // tương ứng với trường 'class' trên API của bạn
    private double gpa;

    // Constructor không tham số
    public Student() {}

    // Constructor đầy đủ tham số
    public Student(String id, String name, String mssv, String email, String className, double gpa) {
        this.id = id;
        this.name = name;
        this.mssv = mssv;
        this.email = email;
        this.className = className;
        this.gpa = gpa;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMssv() { return mssv; }
    public void setMssv(String mssv) { this.mssv = mssv; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public double getGpa() { return gpa; }
    public void setGpa(double gpa) { this.gpa = gpa; }
}