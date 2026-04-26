package com.absensi.absensi_app.exception;

import com.absensi.absensi_app.dto.response.ApiResponse;
import com.absensi.absensi_app.util.ApiMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Penangan exception global yang terpusat untuk seluruh aplikasi.
 *
 * <p>Class ini mengimplementasikan pola <b>Global Exception Handling</b> menggunakan
 * Spring MVC. Tanpa class ini, setiap exception yang tidak ditangani akan menghasilkan
 * response error default dari Spring (yang berisi stack trace HTML) atau response 500 yang tidak informatif.</p>
 *
 * <p>Dengan adanya class ini, <b>semua exception</b> akan ditangkap di satu tempat dan
 * di-transformasi menjadi response JSON yang konsisten menggunakan {@link ApiResponse}.</p>
 *
 * <p><b>Annotation yang digunakan:</b>
 * <ul>
 *   <li>{@code @Slf4j} - Membuat logger {@code log} secara otomatis via Lombok.</li>
 *   <li>{@code @RestControllerAdvice} - Gabungan dari {@code @ControllerAdvice} dan
 *       {@code @ResponseBody}. Menandai class ini sebagai komponen yang akan mengintersepsi
 *       exception yang dilempar oleh layer Controller di seluruh aplikasi. Response dari
 *       class ini akan otomatis di-serialize menjadi JSON (karena {@code @ResponseBody}).</li>
 *   <li>{@code @ExceptionHandler(X.class)} - Menandai sebuah method sebagai handler untuk
 *       tipe exception tertentu {@code X}. Ketika exception bertipe {@code X} dilempar
 *       dari mana saja di layer controller/service, Spring akan merutekannya ke method ini.</li>
 * </ul>
 *
 * @author  Fauzi Septian
 * @version 1.0
 * @see     ApiException
 * @see     ApiResponse
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Menangani {@link ApiException}, yaitu custom exception milik aplikasi ini.
     *
     * <p>{@link ApiException} dilempar secara sengaja dari layer service ketika terjadi
     * kondisi bisnis yang tidak valid (misal: user tidak ditemukan, sudah absen, dll.).
     * Handler ini mengekstrak HTTP status dan pesan dari exception tersebut untuk
     * mengembalikan response error yang tepat dan informatif.</p>
     *
     * @param e exception yang ditangkap, berisi status HTTP, pesan, dan optional error data.
     * @return {@link ResponseEntity} dengan status dan body error sesuai isi exception.
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Object>> handleApiException(ApiException e) {
        log.error("API_EXCEPTION | Error : [{}]", e.getMessage());
        return ResponseEntity
            .status(e.getStatus())
            .body(ApiMapper.error(e.getStatus().value(), e.getMessage(), e.getErrorData()));
    }

    /**
     * Menangani {@link AccessDeniedException} (HTTP 403 Forbidden).
     *
     * <p>Exception ini dilempar oleh Spring Security ketika pengguna yang sudah terautentikasi
     * mencoba mengakses sumber daya yang tidak memiliki izin untuk diaksesnya
     * (misal: user biasa mencoba mengakses endpoint admin).</p>
     *
     * @param e exception akses ditolak dari Spring Security.
     * @return {@link ResponseEntity} dengan status 403 dan pesan error standar.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleForbiddenException(AccessDeniedException e) {
        log.error("ACCESS_DENIED_EXCEPTION | Error : [{}]", e.getMessage());
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ApiMapper.error(403, "Akses ditolak"));
    }

    /**
     * Menangani {@link AuthenticationException} (HTTP 401 Unauthorized).
     *
     * <p>Exception ini dilempar oleh Spring Security ketika autentikasi gagal secara umum,
     * misalnya saat token JWT tidak valid, sudah kedaluwarsa, atau tidak ada token sama sekali
     * pada endpoint yang membutuhkan autentikasi.</p>
     *
     * @param e exception autentikasi dari Spring Security.
     * @return {@link ResponseEntity} dengan status 401 dan pesan Unauthorized.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(AuthenticationException e) {
        log.error("AUTHENTICATION_EXCEPTION | Error : [{}]", e.getMessage());
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiMapper.error(401, "Unauthorized: " + e.getMessage()));
    }

    /**
     * Menangani {@link BadCredentialsException} (HTTP 401 Unauthorized).
     *
     * <p>Exception khusus ini dilempar oleh {@code AuthenticationManager} Spring Security
     * ketika proses autentikasi gagal akibat kredensial yang salah (email atau password tidak cocok).
     * Dipisahkan dari {@link AuthenticationException} agar pesannya bisa lebih spesifik.</p>
     *
     * @param e exception bad credentials dari Spring Security.
     * @return {@link ResponseEntity} dengan status 401 dan pesan error yang ramah pengguna.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentialsException(BadCredentialsException e) {
        log.error("BAD_CREDENTIAL_EXCEPTION | Error : [{}]", e.getMessage());
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiMapper.error(401, "Username atau password salah"));
    }

    /**
     * Menangani {@link RuntimeException} yang tidak terduga (HTTP 500 Internal Server Error).
     *
     * <p>Handler ini berfungsi sebagai "safety net" terakhir. Jika ada exception yang tidak
     * ditangani oleh handler-handler di atas, maka handler ini yang akan menangkapnya dan
     * mengembalikan response 500 yang terkontrol, alih-alih membiarkan Spring mengembalikan
     * stack trace yang membocorkan detail internal aplikasi.</p>
     *
     * @param e runtime exception yang tidak terduga.
     * @return {@link ResponseEntity} dengan status 500 dan pesan error generik.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException e) {
        log.error("RUNTIME_EXCEPTION | Error : [{}]", e.getMessage());
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiMapper.error(500, "Terjadi kesalahan internal: " + e.getMessage()));
    }
}
