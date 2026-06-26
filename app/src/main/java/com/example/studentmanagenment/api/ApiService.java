package com.example.studentmanagenment.api;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    // 1. Hàm lấy danh sách sinh viên phục vụ cho Thành viên 2 (Đổ vào RecyclerView)
    @GET("students")
    Call<List<Student>> getAllStudents();

    // 2. Hàm thêm một sinh viên mới phục vụ cho Thành viên 3
    @POST("students")
    Call<Student> createStudent(@Body Student student);
}