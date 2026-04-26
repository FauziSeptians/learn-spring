package com.absensi.absensi_app.config;

import com.absensi.absensi_app.dto.response.ApiResponse;
import com.absensi.absensi_app.util.ApiMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Konfigurasi keamanan utama aplikasi menggunakan Spring Security.
 *
 * <p>Class ini mendefinisikan seluruh aturan keamanan (security rules) untuk aplikasi,
 * termasuk endpoint mana yang publik, endpoint mana yang butuh autentikasi, mekanisme
 * sesi, dan integrasi filter JWT.</p>
 *
 * <p><b>Annotation yang digunakan:</b>
 * <ul>
 *   <li>{@code @Configuration} - Menandai class ini sebagai sumber definisi {@code @Bean}
 *       untuk Spring IoC Container. Spring akan memproses class ini saat startup untuk
 *       mendaftarkan semua bean yang didefinisikan di dalamnya.</li>
 *   <li>{@code @EnableWebSecurity} - Mengaktifkan dukungan Spring Security Web dan
 *       mengintegrasikannya dengan Spring MVC. Tanpa anotasi ini, konfigurasi di class ini
 *       tidak akan diterapkan ke filter chain HTTP.</li>
 *   <li>{@code @RequiredArgsConstructor} - Anotasi Lombok yang secara otomatis membuat
 *       constructor dengan parameter untuk semua field yang dideklarasikan sebagai {@code final}.
 *       Ini adalah mekanisme <b>Constructor-based Dependency Injection</b> yang direkomendasikan
 *       karena membuat dependency immutable dan mudah diuji (testable).</li>
 * </ul>
 *
 * @author  Fauzi Septian
 * @version 1.0
 * @see     JwtAuthenticationFilter
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * Filter JWT yang akan diinjeksikan oleh Spring secara otomatis (Constructor Injection via Lombok).
     * Filter ini bertanggung jawab untuk memvalidasi token JWT pada setiap request yang masuk.
     */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * ObjectMapper yang diinjeksikan untuk serialisasi response error ke format JSON
     * pada handler akses ditolak (Access Denied Handler).
     */
    private final ObjectMapper objectMapper;

    /**
     * Mendefinisikan rantai filter keamanan (Security Filter Chain) yang mengatur
     * bagaimana setiap HTTP request diproses oleh Spring Security.
     *
     * <p><b>Konfigurasi yang diterapkan:</b>
     * <ol>
     *   <li><b>CSRF Disabled</b>: CSRF (Cross-Site Request Forgery) protection dinonaktifkan karena
     *       aplikasi ini adalah REST API stateless yang menggunakan JWT, bukan cookie-based session.
     *       CSRF umumnya diperlukan untuk aplikasi yang menggunakan browser session.</li>
     *   <li><b>Frame Options Disabled</b>: Diperlukan agar H2 Console (yang menggunakan iframe)
     *       dapat diakses di browser selama development.</li>
     *   <li><b>Session Stateless</b>: {@code SessionCreationPolicy.STATELESS} memastikan Spring Security
     *       tidak pernah membuat atau menggunakan HTTP Session. Setiap request harus membawa
     *       identitasnya sendiri melalui JWT.</li>
     *   <li><b>Access Denied Handler</b>: Menangani error 403 (Forbidden) dan mengembalikannya
     *       dalam format JSON yang konsisten dengan standar API response aplikasi ini.</li>
     *   <li><b>Authorization Rules</b>: Menentukan endpoint publik (tidak perlu token) dan
     *       endpoint yang membutuhkan autentikasi.</li>
     *   <li><b>JWT Filter</b>: Mendaftarkan {@link JwtAuthenticationFilter} untuk berjalan
     *       SEBELUM filter {@code UsernamePasswordAuthenticationFilter} bawaan Spring Security.</li>
     * </ol>
     *
     * @param http objek {@link HttpSecurity} yang disediakan Spring untuk membangun konfigurasi.
     * @return instance {@link SecurityFilterChain} yang siap digunakan.
     * @throws Exception jika terjadi error saat membangun konfigurasi.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)

            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
            )

            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .exceptionHandling(exception -> exception.accessDeniedHandler((request, response, accessDeniedException) -> {
                response.setStatus(403);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");

                ApiResponse<Object> apiResponse = ApiMapper.error(403, "Akses ditolak: Anda tidak memiliki izin");
                response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
            }))

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/v3/api-docs.yaml"
                ).permitAll()
                .requestMatchers("/docs/**").permitAll() // JavaDoc served at /docs/apidocs/index.html
                .requestMatchers("/api/users/**").authenticated()
                .requestMatchers("/api/absensi/**").authenticated()
                .anyRequest().authenticated()
            )

            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    /**
     * Mendaftarkan {@link AuthenticationManager} sebagai Spring Bean.
     *
     * <p>{@code AuthenticationManager} adalah komponen inti Spring Security yang bertanggung jawab
     * memproses request autentikasi (verifikasi username/password). Ia didapatkan dari
     * {@link AuthenticationConfiguration} yang sudah dikonfigurasi secara otomatis oleh Spring Boot.
     * Bean ini dibutuhkan oleh {@code AuthServiceImpl} untuk melakukan autentikasi pada proses login.</p>
     *
     * @param config konfigurasi autentikasi yang diinjeksikan oleh Spring.
     * @return instance {@link AuthenticationManager}.
     * @throws Exception jika terjadi error saat mendapatkan AuthenticationManager.
     */
    @Bean
    public AuthenticationManager authenticationManager(
        AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Mendaftarkan {@link PasswordEncoder} sebagai Spring Bean menggunakan implementasi BCrypt.
     *
     * <p><b>Mengapa BCrypt?</b><br>
     * BCrypt adalah algoritma hashing password yang secara desain lambat dan mahal secara komputasi.
     * Ini dilakukan secara sengaja untuk menahan serangan <i>brute-force</i>. BCrypt juga secara
     * otomatis menyertakan nilai <i>salt</i> acak di dalam hash, sehingga dua password yang sama
     * akan menghasilkan hash yang berbeda setiap kali di-encode. Ini mencegah serangan menggunakan
     * <i>rainbow table</i>.</p>
     *
     * <p>Bean ini diinjeksikan ke {@code AuthServiceImpl} untuk digunakan saat
     * register (encoding) dan login (matching).</p>
     *
     * @return instance {@link BCryptPasswordEncoder}.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
