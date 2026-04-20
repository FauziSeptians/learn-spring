package com.absensi.absensi_app.dto.request;

import com.absensi.absensi_app.enums.AbsensiType;
import lombok.Data;

@Data
public class CheckInRequest {
  private AbsensiType type;
  private String keterangan;
}
