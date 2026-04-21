package com.absensi.absensi_app.services;

import com.absensi.absensi_app.dto.response.AbsensiResponse;
import com.absensi.absensi_app.dto.response.PageResponse;
import com.absensi.absensi_app.entity.Absensi;

import java.util.List;

public interface AbsensiService {
    void clockIn(Long userId, String type, String keterangan);
    void clockOut(Long userId);
    PageResponse<AbsensiResponse> getAttendanceByUser(Long userId, int page, int size);
}
