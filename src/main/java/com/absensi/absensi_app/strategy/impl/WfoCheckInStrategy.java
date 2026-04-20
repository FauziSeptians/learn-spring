package com.absensi.absensi_app.strategy.impl;

import com.absensi.absensi_app.entity.Absensi;
import com.absensi.absensi_app.entity.User;
import com.absensi.absensi_app.enums.AbsensiStatus;
import com.absensi.absensi_app.strategy.CheckInStrategy;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class WfoCheckInStrategy implements CheckInStrategy {
    @Override
    public Absensi checkIn(User user, String keterangan) {
        return Absensi.builder()
                .user(user)
                .checkIn(LocalDateTime.now())
                .status(AbsensiStatus.HADIR)
                .tanggal(LocalDate.now())
                .keterangan("WFO: " + keterangan)
                .build();
    }

    @Override
    public boolean supports(String type) {
        return "WFO".equalsIgnoreCase(type);
    }
}
