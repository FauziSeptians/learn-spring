package com.absensi.absensi_app.exception;

import com.absensi.absensi_app.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationErrors(
        MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
            .getAllErrors()
            .forEach(error -> {
                String fieldName = ((FieldError) error).getField();
                String errorMessage = error.getDefaultMessage();
                errors.put(fieldName, errorMessage);
            });

        ApiResponse<Object> response = ApiResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .message("Validasi gagal")
            .errorData(errors)
            .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(
        RuntimeException ex) {

        ApiResponse<Object> response = ApiResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .message(ex.getMessage())
            .errorData(null)
            .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(
        Exception ex) {

        ApiResponse<Object> response = ApiResponse.builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .message("Terjadi kesalahan pada server")
            .errorData(ex.getMessage())
            .build();

        return ResponseEntity.internalServerError().body(response);
    }
}
