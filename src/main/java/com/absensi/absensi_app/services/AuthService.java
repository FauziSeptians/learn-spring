package com.absensi.absensi_app.services;

import com.absensi.absensi_app.dto.request.LoginRequest;
import com.absensi.absensi_app.dto.request.RegisterRequest;
import com.absensi.absensi_app.dto.response.LoginResponse;
import com.absensi.absensi_app.dto.response.UserResponse;

public interface AuthService {
  UserResponse register(RegisterRequest request);

  LoginResponse login(LoginRequest request);
}
