package com.bookworm.dto;

public record PaginationDto(
        int page,
        int pageSize,
        long totalItems,
        int totalPages
) {}
