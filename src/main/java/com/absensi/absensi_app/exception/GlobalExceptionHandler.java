package com.absensi.absensi_app.exception;

import com.absensi.absensi_app.dto.response.ApiResponse;
import com.absensi.absensi_app.util.ApiMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ApiResponse<Object>> handleApiException(ApiException e) {
    return ResponseEntity.status(e.getStatus())
        .body(ApiMapper.error(e.getStatus().value(), e.getMessage()));
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiResponse<Object>> handleNotFound(ResourceNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ApiMapper.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ApiResponse<Object>> handleBadRequest(BadRequestException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiMapper.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException e) {
    log.error("Internal Server Error: ", e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            ApiMapper.error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Terjadi kesalahan internal: " + e.getMessage()));
  }
}
