package com.bookworm.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class OrderDto {

    public record OrderItemResponse(
            UUID itemId,
            BookDto.BookSummaryDto book,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) {}

    public record OrderResponse(
            UUID orderId,
            UUID userId,
            List<OrderItemResponse> items,
            AddressDto.AddressResponse deliveryAddress,
            CheckoutDto.CheckoutSummaryResponse checkoutSummary,
            PaymentDto.PaymentResponse payment,
            String status,
            LocalDateTime placedAt,
            String estimatedDelivery
    ) {}

    public record OrderListResponse(
            List<OrderResponse> data,
            PaginationDto pagination
    ) {}
}
