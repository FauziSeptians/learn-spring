package com.absensi.absensi_app.services.impl;

import com.absensi.absensi_app.dto.response.UserResponse;
import com.absensi.absensi_app.entity.User;
import com.absensi.absensi_app.services.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Implementasi dari {@link JwtService} yang menangani seluruh operasi JWT
 * (JSON Web Token) dalam aplikasi.
 *
 * <p><b>Apa itu JWT?</b><br>
 * JWT adalah sebuah token berbentuk string dengan format {@code Header.Payload.Signature}.
 * Token ini bersifat <i>self-contained</i>: server tidak perlu menyimpan sesi di database,
 * karena semua informasi yang dibutuhkan (user ID, waktu kadaluarsa) sudah ada di dalam token itu sendiri,
 * dan keasliannya bisa diverifikasi menggunakan digital signature.</p>
 *
 * <p><b>Struktur Token JWT yang dihasilkan aplikasi ini:</b>
 * <pre>
 *   Header  : { "alg": "HS256" }
 *   Payload : { "sub": "userId", "iat": timestamp, "exp": timestamp }
 *   Signature: HMACSHA256(base64(header) + "." + base64(payload), secretKey)
 * </pre>
 *
 * <p><b>Annotation yang digunakan:</b>
 * <ul>
 *   <li>{@code @Slf4j} - Membuat logger otomatis via Lombok.</li>
 *   <li>{@code @Service} - Mendaftarkan class sebagai Spring Bean di layer bisnis.</li>
 *   <li>{@code @RequiredArgsConstructor} - Constructor Injection via Lombok.</li>
 *   <li>{@code @Value("${...}")} - Menginject nilai dari {@code application.properties}
 *       langsung ke dalam field. Ini cara yang lebih baik dari hardcoding nilai konfigurasi
 *       langsung di dalam kode, karena nilainya bisa diubah tanpa perlu recompile kode.</li>
 * </ul>
 *
 * @author  Fauzi Septian
 * @version 1.0
 * @see     JwtService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    /**
     * Secret key untuk signing JWT, diinjeksikan dari properti
     * {@code application.security.jwt.secret-key}.
     * Harus berupa string Hex minimal 64 karakter (256-bit) untuk algoritma HS256.
     *
     * <p>{@code @Value} akan otomatis mengambil nilai dari {@code application.properties}
     * dan menyuntikkannya ke field ini saat Spring melakukan inisialisasi Bean.</p>
     */
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    /**
     * Durasi validitas token JWT dalam milidetik, diinjeksikan dari properti
     * {@code application.security.jwt.expiration}.
     * Default: 86400000ms = 24 jam.
     */
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    /**
     * Durasi validitas refresh token dalam milidetik, diinjeksikan dari properti
     * {@code application.security.jwt.refresh-token.expiration}.
     * Default: 604800000ms = 7 hari.
     */
    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    /**
     * Mengekstrak User ID ({@code subject}) dari token JWT.
     *
     * <p>Dalam token JWT, klaim {@code sub} (subject) digunakan untuk menyimpan
     * identitas utama dari pemilik token. Pada aplikasi ini, nilainya adalah ID user.</p>
     *
     * @param token string JWT yang akan diekstrak.
     * @return ID user dalam bentuk String.
     */
    public String extractId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Mengekstrak klaim (claim) tertentu dari token JWT menggunakan fungsi resolver.
     *
     * <p>Method ini menggunakan pendekatan <b>Generic + Functional Interface</b> dengan
     * {@link Function}{@code <Claims, T>} sebagai parameter. Ini memungkinkan pemanggil
     * mengekstrak klaim apapun (subject, expiration, custom claim) hanya dengan satu method,
     * tanpa perlu membuat method terpisah untuk setiap jenis klaim.</p>
     *
     * @param <T>            tipe data dari klaim yang akan diekstrak.
     * @param token          string JWT yang akan diproses.
     * @param claimsResolver fungsi lambda/method reference untuk mengekstrak klaim spesifik.
     * @return nilai klaim yang diekstrak dengan tipe {@code T}.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Menghasilkan token JWT dengan custom claims tambahan.
     *
     * @param extractClaims Map berisi klaim-klaim tambahan yang akan disertakan dalam payload token.
     * @param user          entity user yang menjadi subject token.
     * @return string JWT yang telah ditandatangani dan siap digunakan.
     */
    public String generateToken(Map<String, Object> extractClaims, User user) {
        return Jwts.builder()
            .setClaims(extractClaims)
            .setSubject(user.getId().toString())       // Menyimpan ID user sebagai subject
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
            .signWith(getSignInKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    /**
     * Menghasilkan token JWT standar tanpa custom claims.
     *
     * @param userDetails entity user yang menjadi subject token.
     * @return string JWT yang telah ditandatangani.
     */
    public String generateToken(User userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Memvalidasi apakah sebuah token JWT masih valid untuk user tertentu.
     *
     * <p>Validasi dilakukan dengan dua pemeriksaan:
     * <ol>
     *   <li>User ID di dalam token cocok dengan ID dari {@code userDetails}.</li>
     *   <li>Token belum melewati waktu kadaluarsa ({@code exp} claim).</li>
     * </ol>
     *
     * @param token       string JWT yang akan divalidasi.
     * @param userDetails data user dari database/cache yang akan dicocokkan dengan token.
     * @return {@code true} jika token valid, {@code false} jika tidak.
     */
    public boolean isTokenValid(String token, UserResponse userDetails) {
        final String userId = extractId(token);
        return (userId.equals(userDetails.getId().toString())) && !isTokenExpired(token);
    }

    /**
     * Memeriksa apakah token JWT sudah kedaluwarsa.
     *
     * @param token string JWT yang diperiksa.
     * @return {@code true} jika waktu kadaluarsa token sudah lewat.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Mengekstrak waktu kadaluarsa dari token JWT.
     *
     * @param token string JWT yang diperiksa.
     * @return {@link Date} yang merepresentasikan waktu kadaluarsa token.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Mem-parse dan mengekstrak seluruh klaim (payload) dari token JWT.
     *
     * <p>Method ini menggunakan secret key untuk memverifikasi signature token.
     * Jika token telah dimanipulasi atau signature tidak cocok, library JJWT akan
     * melempar {@code SignatureException}. Ini adalah mekanisme utama yang mencegah
     * pemalsuan token.</p>
     *
     * @param token string JWT yang akan di-parse.
     * @return objek {@link Claims} berisi semua klaim dari payload token.
     * @throws io.jsonwebtoken.JwtException jika token tidak valid atau signature tidak cocok.
     */
    private Claims extractAllClaims(String token) {
        return Jwts
            .parserBuilder()
            .setSigningKey(getSignInKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    /**
     * Mengkonversi secret key string (Hex) menjadi objek {@link Key} kriptografi
     * yang siap digunakan untuk signing/verifying JWT.
     *
     * <p>Secret key di {@code application.properties} disimpan dalam format Hex string.
     * Method ini men-decode Hex tersebut menjadi byte array, lalu menghasilkan
     * HMAC-SHA key menggunakan library JJWT.</p>
     *
     * @return objek {@link Key} untuk algoritma HMAC-SHA256.
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
