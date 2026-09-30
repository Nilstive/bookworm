package com.bookworm.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class BookDto {

    public record AuthorRefDto(UUID authorId, String name) {}

    public record BookSummaryDto(
            UUID bookId,
            String title,
            AuthorRefDto author,
            String coverImageUrl,
            String format,
            List<String> categories,
            BigDecimal price,
            String currency,
            String estimatedDelivery,
            BigDecimal rating
    ) {}

    public record BookDetailDto(
            UUID bookId,
            String title,
            AuthorRefDto author,
            String coverImageUrl,
            String format,
            List<String> categories,
            BigDecimal price,
            String currency,
            String estimatedDelivery,
            BigDecimal rating,
            String description,
            String publisherName,
            String publishedDate,
            String language,
            String isbn,
            int totalSales,
            int totalReviews,
            String authorBio,
            List<BookSummaryDto> relatedBooks
    ) {}

    public record BookListResponse(
            List<BookSummaryDto> data,
            PaginationDto pagination
    ) {}

    public record BookSearchParams(
            String q,
            String category,
            String language,
            String format,
            BigDecimal priceMin,
            BigDecimal priceMax,
            String sortBy,
            int page,
            int pageSize
    ) {}
}
