package com.bookworm.controller;

import com.bookworm.dto.OrderDto;
import com.bookworm.dto.PaymentDto;
import com.bookworm.service.OrderService;
import com.bookworm.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class PaymentOrderController {

    private final OrderService orderService;

    public PaymentOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // ── Payments ──────────────────────────────────────────────────────────

    @PostMapping("/payments")
    public ResponseEntity<PaymentDto.PaymentResponse> initiatePayment(
            @Valid @RequestBody PaymentDto.PaymentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.initiatePayment(SecurityUtils.currentUserId(), req));
    }

    @GetMapping("/payments/{transactionId}")
    public ResponseEntity<PaymentDto.PaymentResponse> getPaymentStatus(
            @PathVariable UUID transactionId) {
        return ResponseEntity.ok(orderService.getPaymentStatus(transactionId));
    }

    // ── Orders ────────────────────────────────────────────────────────────

    @GetMapping("/orders")
    public ResponseEntity<OrderDto.OrderListResponse> listOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ResponseEntity.ok(
                orderService.listOrders(SecurityUtils.currentUserId(), page, pageSize));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderDto.OrderResponse> getOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(
                orderService.getOrder(SecurityUtils.currentUserId(), orderId));
    }
}
