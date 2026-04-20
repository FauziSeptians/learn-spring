package com.absensi.absensi_app.services;

import com.absensi.absensi_app.entity.Absensi;

import java.util.List;

public interface AbsensiService {
    void clockIn(Long userId, String type, String keterangan);
    void clockOut(Long userId);
    List<Absensi> getAttendanceByUser(Long userId);
}
