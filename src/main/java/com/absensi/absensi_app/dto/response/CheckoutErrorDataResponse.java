package com.absensi.absensi_app.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CheckoutErrorDataResponse {
    private LocalDateTime clockIn;
    private LocalDateTime eligiebleCheckOut;
    private String workingOutInProgress;
    private String minimumWorkingTime;
}
