package com.example.studentmanagenment.score_and_class;

public class StudentScore {
    private String studentId;     // Mã số sinh viên (MSSV)
    private String studentName;   // Họ và tên
    private double processScore;  // Điểm quá trình (QT)
    private double midtermScore;  // Điểm giữa kỳ (GK)
    private double finalScore;    // Điểm cuối kỳ (CK)

    // Constructor rỗng bắt buộc phải có cho Firebase Realtime Database
    public StudentScore() {
    }

    public StudentScore(String studentId, String studentName, double processScore, double midtermScore, double finalScore) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.processScore = processScore;
        this.midtermScore = midtermScore;
        this.finalScore = finalScore;
    }

    // HÀM TÍNH ĐTB ĐỘNG THEO PHẦN TRĂM CẤU HÌNH (MỚI THÊM VÀO)
    public double getCalculatedAvg(int pctQT, int pctGK, int pctCK) {
        double avg = (this.processScore * pctQT / 100.0)
                + (this.midtermScore * pctGK / 100.0)
                + (this.finalScore * pctCK / 100.0);
        return Math.round(avg * 10.0) / 10.0; // Làm tròn 1 chữ số thập phân
    }

    // Các hàm Getter và Setter dùng chung cho toàn hệ thống
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public double getProcessScore() { return processScore; }
    public void setProcessScore(double processScore) { this.processScore = processScore; }

    public double getMidtermScore() { return midtermScore; }
    public void setMidtermScore(double midtermScore) { this.midtermScore = midtermScore; }

    public double getFinalScore() { return finalScore; }
    public void setFinalScore(double finalScore) { this.finalScore = finalScore; }
}