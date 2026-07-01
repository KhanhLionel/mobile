package com.example.sms.api;

public interface ApiCallback<T> {
    void onSuccess(T result);
    void onFailure(Exception e);
}
