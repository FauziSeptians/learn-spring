package com.absensi.absensi_app.services.impl;

import com.absensi.absensi_app.entity.Absensi;
import com.absensi.absensi_app.entity.User;
import com.absensi.absensi_app.exception.ApiException;
import com.absensi.absensi_app.repository.AbsensiRepository;
import com.absensi.absensi_app.repository.UserRepository;
import com.absensi.absensi_app.services.AbsensiService;
import com.absensi.absensi_app.strategy.CheckInStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
    public List<Absensi> getAttendanceByUser(Long userId) {
        return absensiRepository.findByUserId(userId);
    }
}
