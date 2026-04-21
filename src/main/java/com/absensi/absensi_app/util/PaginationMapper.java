package com.absensi.absensi_app.util;

import com.absensi.absensi_app.dto.response.PageResponse;
import org.springframework.data.domain.Page;

public class PaginationMapper {
    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
            .content(page.getContent())
            .currentPage(page.getNumber() + 1)
            .totalPages(page.getTotalPages())
            .totalElements(page.getTotalElements())
            .pageSize(page.getSize())
            .isFirst(page.isFirst())
            .isLast(page.isLast())
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .build();
    }
}
