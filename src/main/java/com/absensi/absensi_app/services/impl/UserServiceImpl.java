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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

/**
 * Implementasi dari {@link UserService} yang mengelola semua operasi bisnis terkait entitas User.
 *
 * <p>Class ini mengintegrasikan beberapa komponen penting:
 * <ul>
 *   <li><b>Repository Pattern</b>: Komunikasi ke database melalui {@link UserRepository} (Spring Data JPA).</li>
 *   <li><b>Redis Caching</b>: Menggunakan Spring Cache abstraction untuk meng-cache data user
 *       dan mengurangi query berulang ke database.</li>
 *   <li><b>MapStruct Mapping</b>: Transformasi antara entity {@link User} dan DTO {@link UserResponse}
 *       melalui {@link UserMapper}.</li>
 * </ul>
 *
 * <p><b>Annotation pada class:</b>
 * <ul>
 *   <li>{@code @Slf4j} - Membuat logger {@code log} secara otomatis via Lombok untuk structured logging.</li>
 *   <li>{@code @Service} - Turunan dari {@code @Component}, menandai class ini sebagai komponen
 *       layer bisnis (Service Layer). Spring akan mendaftarkannya sebagai Bean dan mengelola
 *       siklus hidupnya. Anotasi ini juga menjadi "marker" semantik yang membedakan Service
 *       dari Repository atau Controller.</li>
 *   <li>{@code @RequiredArgsConstructor} - Membuat constructor dengan semua field {@code final}
 *       sebagai parameternya. Ini mengimplementasikan <b>Constructor Injection</b>, cara
 *       Dependency Injection yang direkomendasikan karena menjamin semua dependency tersedia
 *       sebelum object digunakan dan membuat class mudah diuji (testable).</li>
 * </ul>
 *
 * @author  Fauzi Septian
 * @version 1.0
 * @see     UserService
 * @see     UserRepository
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    /**
     * Repository untuk akses data User ke database.
     * Diinjeksikan oleh Spring melalui constructor (Constructor Injection).
     * Field {@code final} menjamin dependency ini tidak bisa diubah setelah konstruksi.
     */
    private final UserRepository userRepository;

    /**
     * Mapper untuk konversi antara entity {@link User} ke DTO {@link UserResponse}.
     * Diimplementasikan secara otomatis oleh MapStruct pada saat kompilasi.
     */
    private final UserMapper userMapper;

    /**
     * Mencari data user berdasarkan ID dengan dukungan Redis Cache.
     *
     * <p><b>Anotasi {@code @Cacheable(value = "users", key = "#id")}:</b><br>
     * Ini adalah inti dari mekanisme caching. Cara kerjanya:
     * <ol>
     *   <li>Sebelum method dieksekusi, Spring memeriksa cache Redis dengan key {@code "users::1"}
     *       (format: {@code "namaCache::nilaiKey"}).</li>
     *   <li><b>Cache HIT</b>: Jika data ditemukan, Spring langsung mengembalikan data dari cache
     *       dan <b>melewati seluruh isi method</b> (tidak ada query ke database!).</li>
     *   <li><b>Cache MISS</b>: Jika data tidak ditemukan, method dieksekusi normal (query database),
     *       lalu hasilnya disimpan ke Redis sebelum dikembalikan ke pemanggil.</li>
     * </ol>
     * Efek sampingnya: log {@code FIND_BY_ID_START} tidak akan muncul di terminal pada cache HIT,
     * karena isi method tidak pernah dieksekusi.
     *
     * @param id ID unik dari user yang ingin dicari.
     * @return {@link UserResponse} DTO berisi data user. Dikembalikan dari Redis jika cache HIT.
     * @throws ApiException dengan status 404 jika user tidak ditemukan di database (saat cache MISS).
     */
    @Cacheable(value = "users", key = "#id")
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

    /**
     * Mengambil semua data user dengan dukungan paginasi.
     *
     * <p>Paginasi diimplementasikan menggunakan {@link PageRequest} dari Spring Data JPA.
     * Endpoint ini tidak menggunakan cache karena data list lebih dinamis dan lebih sulit
     * untuk diinvalidasi dengan tepat.</p>
     *
     * @param page nomor halaman yang diminta, 1-based (halaman pertama = 1).
     * @param size jumlah data per halaman.
     * @return {@link PageResponse} yang berisi data user dan informasi paginasi (total halaman, dll.).
     */
    @Override
    public PageResponse<UserResponse> findAll(int page, int size) {
        log.info("FIND_ALL_START | page : [{}], size :[{}]", page, size);

        // Spring Data JPA menggunakan 0-based page, sehingga perlu dikurangi 1
        Pageable pageable = PageRequest.of(page - 1, size);

        log.debug("FIND_ALL | pageable : [{}]", pageable);

        Page<User> users = userRepository.findAll(pageable);
        Page<UserResponse> userResponses = users.map(userMapper::toResponse);

        log.info("FIND_ALL_SUCCESS | user : [{}]", users);

        return PaginationMapper.of(userResponses);
    }

    /**
     * Menghapus data user berdasarkan ID dan menginvalidasi cache Redis yang bersangkutan.
     *
     * <p><b>Anotasi {@code @CacheEvict(value = "users", key = "#id")}:</b><br>
     * Setelah data user berhasil dihapus dari database, anotasi ini memerintahkan Spring untuk
     * menghapus entri cache dengan key {@code "users::{id}"} dari Redis. Tanpa ini, cache
     * akan tetap menyimpan data user yang sudah dihapus, dan pemanggilan {@code findById}
     * berikutnya akan mengembalikan data "hantu" yang sudah tidak ada di database.</p>
     *
     * <p><b>Anotasi {@code @Transactional}:</b><br>
     * Memastikan operasi penghapusan dieksekusi dalam sebuah transaksi database.
     * Jika terjadi error di tengah proses, seluruh perubahan akan di-rollback,
     * menjaga konsistensi data.</p>
     *
     * @param id ID dari user yang akan dihapus.
     * @throws ApiException dengan status 404 jika user tidak ditemukan.
     */
    @CacheEvict(value = "users", key = "#id")
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

    /**
     * Memperbarui data user berdasarkan ID dan menyegarkan cache Redis secara bersamaan.
     *
     * <p><b>Anotasi {@code @CachePut(value = "users", key = "#id")}:</b><br>
     * Berbeda dengan {@code @CacheEvict} yang hanya menghapus cache, {@code @CachePut}
     * menjalankan method DAN menyimpan hasilnya ke dalam cache. Keuntungannya:
     * setelah update berhasil, cache langsung berisi data terbaru tanpa perlu menunggu
     * request berikutnya untuk mengisi kembali cache (cold start). Ini mencegah
     * lonjakan query ke database jika ada banyak request {@code findById} setelah update.</p>
     *
     * <p><b>Penting</b>: {@code @CachePut} memastikan method <b>selalu dieksekusi</b> (tidak
     * seperti {@code @Cacheable}), sehingga data di database selalu menjadi source of truth.</p>
     *
     * @param id      ID dari user yang akan diperbarui.
     * @param request DTO request yang berisi nama dan email baru.
     * @return {@link UserResponse} berisi data user yang sudah diperbarui, sekaligus tersimpan ke Redis.
     * @throws ApiException dengan status 404 jika user tidak ditemukan.
     */
    @CachePut(value = "users", key = "#id")
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
