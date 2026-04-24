package com.absensi.absensi_app.util;

import com.absensi.absensi_app.dto.response.AbsensiResponse;
import com.absensi.absensi_app.entity.Absensi;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AbsensiMapper {

    AbsensiResponse toResponse(Absensi absensi);
}
