package com.absensi.absensi_app.dto.response;

import com.absensi.absensi_app.enums.AbsensiStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AbsensiResponse {
  private Long id;
  private String userName;
  private LocalDateTime checkIn;
  private LocalDateTime checkOut;
  private AbsensiStatus status;
  private String keterangan;
  private LocalDate tanggal;
}
