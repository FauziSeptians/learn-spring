package com.absensi.absensi_app.services.impl;


import com.absensi.absensi_app.dto.request.LoginRequest;
import com.absensi.absensi_app.dto.request.RegisterRequest;
import com.absensi.absensi_app.dto.response.LoginResponse;
import com.absensi.absensi_app.dto.response.UserResponse;
import com.absensi.absensi_app.entity.User;
import com.absensi.absensi_app.enums.Role;
import com.absensi.absensi_app.repository.UserRepository;
import com.absensi.absensi_app.services.AuthService;
import com.absensi.absensi_app.util.UserMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private  final UserRepository userRepository;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {

        if(userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email sudah terdaftar!");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder().name(request.getName()).email(request.getEmail()).password(hashedPassword).role(Role.EMPLOYEE).build();

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        boolean isUserExist = userRepository.existsByEmail(request.getEmail());

        if(!isUserExist){
            throw  new RuntimeException("User tidak terdaftar");
        }

        User user = userRepository.findByEmail(request.getEmail());
        String password = user.getPassword();

        if(passwordEncoder.matches(password, request.getPassword())){
            throw new RuntimeException("Data yang kamu masukan salah!");
        }

        return LoginResponse.builder().email(user.getEmail()).name(user.getName()).token("LOREM").build();
    }
}
