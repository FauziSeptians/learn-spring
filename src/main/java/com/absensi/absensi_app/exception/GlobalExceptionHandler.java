package com.absensi.absensi_app.exception;

import com.absensi.absensi_app.dto.response.ApiResponse;
import com.absensi.absensi_app.util.ApiMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Object>> handleApiException(ApiException e) {
        return ResponseEntity
            .status(e.getStatus())
            .body(ApiMapper.error(e.getStatus().value(), e.getMessage()));
    }

    // ✅ 403 - Tidak punya akses
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleForbiddenException(
        AccessDeniedException e
    ) {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ApiMapper.error(403, "Akses ditolak"));
    }

    // ✅ 401 - Belum login / token invalid
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(
        AuthenticationException e
    ) {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiMapper.error(401, "Unauthorized: " + e.getMessage()));
    }

    // ✅ 401 - Username/password salah
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentialsException(
        BadCredentialsException e
    ) {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiMapper.error(401, "Username atau password salah"));
    }

    // ✅ 500 - Internal server error
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException e) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiMapper.error(500, "Terjadi kesalahan internal: " + e.getMessage()));
    }
}
