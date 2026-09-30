package com.bookworm.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ReviewDto {

    public record ReviewResponse(
            UUID reviewId,
            UUID bookId,
            UUID userId,
            String userName,
            int rating,
            String body,
            LocalDateTime createdAt
    ) {}

    public record ReviewListResponse(
            List<ReviewResponse> data,
            PaginationDto pagination
    ) {}

    public record ReviewRequest(
            @Min(1) @Max(5) int rating,
            @NotBlank @Size(max = 2000) String body
    ) {}
}
