package com.bookworm.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class CartDto {

    public record CartItemResponse(
            UUID itemId,
            BookDto.BookSummaryDto book,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) {}

    public record CartResponse(
            UUID cartId,
            List<CartItemResponse> items,
            int itemCount,
            BigDecimal subtotal,
            String currency
    ) {}

    public record AddItemRequest(
            @NotBlank String bookId,
            @Min(1) int quantity
    ) {}

    public record UpdateItemRequest(
            @Min(1) int quantity
    ) {}
}
