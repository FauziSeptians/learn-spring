package com.absensi.absensi_app.services.impl;

import com.absensi.absensi_app.dto.response.AbsensiResponse;
import com.absensi.absensi_app.dto.response.CheckoutErrorDataResponse;
import com.absensi.absensi_app.dto.response.PageResponse;
import com.absensi.absensi_app.entity.Absensi;
import com.absensi.absensi_app.entity.User;
import com.absensi.absensi_app.exception.ApiException;
import com.absensi.absensi_app.dto.rabbitmq.NotificationPayload;
import com.absensi.absensi_app.messages.producer.NotificationProducer;
import com.absensi.absensi_app.repository.AbsensiRepository;
import com.absensi.absensi_app.repository.UserRepository;
import com.absensi.absensi_app.services.AbsensiService;
import com.absensi.absensi_app.strategy.CheckInStrategy;
import com.absensi.absensi_app.annotation.LogExecutionTime;
import com.absensi.absensi_app.util.AbsensiMapper;
import com.absensi.absensi_app.util.PaginationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Implementasi dari {@link AbsensiService} yang mengelola seluruh logika bisnis absensi karyawan.
 *
 * <p>Class ini merupakan jantung dari aplikasi, mengintegrasikan beberapa design pattern
 * dan komponen infrastruktur sekaligus:
 * <ul>
 *   <li><b>Strategy Pattern</b>: Untuk menentukan logika check-in berdasarkan tipe
 *       (WFO, WFH) melalui {@link CheckInStrategy}.</li>
 *   <li><b>Message-Driven Architecture</b>: Mengirim notifikasi asinkron via RabbitMQ
 *       setiap kali terjadi event clock-in/clock-out.</li>
 *   <li><b>AOP (Aspect-Oriented Programming)</b>: Pengukuran waktu eksekusi method
 *       dilakukan secara transparan melalui anotasi {@link LogExecutionTime}.</li>
 * </ul>
 *
 * <p><b>Annotation pada class:</b>
 * <ul>
 *   <li>{@code @Slf4j} - Membuat logger {@code log} otomatis via Lombok.</li>
 *   <li>{@code @Service} - Mendaftarkan class sebagai Spring Bean di layer bisnis.</li>
 *   <li>{@code @RequiredArgsConstructor} - Mengimplementasikan Constructor Injection
 *       untuk semua field {@code final}.</li>
 * </ul>
 *
 * @author  Fauzi Septian
 * @version 1.0
 * @see     AbsensiService
 * @see     CheckInStrategy
 * @see     NotificationProducer
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AbsensiServiceImpl implements AbsensiService {

    /** Repository untuk akses data Absensi ke database. */
    private final AbsensiRepository absensiRepository;

    /** Repository untuk validasi keberadaan User sebelum proses absensi. */
    private final UserRepository userRepository;

    /** Producer RabbitMQ untuk mengirim notifikasi event absensi secara asinkron. */
    private final NotificationProducer notificationProducer;

    /**
     * Daftar semua implementasi {@link CheckInStrategy} yang terdaftar sebagai Spring Bean.
     * Spring secara otomatis mengumpulkan semua implementasi interface ini ke dalam sebuah {@link List}
     * melalui mekanisme <b>Collection Injection</b>. Saat ini berisi: {@code WfoCheckInStrategy}
     * dan {@code WfhCheckInStrategy}.
     */
    private final List<CheckInStrategy> strategies;

    /** Mapper untuk konversi antara entity {@link Absensi} ke DTO {@link AbsensiResponse}. */
    private final AbsensiMapper absensiMapper;

    /**
     * Memproses permintaan clock-in (masuk kerja) untuk seorang user.
     *
     * <p><b>Alur Logika Bisnis:</b>
     * <ol>
     *   <li>Validasi keberadaan user berdasarkan ID.</li>
     *   <li>Cek apakah user sudah melakukan absen hari ini (mencegah double check-in).</li>
     *   <li>Validasi batas waktu masuk: jam 09:15. Jika melewati batas, tolak check-in.</li>
     *   <li>Tentukan implementasi {@link CheckInStrategy} yang sesuai berdasarkan {@code type}
     *       (menggunakan Strategy Pattern).</li>
     *   <li>Simpan data absensi ke database.</li>
     *   <li>Kirim pesan notifikasi asinkron ke RabbitMQ.</li>
     * </ol>
     *
     * <p><b>Annotation yang digunakan:</b>
     * <ul>
     *   <li>{@code @Transactional} - Memastikan semua operasi database dalam method ini
     *       dieksekusi dalam satu transaksi atomik. Jika langkah nomor 5 gagal,
     *       tidak ada data yang tersimpan setengah-setengah.</li>
     *   <li>{@code @LogExecutionTime} - Anotasi kustom yang memicu {@code LoggingAspect}
     *       untuk mencatat durasi eksekusi method ini. Berguna untuk monitoring performa.</li>
     * </ul>
     *
     * @param userId     ID dari user yang melakukan check-in.
     * @param type       tipe absensi, misal "WFO" atau "WFH".
     * @param keterangan keterangan tambahan yang opsional dari karyawan.
     * @throws ApiException (404) jika user tidak ditemukan.
     * @throws ApiException (400) jika sudah absen hari ini.
     * @throws ApiException (400) jika melewati batas waktu masuk.
     * @throws ApiException (400) jika tipe absensi tidak dikenali.
     */
    @Override
    @Transactional
    @LogExecutionTime
    public void clockIn(Long userId, String type, String keterangan) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    String errorMessage = "User tidak ditemukan!";
                    log.error("CLOCK_IN_ERROR |  {}", errorMessage);
                    return new ApiException(errorMessage, HttpStatus.NOT_FOUND);
                });

        LocalDate date = LocalDate.now();
        log.debug("CLOCK_IN | date : [{}]", date);

        Optional<Absensi> checkAbsensi = absensiRepository.findByUserIdAndTanggal(userId, date);
        log.debug("CLOCK_IN | checkAbsensi : [{}]", checkAbsensi);

        if (checkAbsensi.isPresent()) {
            String errorMessage = "Kamu sudah absen clock-in";
            log.error("CLOCK_IN_ERROR | {}", errorMessage);
            throw new ApiException(errorMessage, HttpStatus.BAD_REQUEST);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalTime entryHours = LocalTime.of(9, 15);
        boolean workingLate = now.toLocalTime().isAfter(entryHours);

        if (workingLate) {
            String errorMessage = "Kamu telat masuk kantor!";
            log.error("CLOCK_IN_ERROR | {}", errorMessage);
            throw new ApiException(errorMessage, HttpStatus.BAD_REQUEST);
        }

        // Strategy Pattern: Cari implementasi CheckInStrategy yang mendukung 'type' ini.
        // Stream dan findFirst() digunakan untuk mencari elemen pertama yang cocok dari list.
        CheckInStrategy strategy = strategies.stream()
                .filter(s -> s.supports(type))
                .findFirst()
                .orElseThrow(() -> {
                    String errorMessage = "Tipe absensi '" + type + "' tidak didukung";
                    log.error("CLOCK_IN_ERROR | [{}]", errorMessage);
                    return new ApiException(errorMessage, HttpStatus.BAD_REQUEST);
                });

        Absensi absensi = strategy.checkIn(user, keterangan);
        absensiRepository.save(absensi);

        // Kirim pesan ke RabbitMQ secara asinkron. Proses ini tidak memblokir response ke klien.
        NotificationPayload payload = NotificationPayload.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .type("CLOCK_IN")
                .timestamp(absensi.getCheckIn())
                .message("User " + user.getName() + " melakukan Clock-In pada " + absensi.getCheckIn())
                .build();

        notificationProducer.sendClockInNotification(payload);
    }

    /**
     * Memproses permintaan clock-out (pulang kerja) untuk seorang user.
     *
     * <p><b>Alur Logika Bisnis:</b>
     * <ol>
     *   <li>Cari data absensi user untuk hari ini. Jika tidak ada berarti belum clock-in.</li>
     *   <li>Cek apakah sudah clock-out sebelumnya (mencegah double check-out).</li>
     *   <li>Hitung durasi kerja. Minimum kerja adalah 9 jam.
     *       Jika belum mencukupi, lempar error dengan detail info (jam masuk, jam eligible keluar, dll.).</li>
     *   <li>Set waktu clock-out dan simpan ke database.</li>
     *   <li>Kirim pesan notifikasi asinkron ke RabbitMQ.</li>
     * </ol>
     *
     * @param userId ID dari user yang melakukan clock-out.
     * @throws ApiException (404) jika data absensi hari ini tidak ditemukan (belum clock-in).
     * @throws ApiException (400) jika sudah melakukan clock-out.
     * @throws ApiException (400) dengan data detail jam jika belum memenuhi 9 jam kerja.
     */
    @Override
    @Transactional
    @LogExecutionTime
    public void clockOut(Long userId) {

        final boolean isCheckoutEligieble;

        Absensi absensi = absensiRepository.findByUserIdAndTanggal(userId, LocalDate.now())
                .orElseThrow(() -> {
                    String errorMessage = "Data absensi tidak ditemukan";
                    log.error("CLOCK_OUT_ERROR | [{}]", errorMessage);
                    return new ApiException(errorMessage, HttpStatus.NOT_FOUND);
                });

        if (absensi.getCheckOut() != null) {
            String errorMessage = "Anda sudah melakukan clock out hari ini";
            log.error("CLOCK_OUT_ERROR | [{}]", errorMessage);
            throw new ApiException(errorMessage, HttpStatus.BAD_REQUEST);
        }

        long workingHourRange = ChronoUnit.HOURS.between(absensi.getCheckIn(), LocalDateTime.now());
        isCheckoutEligieble = workingHourRange >= 9;

        if (!isCheckoutEligieble) {
            String errorMessage = "Anda belum memenuhi jam bekerja!";
            log.error("CLOCK_OUT_ERROR | [{}]", errorMessage);

            LocalDateTime checkInTime = absensi.getCheckIn();
            LocalDateTime now = LocalDateTime.now();

            long workingHour   = ChronoUnit.HOURS.between(checkInTime, now);
            long workingMinute = ChronoUnit.MINUTES.between(checkInTime, now) % 60;

            // Sertakan data detail sebagai context error agar klien bisa menampilkan info yang berguna
            CheckoutErrorDataResponse errorData = CheckoutErrorDataResponse.builder()
                .clockIn(checkInTime)
                .eligiebleCheckOut(checkInTime.plusHours(9))
                .workingOutInProgress(workingHour + " jam " + workingMinute + " menit")
                .minimumWorkingTime("9 jam")
                .build();

            throw new ApiException(errorMessage, HttpStatus.BAD_REQUEST, errorData);
        }

        absensi.setCheckOut(LocalDateTime.now());
        log.debug("CLOCK_OUT | Checkout time : [{}]", LocalDateTime.now());
        absensiRepository.save(absensi);

        NotificationPayload payload = NotificationPayload.builder()
                .userId(absensi.getUser().getId())
                .name(absensi.getUser().getName())
                .email(absensi.getUser().getEmail())
                .type("CLOCK_OUT")
                .timestamp(absensi.getCheckOut())
                .message("User " + absensi.getUser().getName() + " melakukan Clock-Out pada " + absensi.getCheckOut())
                .build();

        notificationProducer.sendClockOutNotification(payload);
    }

    /**
     * Mengambil riwayat absensi seorang user dengan dukungan paginasi.
     *
     * @param userId ID dari user yang riwayat absensinya ingin diambil.
     * @param page   nomor halaman yang diminta (1-based).
     * @param size   jumlah data per halaman.
     * @return {@link PageResponse} yang berisi daftar {@link AbsensiResponse} dan metadata paginasi.
     */
    @Override
    @LogExecutionTime
    public PageResponse<AbsensiResponse> getAttendanceByUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        log.debug("GET_ATTENDANCE_BY_USER | Pageable : [{}]", pageable);

        Page<Absensi> absensis = absensiRepository.findByUserId(userId, pageable);
        Page<AbsensiResponse> absensiResponse = absensis.map(absensiMapper::toResponse);

        log.debug("GET_ATTENDANCE_BY_USER | absensiUser : [{}]", absensiResponse);

        return PaginationMapper.of(absensiResponse);
    }
}
