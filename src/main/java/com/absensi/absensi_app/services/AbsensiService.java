package com.absensi.absensi_app.services;

import com.absensi.absensi_app.entity.Absensi;
import com.absensi.absensi_app.enums.AbsensiType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AbsensiService {
  void clockIn(Long userId, AbsensiType type, String keterangan);

  void clockOut(Long userId);

  Page<Absensi> getAttendanceByUser(Long userId, Pageable pageable);
}
