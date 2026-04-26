package com.absensi.absensi_app.config;

import com.absensi.absensi_app.dto.response.UserResponse;
import com.absensi.absensi_app.services.JwtService;
import com.absensi.absensi_app.services.impl.UserServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

/**
 * Filter HTTP yang bertanggung jawab untuk memvalidasi JWT (JSON Web Token) pada setiap
 * request yang masuk ke aplikasi.
 *
 * <p>Class ini extend {@link OncePerRequestFilter}, yang merupakan base class dari Spring yang
 * menjamin filter ini hanya dieksekusi <b>satu kali per request</b>, bahkan jika terjadi
 * forward atau include di dalam request yang sama.</p>
 *
 * <p><b>Alur Eksekusi Filter:</b>
 * <ol>
 *   <li>Mengambil header {@code Authorization} dari request.</li>
 *   <li>Jika header tidak ada atau tidak dimulai dengan "Bearer ", request diteruskan
 *       ke filter berikutnya tanpa memblokir (endpoint publik tetap bisa diakses).</li>
 *   <li>Jika header ada, token JWT diekstrak dan di-parse untuk mendapatkan {@code userId}.</li>
 *   <li>Data user dimuat dari database (atau Redis cache) menggunakan {@code userId} tersebut.</li>
 *   <li>Token divalidasi: apakah user ID di dalam token cocok dengan user yang dimuat,
 *       dan apakah token belum kedaluwarsa.</li>
 *   <li>Jika valid, objek {@link UsernamePasswordAuthenticationToken} dibuat dan disimpan
 *       ke dalam {@link SecurityContextHolder}, menandai bahwa request ini sudah terautentikasi.</li>
 *   <li>Request diteruskan ke filter/handler berikutnya dalam chain.</li>
 * </ol>
 *
 * <p><b>Annotation yang digunakan:</b>
 * <ul>
 *   <li>{@code @Slf4j} - Anotasi Lombok yang secara otomatis membuat field logger
 *       ({@code private static final Logger log = LoggerFactory.getLogger(...)}).
 *       Menghilangkan boilerplate code untuk inisialisasi logger.</li>
 *   <li>{@code @Component} - Mendaftarkan class ini sebagai Spring Bean. Diperlukan
 *       agar Spring dapat menemukannya dan mendaftarkannya ke {@link SecurityConfig}.</li>
 *   <li>{@code @RequiredArgsConstructor} - Membuat constructor untuk field {@code final},
 *       memungkinkan Spring melakukan Constructor Injection untuk {@link JwtService}
 *       dan {@link UserServiceImpl}.</li>
 * </ul>
 *
 * @author  Fauzi Septian
 * @version 1.0
 * @see     SecurityConfig
 * @see     JwtService
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Service untuk operasi JWT: ekstrak klaim, generate token, dan validasi token.
     * Diinjeksikan oleh Spring melalui constructor (Constructor Injection).
     */
    private final JwtService jwtService;

    /**
     * Service user, digunakan untuk memuat data user dari database (atau cache Redis)
     * berdasarkan ID yang diekstrak dari token JWT.
     * Diinjeksikan oleh Spring melalui constructor (Constructor Injection).
     */
    private final UserServiceImpl userService;

    /**
     * Metode inti filter yang dieksekusi untuk setiap HTTP request.
     *
     * <p>Anotasi {@code @NonNull} pada parameter memastikan bahwa parameter tersebut
     * tidak null dan memberikan peringatan IDE jika ada yang mencoba memasukkan null.</p>
     *
     * @param request     objek HTTP request yang masuk, berisi semua data request termasuk header.
     * @param response    objek HTTP response yang akan dikirim balik ke klien.
     * @param filterChain rantai filter berikutnya. Harus dipanggil dengan {@code doFilter()}
     *                    untuk meneruskan request ke handler atau filter berikutnya.
     * @throws ServletException jika terjadi error generik di level servlet.
     * @throws IOException      jika terjadi error I/O saat membaca atau menulis request/response.
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");
        final String jwt;
        final String userId;

        // Jika header Authorization tidak ada atau bukan "Bearer token", lewati filter ini.
        // Request akan diteruskan; SecurityConfig akan memutuskan apakah endpoint ini membutuhkan auth.
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }

        // Potong prefix "Bearer " (7 karakter) untuk mendapatkan token JWT murni
        jwt = authorizationHeader.substring(7);
        userId = jwtService.extractId(jwt);

        // Muat data user. Karena UserServiceImpl menggunakan @Cacheable,
        // pada request kedua dan seterusnya ini akan diambil dari Redis, bukan database.
        UserResponse user = userService.findById(Long.parseLong(userId));

        // Buat objek GrantedAuthority dari role user untuk keperluan authorization
        List<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority(user.getRole().toString())
        );

        // Validasi token: pastikan userId di dalam token cocok dengan user yang ditemukan
        // dan token belum kedaluwarsa
        if (jwtService.isTokenValid(jwt, user)){
            // Buat objek Authentication token yang dikenali Spring Security
            // Parameter kedua (credentials) di-set null karena JWT sudah sebagai bukti autentikasi
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userId,
                null,
                authorities);

            // Tambahkan detail request (IP address, session ID, dll.) ke dalam token autentikasi
            authToken.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
            );

            // Simpan token autentikasi ke SecurityContext.
            // Ini yang menandai bahwa request SAAT INI sudah terautentikasi.
            // SecurityConfig akan membaca ini untuk menentukan akses ke endpoint.
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        // Teruskan request ke filter/controller berikutnya dalam chain
        filterChain.doFilter(request, response);
    }
}
