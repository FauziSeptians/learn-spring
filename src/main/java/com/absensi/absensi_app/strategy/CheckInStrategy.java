package com.absensi.absensi_app.strategy;

import com.absensi.absensi_app.entity.Absensi;
import com.absensi.absensi_app.entity.User;

public interface CheckInStrategy {
  Absensi checkIn(User user, String keterangan);

  boolean supports(String type);
}
