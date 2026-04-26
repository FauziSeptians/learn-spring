package com.absensi.absensi_app.controller;

import com.absensi.absensi_app.dto.request.RegisterRequest;
import com.absensi.absensi_app.dto.response.ApiResponse;
import com.absensi.absensi_app.dto.response.PageResponse;
import com.absensi.absensi_app.dto.response.UserResponse;
import com.absensi.absensi_app.services.impl.UserServiceImpl;
import com.absensi.absensi_app.util.ApiMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller yang menangani operasi CRUD (Create, Read, Update, Delete) untuk entitas User.
 *
 * <p>Semua endpoint dalam class ini memerlukan autentikasi JWT yang valid karena dikonfigurasi
 * sebagai {@code .authenticated()} di {@code SecurityConfig}. Token JWT harus disertakan
 * pada header {@code Authorization: Bearer <token>}.</p>
 *
 * <p><b>Annotation yang digunakan:</b>
 * <ul>
 *   <li>{@code @RestController} - Menandai class sebagai REST Controller; semua return value
 *       di-serialize otomatis menjadi JSON.</li>
 *   <li>{@code @RequestMapping("/api/users")} - Base URL untuk semua endpoint di class ini.</li>
 *   <li>{@code @RequiredArgsConstructor} - Constructor Injection untuk {@link UserServiceImpl}.</li>
 *   <li>{@code @Tag} - Mengelompokkan endpoint ini di bawah grup "User" di Swagger UI.</li>
 *   <li>{@code @SecurityRequirement(name = "Bearer Authentication")} - Memberi tahu Swagger UI
 *       bahwa endpoint ini membutuhkan token Bearer. Dengan anotasi ini, Swagger UI akan
 *       menampilkan tombol kunci (🔒) dan memungkinkan pengguna mengisi token JWT
 *       untuk mencoba endpoint langsung dari browser.</li>
 * </ul>
 *
 * @author  Fauzi Septian
 * @version 1.0
 * @see     UserServiceImpl
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "API untuk manajemen data user (butuh autentikasi)")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    /**
     * Service untuk operasi bisnis user.
     * Menggunakan implementasi konkret ({@link UserServiceImpl}) untuk mengakses cache annotations.
     */
    private final UserServiceImpl userService;

    /**
     * Mengambil semua data user dengan pagination.
     *
     * <p><b>Annotation method:</b>
     * <ul>
     *   <li>{@code @GetMapping} - Mengikat ke HTTP GET pada {@code /api/users}.</li>
     *   <li>{@code @RequestParam(defaultValue = "1")} - Mengambil query parameter {@code ?page=}
     *       dari URL. Jika tidak ada, nilai defaultnya adalah 1.</li>
     * </ul>
     *
     * @param page nomor halaman (default: 1).
     * @param size jumlah item per halaman (default: 10).
     * @return {@link ResponseEntity} berisi {@link PageResponse} dengan data user dan metadata paginasi.
     */
    @GetMapping
    @Operation(summary = "Get semua user", description = "Mengambil semua data user dengan pagination")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> findAll(
        @RequestParam(defaultValue = "1")  int page,
        @RequestParam(defaultValue = "10") int size) {

        PageResponse<UserResponse> users = userService.findAll(page, size);
        return ResponseEntity.ok(ApiMapper.success("Sukses mengambil semua data user", users));
    }

    /**
     * Mengambil data user berdasarkan ID. Endpoint ini memanfaatkan Redis Cache.
     *
     * <p>Request kedua dan seterusnya untuk user ID yang sama akan diambil dari Redis
     * (bukan database), sehingga jauh lebih cepat. Periksa konsol untuk memverifikasi
     * bahwa log SQL Hibernate tidak muncul pada request kedua.</p>
     *
     * <p><b>Annotation method:</b>
     * <ul>
     *   <li>{@code @GetMapping("/{id}")} - Mengikat ke HTTP GET pada {@code /api/users/{id}}.</li>
     *   <li>{@code @PathVariable} - Mengekstrak nilai {@code id} dari path URL secara otomatis.</li>
     * </ul>
     *
     * @param id ID dari user yang ingin diambil.
     * @return {@link ResponseEntity} berisi {@link UserResponse}.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Mengambil data user berdasarkan ID (dengan Redis Cache)")
    public ResponseEntity<ApiResponse<UserResponse>> findById(@PathVariable Long id) {
        UserResponse user = userService.findById(id);
        return ResponseEntity.ok(ApiMapper.success("Sukses mengambil data", user));
    }

    /**
     * Memperbarui data user berdasarkan ID dan menyegarkan cache secara bersamaan.
     *
     * <p>Menggunakan {@code @CachePut} di service layer, sehingga setelah update berhasil,
     * data terbaru langsung tersimpan ke Redis tanpa cold-start pada request berikutnya.</p>
     *
     * <p><b>Annotation method:</b>
     * <ul>
     *   <li>{@code @PutMapping("/{id}")} - Mengikat ke HTTP PUT pada {@code /api/users/{id}}.</li>
     *   <li>{@code @RequestBody} - Men-deserialize body JSON dari request menjadi {@link RegisterRequest}.</li>
     * </ul>
     *
     * @param id      ID dari user yang akan diperbarui.
     * @param request DTO berisi data user yang baru.
     * @return {@link ResponseEntity} berisi {@link UserResponse} dengan data yang sudah diperbarui.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update data user berdasarkan ID")
    public ResponseEntity<ApiResponse<UserResponse>> update(
        @PathVariable Long id,
        @RequestBody RegisterRequest request) {

        UserResponse user = userService.update(id, request);
        return ResponseEntity.ok(ApiMapper.success("Sukses melakukan perubahan data", user));
    }

    /**
     * Menghapus user berdasarkan ID dan sekaligus menghapus cache Redis yang terkait.
     *
     * <p>Menggunakan {@code @CacheEvict} di service layer, memastikan tidak ada data
     * "hantu" yang tersisa di Redis setelah user dihapus dari database.</p>
     *
     * <p><b>Annotation method:</b>
     * <ul>
     *   <li>{@code @DeleteMapping("/{id}")} - Mengikat ke HTTP DELETE pada {@code /api/users/{id}}.</li>
     * </ul>
     *
     * @param id ID dari user yang akan dihapus.
     * @return {@link ResponseEntity} dengan status 200 dan pesan sukses tanpa body data.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Hapus user berdasarkan ID")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok(ApiMapper.success("Sukses menghapus data"));
    }
}
