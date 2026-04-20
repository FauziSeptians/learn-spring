package com.absensi.absensi_app.controller;

import com.absensi.absensi_app.dto.request.CheckInRequest;
import com.absensi.absensi_app.dto.response.AbsensiResponse;
import com.absensi.absensi_app.dto.response.ApiResponse;
import com.absensi.absensi_app.entity.Absensi;
import com.absensi.absensi_app.services.AbsensiService;
import com.absensi.absensi_app.util.AbsensiMapper;
import com.absensi.absensi_app.util.ApiMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/absensi")
@RequiredArgsConstructor
@Tag(name = "Absensi", description = "API untuk manajemen absensi")
@SecurityRequirement(name = "Bearer Authentication")
public class AbsensiController {

  private final AbsensiService absensiService;
  private final AbsensiMapper absensiMapper;

  @PostMapping("/check-in/{userId}")
  @Operation(summary = "Check In", description = "Melakukan check-in untuk user tertentu")
  public ResponseEntity<ApiResponse<Void>> checkIn(
      @PathVariable Long userId, @Valid @RequestBody CheckInRequest request) {
    absensiService.clockIn(userId, request.getType(), request.getKeterangan());
    return ResponseEntity.ok(ApiMapper.success("Berhasil melakukan check-in"));
  }

  @PostMapping("/check-out/{userId}")
  @Operation(summary = "Check Out", description = "Melakukan check-out untuk user tertentu")
  public ResponseEntity<ApiResponse<Void>> checkOut(@PathVariable Long userId) {
    absensiService.clockOut(userId);
    return ResponseEntity.ok(ApiMapper.success("Berhasil melakukan check-out"));
  }

  @GetMapping("/history/{userId}")
  @Operation(
      summary = "Get History Absensi",
      description = "Mengambil riwayat absensi user tertentu")
  public ResponseEntity<ApiResponse<Page<AbsensiResponse>>> getHistory(
      @PathVariable Long userId, Pageable pageable) {
    Page<Absensi> absensiPage = absensiService.getAttendanceByUser(userId, pageable);
    Page<AbsensiResponse> responses = absensiPage.map(absensiMapper::toResponse);
    return ResponseEntity.ok(ApiMapper.success("Berhasil mengambil riwayat absensi", responses));
  }
}
