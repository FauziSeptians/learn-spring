package com.absensi.absensi_app.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Custom exception yang merepresentasikan error bisnis atau validasi di dalam aplikasi ini.
 *
 * <p>Class ini extend {@link RuntimeException} agar tidak perlu di-declare secara eksplisit
 * di signature method (unchecked exception), sehingga kode lebih bersih dan boilerplate berkurang.</p>
 *
 * <p>{@code ApiException} didesain untuk membawa informasi yang cukup bagi
 * {@link GlobalExceptionHandler} untuk membangun HTTP response yang tepat, yaitu:
 * <ul>
 *   <li>Pesan error (dari superclass {@link RuntimeException}).</li>
 *   <li>HTTP status yang sesuai (misal: 404 Not Found, 400 Bad Request).</li>
 *   <li>Data error opsional untuk memberikan konteks tambahan kepada klien API.</li>
 * </ul>
 *
 * <p><b>Cara penggunaan di Service layer:</b>
 * <pre>{@code
 * // Melempar exception tanpa data tambahan
 * throw new ApiException("User tidak ditemukan!", HttpStatus.NOT_FOUND);
 *
 * // Melempar exception dengan data detail (misal: info sisa jam kerja)
 * CheckoutErrorDataResponse errorData = ...;
 * throw new ApiException("Belum memenuhi jam kerja", HttpStatus.BAD_REQUEST, errorData);
 * }</pre>
 *
 * <p><b>Annotation yang digunakan:</b>
 * <ul>
 *   <li>{@code @Getter} - Anotasi Lombok yang otomatis menghasilkan getter untuk semua field
 *       ({@code getStatus()} dan {@code getErrorData()}). Tanpa ini, {@link GlobalExceptionHandler}
 *       tidak bisa mengakses nilai field tersebut.</li>
 * </ul>
 *
 * @author  Fauzi Septian
 * @version 1.0
 * @see     GlobalExceptionHandler
 */
@Getter
public class ApiException extends RuntimeException {

    /**
     * HTTP status yang harus dikembalikan ke klien ketika exception ini ditangkap.
     * Bersifat {@code final} agar nilainya tidak bisa diubah setelah object dibuat (immutable).
     */
    private final HttpStatus status;

    /**
     * Data tambahan yang opsional untuk memberikan konteks error yang lebih detail kepada klien.
     * Contoh: objek yang berisi detail jam check-in, jam eligible check-out, dll.
     * Bisa bernilai {@code null} jika tidak ada data tambahan.
     */
    private final Object errorData;

    /**
     * Constructor untuk exception tanpa data error tambahan.
     *
     * @param message pesan error yang akan ditampilkan kepada klien.
     * @param status  HTTP status code yang sesuai dengan jenis error.
     */
    public ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.errorData = null;
    }

    /**
     * Constructor untuk exception dengan data error tambahan.
     *
     * @param message   pesan error yang akan ditampilkan kepada klien.
     * @param status    HTTP status code yang sesuai dengan jenis error.
     * @param errorData objek data tambahan yang memberikan konteks lebih detail tentang error.
     */
    public ApiException(String message, HttpStatus status, Object errorData) {
        super(message);
        this.status = status;
        this.errorData = errorData;
    }
}
