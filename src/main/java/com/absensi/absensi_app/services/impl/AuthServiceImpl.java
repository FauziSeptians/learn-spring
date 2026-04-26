package com.absensi.absensi_app.services.impl;

import com.absensi.absensi_app.dto.request.LoginRequest;
import com.absensi.absensi_app.dto.request.RegisterRequest;
import com.absensi.absensi_app.dto.response.LoginResponse;
import com.absensi.absensi_app.dto.response.UserResponse;
import com.absensi.absensi_app.entity.User;
import com.absensi.absensi_app.enums.Role;
import com.absensi.absensi_app.exception.ApiException;
import com.absensi.absensi_app.repository.UserRepository;
import com.absensi.absensi_app.services.AuthService;
import com.absensi.absensi_app.services.JwtService;
import com.absensi.absensi_app.annotation.LogExecutionTime;
import com.absensi.absensi_app.util.UserMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Implementasi dari {@link AuthService} yang mengelola logika autentikasi dan registrasi pengguna.
 *
 * <p>Class ini bertanggung jawab atas dua operasi kritis dalam sistem keamanan:
 * <ul>
 *   <li><b>Register</b>: Membuat akun baru dengan password yang di-hash menggunakan BCrypt.</li>
 *   <li><b>Login</b>: Memverifikasi kredensial dan menghasilkan JWT untuk sesi berikutnya.</li>
 * </ul>
 *
 * <p><b>Dependency yang diinjeksikan dan perannya:</b>
 * <ul>
 *   <li>{@link UserRepository}: Akses database untuk cek email dan simpan user baru.</li>
 *   <li>{@link UserMapper}: Transformasi dari entity {@link User} ke DTO {@link UserResponse}.</li>
 *   <li>{@link PasswordEncoder}: BCrypt encoder untuk hashing password (dari {@code SecurityConfig}).</li>
 *   <li>{@link JwtService}: Untuk menghasilkan JWT token setelah login berhasil.</li>
 * </ul>
 *
 * <p><b>Annotation pada class:</b>
 * <ul>
 *   <li>{@code @Service} - Menandai class sebagai Spring Bean di layer bisnis.</li>
 *   <li>{@code @RequiredArgsConstructor} - Constructor Injection via Lombok untuk semua field {@code final}.</li>
 * </ul>
 *
 * @author  Fauzi Septian
 * @version 1.0
 * @see     AuthService
 * @see     JwtService
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Mendaftarkan pengguna baru ke dalam sistem.
     *
     * <p><b>Alur Logika:</b>
     * <ol>
     *   <li>Validasi bahwa email belum terdaftar di database.</li>
     *   <li>Hash password menggunakan {@link PasswordEncoder} (BCrypt).
     *       Password asli tidak pernah disimpan ke database.</li>
     *   <li>Buat entity {@link User} baru menggunakan Builder pattern (Lombok {@code @Builder}).
     *       Role di-hardcode sebagai {@link Role#EMPLOYEE} untuk registrasi publik.</li>
     *   <li>Simpan user ke database dan kembalikan DTO response-nya.</li>
     * </ol>
     *
     * <p><b>Annotation:</b>
     * <ul>
     *   <li>{@code @Transactional} - Memastikan penyimpanan user berjalan dalam satu transaksi.
     *       Jika ada error di tengah proses, seluruh operasi di-rollback.</li>
     *   <li>{@code @LogExecutionTime} - Mencatat waktu eksekusi method ini via AOP.</li>
     * </ul>
     *
     * @param request DTO berisi data registrasi (nama, email, password).
     * @return {@link UserResponse} DTO berisi data user yang baru saja dibuat.
     * @throws ApiException (400) jika email sudah terdaftar di sistem.
     */
    @Override
    @Transactional
    @LogExecutionTime
    public UserResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException("Email sudah terdaftar!", HttpStatus.BAD_REQUEST);
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(hashedPassword)
                .role(Role.EMPLOYEE)
                .build();

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    /**
     * Melakukan proses login dan menghasilkan JWT token.
     *
     * <p><b>Alur Logika:</b>
     * <ol>
     *   <li>Cek keberadaan user berdasarkan email.</li>
     *   <li>Verifikasi password menggunakan {@code passwordEncoder.matches()}.
     *       Method ini membandingkan password teks biasa dari request dengan hash BCrypt di database
     *       secara aman (time-safe comparison).</li>
     *   <li>Jika valid, hasilkan JWT token menggunakan {@link JwtService}.</li>
     *   <li>Kembalikan {@link LoginResponse} berisi token JWT untuk digunakan pada request berikutnya.</li>
     * </ol>
     *
     * @param request DTO berisi kredensial login (email, password).
     * @return {@link LoginResponse} berisi JWT token, nama, dan email user.
     * @throws ApiException (404) jika email tidak terdaftar.
     * @throws ApiException (401) jika password tidak cocok.
     */
    @Override
    @LogExecutionTime
    public LoginResponse login(LoginRequest request) {

        boolean isUserExist = userRepository.existsByEmail(request.getEmail());

        if (!isUserExist) {
            throw new ApiException("User tidak terdaftar", HttpStatus.NOT_FOUND);
        }

        User user = userRepository.findByEmail(request.getEmail());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ApiException("Email atau Password salah!", HttpStatus.UNAUTHORIZED);
        }

        String jwtToken = jwtService.generateToken(user);

        return LoginResponse.builder()
                .email(user.getEmail())
                .name(user.getName())
                .token(jwtToken)
                .build();
    }
}
