package com.absensi.absensi_app.strategy;

import com.absensi.absensi_app.entity.Absensi;
import com.absensi.absensi_app.entity.User;

/**
 * Interface yang mendefinisikan kontrak untuk <b>Strategy Pattern</b> dalam proses check-in karyawan.
 *
 * <p><b>Apa itu Strategy Pattern?</b><br>
 * Strategy Pattern adalah design pattern behavioral yang memungkinkan kita mendefinisikan
 * sekumpulan algoritma (dalam hal ini: logika check-in WFO dan WFH), mengenkapsulasi
 * masing-masing ke dalam class terpisah, dan membuatnya dapat dipertukarkan satu sama lain
 * tanpa mengubah kode yang menggunakannya.</p>
 *
 * <p><b>Mengapa menggunakan Strategy Pattern di sini?</b><br>
 * Tanpa Strategy Pattern, {@code AbsensiServiceImpl} akan memiliki blok {@code if-else}
 * atau {@code switch} yang panjang:
 * <pre>{@code
 * if (type.equals("WFO")) { // logika WFO... }
 * else if (type.equals("WFH")) { // logika WFH... }
 * else if (type.equals("LEMBUR")) { // logika LEMBUR... } // susah di-extend!
 * }</pre>
 * Dengan Strategy Pattern, menambah tipe absensi baru (misal: LEMBUR) hanya perlu
 * membuat class baru yang implements interface ini, tanpa menyentuh kode yang sudah ada
 * (<b>Open/Closed Principle</b>).</p>
 *
 * <p><b>Cara Spring mengintegrasikan Strategy Pattern:</b><br>
 * Semua class yang mengimplementasikan interface ini dan dianotasi {@code @Component}
 * akan secara otomatis dikumpulkan oleh Spring ke dalam sebuah {@code List<CheckInStrategy>}
 * dan diinjeksikan ke {@code AbsensiServiceImpl}. Ini disebut <b>Collection Injection</b>.</p>
 *
 * @author  Fauzi Septian
 * @version 1.0
 * @see     com.absensi.absensi_app.strategy.impl.WfoCheckInStrategy
 * @see     com.absensi.absensi_app.strategy.impl.WfhCheckInStrategy
 * @see     com.absensi.absensi_app.services.impl.AbsensiServiceImpl
 */
public interface CheckInStrategy {

    /**
     * Mengeksekusi logika check-in dan menghasilkan entity {@link Absensi} yang siap disimpan.
     *
     * <p>Setiap implementasi mendefinisikan cara pembuatan entitas Absensi yang spesifik
     * sesuai dengan tipenya (misal: WFH menambahkan prefix "WFH:" pada keterangan).</p>
     *
     * @param user       entity {@link User} yang melakukan check-in.
     * @param keterangan keterangan opsional dari karyawan.
     * @return entity {@link Absensi} yang sudah terisi data dan siap untuk di-persist ke database.
     */
    Absensi checkIn(User user, String keterangan);

    /**
     * Memeriksa apakah implementasi strategy ini mendukung tipe absensi yang diberikan.
     *
     * <p>Method ini digunakan oleh {@code AbsensiServiceImpl} untuk memilih implementasi
     * yang tepat menggunakan Stream API:
     * <pre>{@code
     * strategies.stream()
     *     .filter(s -> s.supports(type))
     *     .findFirst()
     * }</pre>
     *
     * @param type tipe absensi dari request, contoh: "WFO", "WFH".
     * @return {@code true} jika implementasi ini menangani tipe tersebut, {@code false} jika tidak.
     */
    boolean supports(String type);
}
