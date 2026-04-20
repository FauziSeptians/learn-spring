package com.absensi.absensi_app.exception;

import com.absensi.absensi_app.dto.response.ApiResponse;
import com.absensi.absensi_app.util.ApiMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ApiResponse<Object>> handleApiException(ApiException e) {
    return ResponseEntity.status(e.getStatus())
        .body(ApiMapper.error(e.getStatus().value(), e.getMessage()));
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException e) {
    return ResponseEntity.status(500)
        .body(ApiMapper.error(500, "Terjadi kesalahan internal: " + e.getMessage()));
  }
}
