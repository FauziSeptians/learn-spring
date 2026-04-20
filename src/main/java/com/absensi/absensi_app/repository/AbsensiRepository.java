package com.absensi.absensi_app.repository;

import com.absensi.absensi_app.entity.Absensi;
import com.absensi.absensi_app.enums.AbsensiStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AbsensiRepository extends JpaRepository<Absensi, Long> {

  Page<Absensi> findByUserId(Long userId, Pageable pageable);

  List<Absensi> findByTanggal(LocalDate tanggal);

  Optional<Absensi> findByUserIdAndTanggal(Long userId, LocalDate tanggal);

  List<Absensi> findByStatus(AbsensiStatus status);

  Optional<Absensi> findFirstByUserIdOrderByCheckInDesc(Long userId);
}
