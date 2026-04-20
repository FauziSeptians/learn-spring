package com.absensi.absensi_app.strategy;

import com.absensi.absensi_app.entity.Absensi;
import com.absensi.absensi_app.entity.User;
import com.absensi.absensi_app.enums.AbsensiType;

public interface CheckInStrategy {
  Absensi checkIn(User user, String keterangan);

  boolean supports(AbsensiType type);
}
