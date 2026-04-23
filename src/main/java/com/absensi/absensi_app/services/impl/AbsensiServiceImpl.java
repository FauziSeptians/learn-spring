package com.absensi.absensi_app.services.impl;

import com.absensi.absensi_app.dto.response.AbsensiResponse;
import com.absensi.absensi_app.dto.response.PageResponse;
import com.absensi.absensi_app.entity.Absensi;
import com.absensi.absensi_app.entity.User;
import com.absensi.absensi_app.exception.ApiException;
import com.absensi.absensi_app.repository.AbsensiRepository;
import com.absensi.absensi_app.repository.UserRepository;
import com.absensi.absensi_app.services.AbsensiService;
import com.absensi.absensi_app.strategy.CheckInStrategy;
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
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AbsensiServiceImpl implements AbsensiService {

    private final AbsensiRepository absensiRepository;
    private final UserRepository userRepository;
    private final List<CheckInStrategy> strategies;

    @Override
    @Transactional
    public void clockIn(Long userId, String type, String keterangan) {

        log.info("CLOCK_IN_START | userId : [{}], type : [{}], keterangan : [{}]", userId, type, keterangan);

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

        log.info("CLOCK_IN_SUCCESS | Absensi : [{}]", absensi);
    }

    @Override
    @Transactional
    public void clockOut(Long userId) {

        Absensi absensi = absensiRepository.findFirstByUserIdOrderByCheckInDesc(userId)
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

        absensi.setCheckOut(LocalDateTime.now());

        log.debug("CLOCK_OUT | Checkout time : [{}]", LocalDateTime.now());

        absensiRepository.save(absensi);

        log.info("CLOCK_OUT_SUCCESS | Absensi : [{}]", absensi);
    }

    @Override
    public PageResponse<AbsensiResponse> getAttendanceByUser(Long userId, int page, int size) {

        log.info("GET_ATTENDANCE_BY_USER_START | userId : [{}] | page : [{}] | size : [{}]", userId, page
        , size);

        Pageable pageable = PageRequest.of(page - 1, size);

        log.debug("GET_ATTENDANCE_BY_USER | Pageable : [{}]", pageable);

        Page<Absensi> absensis = absensiRepository.findByUserId(userId, pageable);

        Page<AbsensiResponse> absensiResponse = absensis.map(AbsensiMapper::toResponse);

        log.debug("GET_ATTENDANCE_BY_USER | absensiUser : [{}]", absensiResponse);

        log.info("GET_ATTENDANCE_BY_USER_SUCCESS");

        return PaginationMapper.of(absensiResponse);
    }
}
