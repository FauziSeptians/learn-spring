package com.absensi.absensi_app.dto.request;

import jakarta.annotation.Nullable;
import lombok.Data;

@Data
public class CheckInRequest {
    private String type;
    private String keterangan;
}
