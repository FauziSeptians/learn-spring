package com.absensi.absensi_app.controller;

import com.absensi.absensi_app.dto.request.CheckInRequest;
import com.absensi.absensi_app.dto.response.AbsensiResponse;
import com.absensi.absensi_app.dto.response.ApiResponse;
import com.absensi.absensi_app.dto.response.PageResponse;
import com.absensi.absensi_app.services.AbsensiService;
import com.absensi.absensi_app.util.AbsensiMapper;
import com.absensi.absensi_app.util.ApiMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller yang menangani seluruh endpoint operasi absensi karyawan.
 *
 * <p>Menyediakan tiga operasi utama: clock-in (masuk kerja), clock-out (pulang kerja),
 * dan pengambilan riwayat absensi. Semua endpoint memerlukan autentikasi JWT yang valid.</p>
 *
 * <p><b>Annotation yang digunakan:</b>
 * <ul>
 *   <li>{@code @RestController} - Menandai class sebagai REST Controller dengan serialisasi
 *       JSON otomatis pada setiap return value.</li>
 *   <li>{@code @RequestMapping("/api/absensi")} - Base URL untuk semua endpoint di class ini.</li>
 *   <li>{@code @RequiredArgsConstructor} - Constructor Injection via Lombok.</li>
 *   <li>{@code @Tag} - Pengelompokan endpoint di Swagger UI.</li>
 *   <li>{@code @SecurityRequirement} - Menampilkan ikon kunci di Swagger UI dan mengaktifkan
 *       input token JWT untuk mencoba endpoint langsung dari browser.</li>
 * </ul>
 *
 * @author  Fauzi Septian
 * @version 1.0
 * @see     AbsensiService
 */
@RestController
@RequestMapping("/api/absensi")
@RequiredArgsConstructor
@Tag(name = "Absensi", description = "API untuk manajemen absensi karyawan (butuh autentikasi)")
@SecurityRequirement(name = "Bearer Authentication")
public class AbsensiController {

    /** Service bisnis absensi yang mengandung seluruh logika bisnis clock-in dan clock-out. */
    private final AbsensiService absensiService;

    /** Mapper untuk transformasi entity Absensi ke DTO Response. */
    private final AbsensiMapper absensiMapper;

    /**
     * Memproses permintaan check-in (masuk kerja) untuk user tertentu.
     *
     * <p>Validasi yang dilakukan oleh service layer:
     * <ul>
     *   <li>User harus ada di database.</li>
     *   <li>User belum absen clock-in hari ini.</li>
     *   <li>Jam saat ini tidak melebihi batas waktu masuk (09:15).</li>
     *   <li>Tipe absensi harus valid ("WFO" atau "WFH").</li>
     * </ul>
     *
     * <p><b>Annotation method:</b>
     * <ul>
     *   <li>{@code @PostMapping("/check-in/{userId}")} - Mengikat ke HTTP POST.</li>
     *   <li>{@code @PathVariable} - Mengekstrak {@code userId} dari path URL.</li>
     *   <li>{@code @Valid} - Memvalidasi {@link CheckInRequest} berdasarkan constraint
     *       Bean Validation (misal: {@code @NotBlank}, {@code @NotNull}) yang ada pada class tersebut.</li>
     *   <li>{@code @RequestBody} - Men-deserialize body JSON menjadi {@link CheckInRequest}.</li>
     * </ul>
     *
     * @param userId  ID dari user yang melakukan check-in.
     * @param request DTO berisi tipe absensi dan keterangan.
     * @return {@link ResponseEntity} dengan status 200 dan pesan sukses tanpa data.
     */
    @PostMapping("/check-in/{userId}")
    @Operation(summary = "Check In", description = "Melakukan clock-in. Batas waktu masuk: 09:15")
    public ResponseEntity<ApiResponse<Void>> checkIn(
            @PathVariable Long userId,
            @Valid @RequestBody CheckInRequest request) {
        absensiService.clockIn(userId, request.getType(), request.getKeterangan());
        return ResponseEntity.ok(ApiMapper.success("Berhasil melakukan check-in"));
    }

    /**
     * Memproses permintaan check-out (pulang kerja) untuk user tertentu.
     *
     * <p>Validasi yang dilakukan oleh service layer:
     * <ul>
     *   <li>User harus sudah clock-in hari ini.</li>
     *   <li>User belum melakukan clock-out sebelumnya.</li>
     *   <li>Durasi kerja minimal 9 jam dari waktu clock-in. Jika belum,
     *       response error akan menyertakan detail waktu yang telah bekerja
     *       dan waktu eligible clock-out.</li>
     * </ul>
     *
     * @param userId ID dari user yang melakukan check-out.
     * @return {@link ResponseEntity} dengan status 200 dan pesan sukses tanpa data.
     */
    @PostMapping("/check-out/{userId}")
    @Operation(summary = "Check Out", description = "Melakukan clock-out. Minimum 9 jam kerja")
    public ResponseEntity<ApiResponse<Void>> checkOut(@PathVariable Long userId) {
        absensiService.clockOut(userId);
        return ResponseEntity.ok(ApiMapper.success("Berhasil melakukan check-out"));
    }

    /**
     * Mengambil riwayat absensi user tertentu dengan dukungan paginasi.
     *
     * <p><b>Annotation method:</b>
     * <ul>
     *   <li>{@code @GetMapping("/history/{userId}")} - Mengikat ke HTTP GET.</li>
     *   <li>{@code @RequestParam(defaultValue = "1")} - Query parameter opsional dengan nilai default.</li>
     * </ul>
     *
     * @param userId ID dari user yang riwayat absensinya diminta.
     * @param page   nomor halaman (default: 1).
     * @param size   jumlah item per halaman (default: 10).
     * @return {@link ResponseEntity} berisi {@link PageResponse} dari {@link AbsensiResponse}.
     */
    @GetMapping("/history/{userId}")
    @Operation(summary = "Get Riwayat Absensi", description = "Mengambil riwayat absensi user tertentu dengan pagination")
    public ResponseEntity<ApiResponse<PageResponse<AbsensiResponse>>> getHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<AbsensiResponse> absensiList = absensiService.getAttendanceByUser(userId, page, size);
        return ResponseEntity.ok(ApiMapper.success("Berhasil mengambil riwayat absensi", absensiList));
    }
}
