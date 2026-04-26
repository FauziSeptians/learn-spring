package com.absensi.absensi_app.strategy.impl;

import com.absensi.absensi_app.entity.Absensi;
import com.absensi.absensi_app.entity.User;
import com.absensi.absensi_app.enums.AbsensiStatus;
import com.absensi.absensi_app.strategy.CheckInStrategy;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Implementasi {@link CheckInStrategy} untuk tipe absensi <b>WFO (Work From Office)</b>.
 *
 * <p>Class ini menangani logika check-in ketika karyawan bekerja dari kantor.
 * Status absensi yang dihasilkan adalah {@link AbsensiStatus#HADIR}
 * dengan keterangan yang diawali prefix "WFO:".</p>
 *
 * <p><b>Annotation yang digunakan:</b>
 * <ul>
 *   <li>{@code @Component} - Mendaftarkan class ini sebagai Spring Bean. Dengan anotasi ini,
 *       Spring akan secara otomatis menemukan class ini saat component scanning dan
 *       memasukkannya ke dalam {@code List<CheckInStrategy>} yang diinjeksikan ke
 *       {@code AbsensiServiceImpl}. Inilah mekanisme Collection Injection yang membuat
 *       Strategy Pattern berjalan secara elegan di Spring.</li>
 * </ul>
 *
 * @author  Fauzi Septian
 * @version 1.0
 * @see     CheckInStrategy
 */
@Component
public class WfoCheckInStrategy implements CheckInStrategy {

    /**
     * Membuat entity {@link Absensi} untuk skenario Work From Office.
     *
     * <p>Menggunakan Builder pattern dari Lombok ({@code @Builder} pada class {@link Absensi})
     * untuk membuat objek secara deklaratif dan mudah dibaca.</p>
     *
     * @param user       entity user yang melakukan check-in.
     * @param keterangan keterangan dari karyawan, akan diawali dengan prefix "WFO:".
     * @return entity {@link Absensi} dengan status {@link AbsensiStatus#HADIR} dan label WFO.
     */
    @Override
    public Absensi checkIn(User user, String keterangan) {
        return Absensi.builder()
                .user(user)
                .checkIn(LocalDateTime.now())
                .status(AbsensiStatus.HADIR)
                .tanggal(LocalDate.now())
                .keterangan("WFO: " + keterangan)
                .build();
    }

    /**
     * Memeriksa apakah strategy ini mendukung tipe "WFO" (case-insensitive).
     *
     * <p>Penggunaan {@code equalsIgnoreCase} memastikan input "wfo", "WFO", atau "Wfo"
     * semuanya diterima tanpa perlu validasi tambahan di tempat lain.</p>
     *
     * @param type tipe absensi dari request.
     * @return {@code true} jika {@code type} adalah "WFO" (tidak peka huruf besar/kecil).
     */
    @Override
    public boolean supports(String type) {
        return "WFO".equalsIgnoreCase(type);
    }
}
