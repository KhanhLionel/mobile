package com.example.sms.models;

public class Student {
    private String id;
    private String ten;
    private String mssv;
    private String email;
    private String tenLop;
    private double gpaTong;

    public Student() {
    }

    public Student(String id, String ten, String mssv, String email, String tenLop) {
        this.id = id;
        this.ten = ten;
        this.mssv = mssv;
        this.email = email;
        this.tenLop = tenLop;
        this.gpaTong = 0.0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTen() {
        return ten;
    }

    public void setTen(String ten) {
        this.ten = ten;
    }

    public String getMssv() {
        return mssv;
    }

    public void setMssv(String mssv) {
        this.mssv = mssv;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTenLop() {
        return tenLop;
    }

    public void setTenLop(String tenLop) {
        this.tenLop = tenLop;
    }

    public double getGpaTong() {
        return gpaTong;
    }

    public void setGpaTong(double gpaTong) {
        this.gpaTong = gpaTong;
    }
}
