package com.absensi.absensi_app.strategy.impl;

import com.absensi.absensi_app.entity.Absensi;
import com.absensi.absensi_app.entity.User;
import com.absensi.absensi_app.enums.AbsensiStatus;
import com.absensi.absensi_app.strategy.CheckInStrategy;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Implementasi {@link CheckInStrategy} untuk tipe absensi <b>WFH (Work From Home)</b>.
 *
 * <p>Class ini menangani logika check-in ketika karyawan bekerja dari rumah.
 * Secara bisnis, status absensi WFH tetap dianggap {@link AbsensiStatus#HADIR}
 * namun dibedakan melalui prefix "WFH:" pada field keterangan.</p>
 *
 * <p>Dengan adanya prefix ini, laporan absensi dapat membedakan karyawan yang hadir
 * di kantor (WFO) dengan yang bekerja dari rumah (WFH) tanpa perlu menambahkan
 * kolom baru di database.</p>
 *
 * <p><b>Annotation yang digunakan:</b>
 * <ul>
 *   <li>{@code @Component} - Mendaftarkan class ini sebagai Spring Bean agar dapat
 *       ditemukan saat component scanning dan dimasukkan ke dalam Collection Injection
 *       di {@code AbsensiServiceImpl}.</li>
 * </ul>
 *
 * @author  Fauzi Septian
 * @version 1.0
 * @see     CheckInStrategy
 * @see     WfoCheckInStrategy
 */
@Component
public class WfhCheckInStrategy implements CheckInStrategy {

    /**
     * Membuat entity {@link Absensi} untuk skenario Work From Home.
     *
     * <p>Logika bisnis WFH saat ini sama dengan WFO (status HADIR),
     * namun dipisah menjadi class tersendiri untuk memudahkan penambahan
     * aturan khusus WFH di masa mendatang (misal: perlu upload bukti foto,
     * atau jam check-in lebih fleksibel) tanpa memengaruhi logika WFO.</p>
     *
     * @param user       entity user yang melakukan check-in WFH.
     * @param keterangan keterangan dari karyawan, akan diawali prefix "WFH:".
     * @return entity {@link Absensi} dengan status {@link AbsensiStatus#HADIR} dan label WFH.
     */
    @Override
    public Absensi checkIn(User user, String keterangan) {
        return Absensi.builder()
                .user(user)
                .checkIn(LocalDateTime.now())
                .status(AbsensiStatus.HADIR)
                .tanggal(LocalDate.now())
                .keterangan("WFH: " + keterangan)
                .build();
    }

    /**
     * Memeriksa apakah strategy ini mendukung tipe "WFH" (case-insensitive).
     *
     * @param type tipe absensi dari request.
     * @return {@code true} jika {@code type} adalah "WFH" (tidak peka huruf besar/kecil).
     */
    @Override
    public boolean supports(String type) {
        return "WFH".equalsIgnoreCase(type);
    }
}
