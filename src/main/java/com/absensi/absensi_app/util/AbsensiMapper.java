package com.absensi.absensi_app.util;

import com.absensi.absensi_app.dto.response.AbsensiResponse;
import com.absensi.absensi_app.entity.Absensi;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class AbsensiMapper {

  public AbsensiResponse toResponse(Absensi absensi) {
    return AbsensiResponse.builder()
        .id(absensi.getId())
        .userName(absensi.getUser().getName())
        .checkIn(absensi.getCheckIn())
        .checkOut(absensi.getCheckOut())
        .status(absensi.getStatus())
        .keterangan(absensi.getKeterangan())
        .tanggal(absensi.getTanggal())
        .build();
  }

  public List<AbsensiResponse> toResponseList(List<Absensi> absensis) {
    return absensis.stream().map(this::toResponse).collect(Collectors.toList());
  }
}
