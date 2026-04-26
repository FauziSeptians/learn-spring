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

@Slf4j
@Service
@RequiredArgsConstructor
public class AbsensiServiceImpl implements AbsensiService {

    private final AbsensiRepository absensiRepository;
    private final UserRepository userRepository;
    private final NotificationProducer notificationProducer;
    private final List<CheckInStrategy> strategies;
    private final AbsensiMapper absensiMapper;

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

        if(checkAbsensi.isPresent()){
            String errorMessage = "Kamu sudah absen clock-in";

            log.error("CLOCK_IN_ERROR | {}", errorMessage);

            throw  new ApiException(errorMessage, HttpStatus.BAD_REQUEST);
        }

        LocalDateTime now = LocalDateTime.now();

        LocalTime entryHours = LocalTime.of(9, 15);

        boolean workingLate = now.toLocalTime().isAfter(entryHours);

        if (workingLate) {
            String errorMessage = "Kamu telat masuk kantor!";

            log.error("CLOCK_IN_ERROR | {}", errorMessage);

            throw  new ApiException(errorMessage, HttpStatus.BAD_REQUEST);
        }

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

        // [Phase 4] Kirim Pesan ke RabbitMQ
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

        if(!isCheckoutEligieble){
            String errorMessage = "Anda belum memenuhi jam bekerja!";

            log.error("CLOCK_OUT_ERROR | [{}]", errorMessage);

            LocalDateTime checkInTime = absensi.getCheckIn();
            LocalDateTime now = LocalDateTime.now();

            long workingHour = ChronoUnit.HOURS.between(checkInTime, now);
            long workingMinute = ChronoUnit.MINUTES.between(checkInTime, now) % 60;

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

        // [Phase 4] Kirim Pesan ke RabbitMQ
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
