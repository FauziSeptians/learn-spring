package com.absensi.absensi_app.services;

import com.absensi.absensi_app.dto.request.RegisterRequest;
import com.absensi.absensi_app.dto.response.PageResponse;
import com.absensi.absensi_app.dto.response.UserResponse;
import com.absensi.absensi_app.entity.User;
import jakarta.transaction.Transactional;
import jakarta.websocket.MessageHandler;
import org.springdoc.core.converters.models.Pageable;

import java.util.List;

public interface UserService {
    UserResponse findById(Long Id);

    PageResponse<UserResponse> findAll(int page, int size);

    void delete(Long id);
    UserResponse update(Long id, RegisterRequest request);
}
