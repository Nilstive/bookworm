package com.bookworm.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public class CheckoutDto {

    public record CheckoutSummaryResponse(
            BigDecimal subtotal,
            BigDecimal tax,
            BigDecimal deliveryCharges,
            String couponCode,
            BigDecimal discount,
            BigDecimal totalPayable,
            String currency
    ) {}

    public record CouponRequest(
            @NotBlank String couponCode
    ) {}
}
