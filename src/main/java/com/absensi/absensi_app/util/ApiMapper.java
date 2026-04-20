package com.absensi.absensi_app.util;

import com.absensi.absensi_app.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ApiMapper {

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .status(HttpStatus.OK.value())
                .message(message)
                .data(data)
                .errorData(null)
                .build();
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return ApiResponse.<T>builder()
                .status(HttpStatus.CREATED.value())
                .message(message)
                .data(data)
                .errorData(null)
                .build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .status(HttpStatus.OK.value())
                .message(message)
                .data(null)
                .errorData(null)
                .build();
    }

    public static <T> ApiResponse<T> error(int status, String message, Object errorData) {
        return ApiResponse.<T>builder()
                .status(status)
                .message(message)
                .data(null)
                .errorData(errorData)
                .build();
    }

    public static <T> ApiResponse<T> error(int status, String message) {
        return ApiResponse.<T>builder()
                .status(status)
                .message(message)
                .data(null)
                .errorData(null)
                .build();
    }
}