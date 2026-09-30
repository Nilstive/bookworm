package com.bookworm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PaymentDto {

    public record CardDetailsRequest(
            @NotBlank @Pattern(regexp = "\\d{16}") String cardNumber,
            @NotBlank String nameOnCard,
            @NotBlank @Pattern(regexp = "\\d{3,4}") String cvv,
            @NotNull int expiryMonth,
            @NotNull int expiryYear
    ) {}

    public record PaymentRequest(
            @NotBlank String paymentMethod,
            CardDetailsRequest cardDetails,
            String upiId,
            String walletProvider
    ) {}

    public record PaymentResponse(
            UUID transactionId,
            UUID orderId,
            String status,
            BigDecimal payableAmount,
            String currency,
            String paymentMethod,
            LocalDateTime paidAt
    ) {}
}
