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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponse findById(Long id) {

        log.info("FIND_BY_ID_START | UserId: [{}]", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    String errorMessage = "User tidak ditemukan!";

                    log.error("FIND_BY_ID_ERROR | [{}]", errorMessage);

                    return new ApiException(errorMessage, HttpStatus.NOT_FOUND);
                });

        log.info("FIND_BY_ID_SUCCESS | UserData : [{}]", user);

        return userMapper.toResponse(user);
    }

    @Override
    public PageResponse<UserResponse> findAll(int page, int size) {

        log.info("FIND_ALL_START | page : [{}], size :[{}]", page, size);

        Pageable pageable = PageRequest.of(page - 1, size);

        log.debug("FIND_ALL | pageable : [{}]", pageable);

        Page<User> users = userRepository.findAll(pageable);
        Page<UserResponse> userResponses = users.map(userMapper::toResponse);

        log.info("FIND_ALL_SUCCESS | user : [{}]", users);

        return PaginationMapper.of(userResponses);
    }

    @Transactional
    @Override
    public void delete(Long id) {

        log.info("DELETE_START | userId : [{}]", id);

        User user = userRepository.findById(id).orElseThrow(() -> {
            String errorMessage = "User tidak ditemukan!";

            log.debug("DELETE | [{}]", errorMessage);

            return new ApiException(errorMessage, HttpStatus.NOT_FOUND);
        });

        userRepository.delete(user);

        log.info("DELETE_SUCCESS | [{}]", id);
    }

    @Transactional
    @Override
    public UserResponse update(Long id, RegisterRequest request) {

        log.info("UPDATE_START | id : [{}], request : [{}]", id, request);

        User user = userRepository.findById(id).orElseThrow(() -> {
            String errorMessage = "User tidak ditemukan!";

            log.error("UPDATE_ERROR | {}", errorMessage);

            return new ApiException(errorMessage, HttpStatus.NOT_FOUND);
        });

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        User updatedUser = userRepository.save(user);

        log.info("UPDATE_SUCCESS | User : [{}]", updatedUser);

        return userMapper.toResponse(updatedUser);
    }
}
