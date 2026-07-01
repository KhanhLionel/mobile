package com.example.sms.models;

public class Grade {
    private double diemThanhPhan;
    private double diemGiuaKy;
    private double diemCuoiKy;
    private double gpaMon;

    public Grade() {
    }

    public Grade(double diemThanhPhan, double diemGiuaKy, double diemCuoiKy) {
        this.diemThanhPhan = diemThanhPhan;
        this.diemGiuaKy = diemGiuaKy;
        this.diemCuoiKy = diemCuoiKy;
        calculateGpaMon();
    }

    public void calculateGpaMon() {
        // Ví dụ: 20% thành phần, 30% giữa kỳ, 50% cuối kỳ
        this.gpaMon = (this.diemThanhPhan * 0.2) + (this.diemGiuaKy * 0.3) + (this.diemCuoiKy * 0.5);
    }

    public double getDiemThanhPhan() { return diemThanhPhan; }
    public void setDiemThanhPhan(double diemThanhPhan) { this.diemThanhPhan = diemThanhPhan; calculateGpaMon(); }

    public double getDiemGiuaKy() { return diemGiuaKy; }
    public void setDiemGiuaKy(double diemGiuaKy) { this.diemGiuaKy = diemGiuaKy; calculateGpaMon(); }

    public double getDiemCuoiKy() { return diemCuoiKy; }
    public void setDiemCuoiKy(double diemCuoiKy) { this.diemCuoiKy = diemCuoiKy; calculateGpaMon(); }

    public double getGpaMon() { return gpaMon; }
    public void setGpaMon(double gpaMon) { this.gpaMon = gpaMon; }
}
