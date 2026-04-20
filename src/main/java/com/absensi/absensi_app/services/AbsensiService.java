package com.absensi.absensi_app.services;

import com.absensi.absensi_app.entity.Absensi;

import java.util.List;

public interface AbsensiService {
    void clockIn(Long id);
    void clockOut(Long id);
    List<Absensi> getAttendanceByUser(Long id);
}
