package com.absensi.absensi_app.services;

import com.absensi.absensi_app.dto.request.RegisterRequest;
import com.absensi.absensi_app.dto.response.UserResponse;
import com.absensi.absensi_app.entity.User;
import jakarta.transaction.Transactional;
import jakarta.websocket.MessageHandler;

import java.util.List;

public interface UserService {
    List<UserResponse> findAll();
    UserResponse findById(Long Id);
    void delete(Long id);
    UserResponse update(Long id, RegisterRequest request);
}
