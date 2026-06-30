package com.example.studentmanagenment.api;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    // 1. Lấy danh sách sinh viên (Thành viên 2 dùng)
    @GET("students.json")
    Call<Map<String, Student>> getStudents();

    // 2. Thêm một sinh viên mới (Thành viên 3 dùng)
    @POST("students.json")
    Call<Map<String, String>> addStudent(@Body Student student);

    // 3. Cập nhật thông tin sinh viên theo ID (Thành viên 3 hoặc 4 dùng)
    @PUT("students/{id}.json")
    Call<Student> updateStudent(@Path("id") String id, @Body Student student);

    // 4. Xóa sinh viên (Thành viên 3 dùng)
    @DELETE("students/{id}.json")
    Call<Void> deleteStudent(@Path("id") String id);
}