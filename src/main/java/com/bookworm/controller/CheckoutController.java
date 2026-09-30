package com.bookworm.controller;

import com.bookworm.dto.AddressDto;
import com.bookworm.dto.CheckoutDto;
import com.bookworm.service.CheckoutService;
import com.bookworm.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @GetMapping("/summary")
    public ResponseEntity<CheckoutDto.CheckoutSummaryResponse> getSummary() {
        return ResponseEntity.ok(checkoutService.getSummary(SecurityUtils.currentUserId()));
    }

    @PostMapping("/coupon")
    public ResponseEntity<CheckoutDto.CheckoutSummaryResponse> applyCoupon(
            @Valid @RequestBody CheckoutDto.CouponRequest req) {
        return ResponseEntity.ok(
                checkoutService.applyCoupon(SecurityUtils.currentUserId(), req.couponCode()));
    }

    @PostMapping("/address")
    public ResponseEntity<CheckoutDto.CheckoutSummaryResponse> setAddress(
            @Valid @RequestBody AddressDto.AddressRequest req) {
        return ResponseEntity.ok(
                checkoutService.setAddress(SecurityUtils.currentUserId(), req));
    }
}
