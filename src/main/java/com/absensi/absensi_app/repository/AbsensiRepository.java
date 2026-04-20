package com.absensi.absensi_app.repository;

import com.absensi.absensi_app.entity.Absensi;
import com.absensi.absensi_app.enums.AbsensiStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AbsensiRepository extends JpaRepository<Absensi, Long> {

    List<Absensi> findByUserId(Long userId);

    List<Absensi> findByTanggal(LocalDate tanggal);

    Optional<Absensi> findByUserIdAndTanggal(Long userId, LocalDate tanggal);

    List<Absensi> findByStatus (AbsensiStatus status);
}
