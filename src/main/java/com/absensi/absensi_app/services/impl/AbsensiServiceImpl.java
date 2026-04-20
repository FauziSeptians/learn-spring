package com.absensi.absensi_app.services.impl;

import com.absensi.absensi_app.entity.Absensi;
import com.absensi.absensi_app.entity.User;
import com.absensi.absensi_app.enums.AbsensiType;
import com.absensi.absensi_app.exception.BadRequestException;
import com.absensi.absensi_app.exception.ResourceNotFoundException;
import com.absensi.absensi_app.repository.AbsensiRepository;
import com.absensi.absensi_app.repository.UserRepository;
import com.absensi.absensi_app.services.AbsensiService;
import com.absensi.absensi_app.strategy.CheckInStrategy;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AbsensiServiceImpl implements AbsensiService {

  private final AbsensiRepository absensiRepository;
  private final UserRepository userRepository;
  private final List<CheckInStrategy> strategies;

  @Override
  @Transactional
  public void clockIn(Long userId, AbsensiType type, String keterangan) {
    log.info("User {} attempting to clock-in with type {}", userId, type);

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

    // Strategy Pattern: Mencari strategy yang sesuai
    CheckInStrategy strategy =
        strategies.stream()
            .filter(s -> s.supports(type))
            .findFirst()
            .orElseThrow(
                () -> new BadRequestException("Tipe absensi '" + type + "' tidak didukung"));

    Absensi absensi = strategy.checkIn(user, keterangan);
    absensiRepository.save(absensi);

    log.info("User {} successfully clocked-in", userId);
  }

  @Override
  @Transactional
  public void clockOut(Long userId) {
    log.info("User {} attempting to clock-out", userId);

    Absensi absensi =
        absensiRepository
            .findFirstByUserIdOrderByCheckInDesc(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Data absensi tidak ditemukan"));

    if (absensi.getCheckOut() != null) {
      log.warn("User {} already clocked-out today", userId);
      throw new BadRequestException("Anda sudah melakukan clock out hari ini");
    }

    absensi.setCheckOut(LocalDateTime.now());
    absensiRepository.save(absensi);

    log.info("User {} successfully clocked-out", userId);
  }

  @Override
  public Page<Absensi> getAttendanceByUser(Long userId, Pageable pageable) {
    return absensiRepository.findByUserId(userId, pageable);
  }
}
