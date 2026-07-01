package com.example.sms.models;

public class Subject {
    private String id;
    private String tenMonHoc;
    private String giangVien;
    private String caHoc;

    public Subject() {
    }

    public Subject(String id, String tenMonHoc, String giangVien, String caHoc) {
        this.id = id;
        this.tenMonHoc = tenMonHoc;
        this.giangVien = giangVien;
        this.caHoc = caHoc;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTenMonHoc() {
        return tenMonHoc;
    }

    public void setTenMonHoc(String tenMonHoc) {
        this.tenMonHoc = tenMonHoc;
    }

    public String getGiangVien() {
        return giangVien;
    }

    public void setGiangVien(String giangVien) {
        this.giangVien = giangVien;
    }

    public String getCaHoc() {
        return caHoc;
    }

    public void setCaHoc(String caHoc) {
        this.caHoc = caHoc;
    }
}
