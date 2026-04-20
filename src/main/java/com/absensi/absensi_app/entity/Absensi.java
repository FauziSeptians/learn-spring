package com.absensi.absensi_app.entity;

import com.absensi.absensi_app.enums.AbsensiStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "absensis")
public class Absensi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name =  "userId", unique = true , nullable = false)
    @ManyToOne()
    @JoinColumn(name = "userId", nullable = false)
    private String userId;

    @Column(name = "checkIn", nullable = false)
    private LocalDateTime checkIn;

    @Column(name = "checkOut")
    private  LocalDateTime checkOut;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AbsensiStatus status;

    @Column(name = "keterangan")
    private String keterangan;

    @Column(name = "tanggal")
    private LocalDate tanggal;

    @CreationTimestamp
    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;
}
