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

@Service
@RequiredArgsConstructor
public class AbsensiServiceImpl implements AbsensiService {

    private final AbsensiRepository absensiRepository;
    private final UserRepository userRepository;
    private final List<CheckInStrategy> strategies;

    @Override
    @Transactional
    public void clockIn(Long userId, String type, String keterangan) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User tidak ditemukan", HttpStatus.NOT_FOUND));

        LocalDate date = LocalDate.now();

        Optional<Absensi> checkAbsensi = absensiRepository.findByUserIdAndTanggal(userId, date);

        if(checkAbsensi.isPresent()){
            throw  new ApiException("Kamu sudah absen clock-in", HttpStatus.BAD_REQUEST);
        }

        // Strategy Pattern: Mencari strategy yang sesuai
        CheckInStrategy strategy = strategies.stream()
                .filter(s -> s.supports(type))
                .findFirst()
                .orElseThrow(() -> new ApiException("Tipe absensi '" + type + "' tidak didukung", HttpStatus.BAD_REQUEST));

        Absensi absensi = strategy.checkIn(user, keterangan);
        absensiRepository.save(absensi);
    }

    @Override
    @Transactional
    public void clockOut(Long userId) {
        // Logika clock out sederhana
        Absensi absensi = absensiRepository.findFirstByUserIdOrderByCheckInDesc(userId)
                .orElseThrow(() -> new ApiException("Data absensi tidak ditemukan", HttpStatus.NOT_FOUND));

        if (absensi.getCheckOut() != null) {
            throw new ApiException("Anda sudah melakukan clock out hari ini", HttpStatus.BAD_REQUEST);
        }

        absensi.setCheckOut(LocalDateTime.now());
        absensiRepository.save(absensi);
    }

    @Override
    public PageResponse<AbsensiResponse> getAttendanceByUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Absensi> absensis = absensiRepository.findByUserId(userId, pageable);

        Page<AbsensiResponse> absensiResponse = absensis.map(AbsensiMapper::toResponse);
        return PaginationMapper.of(absensiResponse);
    }
}
