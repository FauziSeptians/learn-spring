package com.absensi.absensi_app.strategy.impl;

import com.absensi.absensi_app.entity.Absensi;
import com.absensi.absensi_app.entity.User;
import com.absensi.absensi_app.enums.AbsensiStatus;
import com.absensi.absensi_app.enums.AbsensiType;
import com.absensi.absensi_app.strategy.CheckInStrategy;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class WfhCheckInStrategy implements CheckInStrategy {
  @Override
  public Absensi checkIn(User user, String keterangan) {
    // Logic khusus WFH, misalnya statusnya tetep HADIR tapi ada label WFH
    return Absensi.builder()
        .user(user)
        .checkIn(LocalDateTime.now())
        .status(AbsensiStatus.HADIR)
        .tanggal(LocalDate.now())
        .keterangan("WFH: " + keterangan)
        .build();
  }

  @Override
  public boolean supports(AbsensiType type) {
    return AbsensiType.WFH == type;
  }
}
