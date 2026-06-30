package com.example.studentmanagenment.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "https://studentmanagenment-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private static Retrofit retrofit = null;

    public static ApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create()) // Tự động chuyển JSON sang Object
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}
