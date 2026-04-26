package com.absensi.absensi_app.controller;

import com.absensi.absensi_app.dto.request.LoginRequest;
import com.absensi.absensi_app.dto.request.RegisterRequest;
import com.absensi.absensi_app.dto.response.ApiResponse;
import com.absensi.absensi_app.dto.response.LoginResponse;
import com.absensi.absensi_app.dto.response.UserResponse;
import com.absensi.absensi_app.services.AuthService;
import com.absensi.absensi_app.util.ApiMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller yang menangani endpoint-endpoint untuk autentikasi pengguna.
 *
 * <p>Class ini adalah <b>entry point</b> untuk semua request yang berkaitan dengan
 * registrasi dan login. Endpoint di dalam class ini dikonfigurasi sebagai <b>publik</b>
 * di {@code SecurityConfig}, artinya dapat diakses tanpa token JWT.</p>
 *
 * <p><b>Annotation yang digunakan:</b>
 * <ul>
 *   <li>{@code @RestController} - Gabungan dari {@code @Controller} dan {@code @ResponseBody}.
 *       Menandai class ini sebagai controller Spring MVC dimana setiap method secara otomatis
 *       me-serialize nilai kembaliannya menjadi JSON (menggunakan Jackson).</li>
 *   <li>{@code @RequestMapping("/api/auth")} - Mendefinisikan base URL untuk semua endpoint
 *       dalam class ini. Semua path di dalam class ini akan diawali dengan {@code /api/auth}.</li>
 *   <li>{@code @RequiredArgsConstructor} - Constructor Injection untuk {@link AuthService}
 *       via Lombok, menghindari field injection yang kurang direkomendasikan.</li>
 *   <li>{@code @Tag} - Anotasi Swagger/OpenAPI untuk mengelompokkan endpoint di dokumentasi
 *       API pada Swagger UI ({@code /swagger-ui.html}).</li>
 * </ul>
 *
 * @author  Fauzi Septian
 * @version 1.0
 * @see     AuthService
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "API untuk authentication (Register & Login)")
public class AuthController {

    /**
     * Service bisnis autentikasi, diinjeksikan melalui Constructor Injection.
     * Controller tidak mengandung logika bisnis; ia hanya mendelegasikan ke service.
     */
    private final AuthService authService;

    /**
     * Mendaftarkan pengguna baru ke dalam sistem.
     *
     * <p><b>Annotation method:</b>
     * <ul>
     *   <li>{@code @PostMapping("/register")} - Mengikat method ini ke HTTP POST request
     *       pada URL {@code /api/auth/register}.</li>
     *   <li>{@code @Valid} - Memicu validasi Bean Validation pada {@link RegisterRequest}
     *       sebelum method dieksekusi. Jika ada field yang tidak valid (misal: email kosong,
     *       password terlalu pendek), Spring akan mengembalikan error 400 secara otomatis.</li>
     *   <li>{@code @RequestBody} - Memerintahkan Spring untuk men-deserialize body JSON
     *       dari request menjadi objek {@link RegisterRequest}.</li>
     *   <li>{@code @Operation} - Metadata Swagger untuk mendokumentasikan endpoint ini di UI.</li>
     * </ul>
     *
     * @param request DTO berisi data registrasi yang sudah divalidasi.
     * @return {@link ResponseEntity} dengan status 200 OK dan data user yang baru dibuat.
     */
    @PostMapping("/register")
    @Operation(summary = "Register user baru", description = "Membuat akun user baru dengan role EMPLOYEE")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse user = authService.register(request);
        return ResponseEntity.ok(ApiMapper.success("Sukses membuat user baru", user));
    }

    /**
     * Melakukan proses login dan menghasilkan JWT token untuk sesi berikutnya.
     *
     * @param request DTO berisi kredensial login (email, password) yang sudah divalidasi.
     * @return {@link ResponseEntity} dengan status 200 OK dan objek {@link LoginResponse}
     *         yang berisi JWT token siap digunakan.
     */
    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Melakukan login dan mendapatkan JWT token")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse loginRes = authService.login(request);
        return ResponseEntity.ok(ApiMapper.success("Sukses melakukan login", loginRes));
    }
}
