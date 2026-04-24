package com.absensi.absensi_app.dto.response;

import com.absensi.absensi_app.enums.AbsensiStatus;
import com.absensi.absensi_app.serializer.IndonesiaDateTimeSerializer;
import com.absensi.absensi_app.serializer.IndonesiaDateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class AbsensiResponse {
    private Long id;

    private String userName;

    @JsonSerialize(using = IndonesiaDateTimeSerializer.class)
    private LocalDateTime checkIn;

    @JsonSerialize(using = IndonesiaDateTimeSerializer.class)
    private LocalDateTime checkOut;

    private AbsensiStatus status;

    private String keterangan;

    @JsonSerialize(using = IndonesiaDateSerializer.class)
    private LocalDate tanggal;
}
