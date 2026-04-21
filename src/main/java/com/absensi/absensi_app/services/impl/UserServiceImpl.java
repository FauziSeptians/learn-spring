package com.absensi.absensi_app.services.impl;

import com.absensi.absensi_app.dto.request.RegisterRequest;
import com.absensi.absensi_app.dto.response.PageResponse;
import com.absensi.absensi_app.dto.response.UserResponse;
import com.absensi.absensi_app.entity.User;
import com.absensi.absensi_app.exception.ApiException;
import com.absensi.absensi_app.repository.UserRepository;
import com.absensi.absensi_app.services.UserService;
import com.absensi.absensi_app.util.PaginationMapper;
import com.absensi.absensi_app.util.UserMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponse findById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException("User tidak ditemukan!", HttpStatus.NOT_FOUND));

        return userMapper.toResponse(user);
    }

    @Override
    public PageResponse<UserResponse> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<User> users = userRepository.findAll(pageable);
        Page<UserResponse> userResponses = users.map(userMapper::toResponse);

        return PaginationMapper.of(userResponses);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ApiException("User tidak ditemukan!", HttpStatus.NOT_FOUND));

        userRepository.delete(user);
    }

    @Transactional
    @Override
    public UserResponse update(Long id, RegisterRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new ApiException("User tidak ditemukan!", HttpStatus.NOT_FOUND));

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        User updatedUser = userRepository.save(user);

        return userMapper.toResponse(updatedUser);
    }
}
