package com.bookworm.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.UUID;

public class WishlistDto {

    public record WishlistResponse(
            UUID wishlistId,
            List<BookDto.BookSummaryDto> items
    ) {}

    public record AddItemRequest(
            @NotBlank String bookId
    ) {}
}
