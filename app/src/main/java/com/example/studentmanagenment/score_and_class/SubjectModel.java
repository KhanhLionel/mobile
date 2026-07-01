package com.example.studentmanagenment.score_and_class; // Thay đổi package cho đúng với cấu trúc của bạn nếu cần

public class SubjectModel {
    private String subjectId;
    private String subjectName;
    private int credits;

    // 3 biến cấu hình phần trăm điểm mới
    private int percentProcess;
    private int percentMidterm;
    private int percentFinal;

    // Constructor rỗng bắt buộc phải có cho Firebase Realtime Database
    public SubjectModel() {
    }

    public SubjectModel(String subjectId, String subjectName, int credits, int percentProcess, int percentMidterm, int percentFinal) {
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.credits = credits;
        this.percentProcess = percentProcess;
        this.percentMidterm = percentMidterm;
        this.percentFinal = percentFinal;
    }

    // Toàn bộ các hàm Getter và Setter
    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public int getPercentProcess() { return percentProcess; }
    public void setPercentProcess(int percentProcess) { this.percentProcess = percentProcess; }

    public int getPercentMidterm() { return percentMidterm; }
    public void setPercentMidterm(int percentMidterm) { this.percentMidterm = percentMidterm; }

    public int getPercentFinal() { return percentFinal; }
    public void setPercentFinal(int percentFinal) { this.percentFinal = percentFinal; }
}