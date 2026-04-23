package com.absensi.absensi_app.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Optional;

@Getter
public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final Object errorData;

    public ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.errorData = null;
    }

    public ApiException(String message, HttpStatus status, Object errorData) {
        super(message);
        this.status = status;
        this.errorData = errorData;
    }

}
