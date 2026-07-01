package com.example.studentmanagenment.api;

import com.example.studentmanagenment.score_and_class.StudentScore;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    // 1. Hàm lấy danh sách sinh viên phục vụ cho Thành viên 2 (Đổ vào RecyclerView)
    @GET("students")
    Call<List<Student>> getAllStudents();

    // 2. Hàm thêm một sinh viên mới phục vụ cho Thành viên 3
    @POST("students")
    Call<Student> createStudent(@Body Student student);


    //Cập nhật và nhận vào danh sách điểm
    @GET("students/{id}/scores")
    Call<List<StudentScore>> getStudentScores(@Path("id") String studentId);
    @PUT("students/{id}/scores/{scoreId}")
    Call<Void> updateStudentScore(
            @Path("id") String studentId,
            @Path("scoreId") String scoreId,
            @Body StudentScore score
    );
}