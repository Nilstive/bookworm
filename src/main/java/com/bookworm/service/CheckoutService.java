package com.bookworm.service;

import com.bookworm.dto.AddressDto;
import com.bookworm.dto.CheckoutDto;
import com.bookworm.entity.AddressEntity;
import com.bookworm.entity.CartEntity;
import com.bookworm.exception.BadRequestException;
import com.bookworm.exception.ResourceNotFoundException;
import com.bookworm.repository.AddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CheckoutService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.12");
    private static final BigDecimal COUPON_DISCOUNT = new BigDecimal("100.00"); // flat demo discount

    private final CartService cartService;
    private final AddressRepository addressRepository;

    public CheckoutService(CartService cartService, AddressRepository addressRepository) {
        this.cartService = cartService;
        this.addressRepository = addressRepository;
    }

    public CheckoutDto.CheckoutSummaryResponse getSummary(UUID userId) {
        return buildSummary(userId, null, null);
    }

    public CheckoutDto.CheckoutSummaryResponse applyCoupon(UUID userId, String couponCode) {
        // Stub: any non-empty code gives the flat discount
        if (couponCode == null || couponCode.isBlank()) {
            throw new BadRequestException("Invalid coupon code");
        }
        return buildSummary(userId, couponCode, COUPON_DISCOUNT);
    }

    @Transactional
    public CheckoutDto.CheckoutSummaryResponse setAddress(UUID userId, AddressDto.AddressRequest req) {
        // Validate saved address exists if an ID is provided
        if (req.addressId() != null) {
            addressRepository.findById(UUID.fromString(req.addressId()))
                    .filter(a -> a.getUser().getId().equals(userId))
                    .orElseThrow(() -> new ResourceNotFoundException("Saved address not found"));
        }
        return buildSummary(userId, null, null);
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private CheckoutDto.CheckoutSummaryResponse buildSummary(UUID userId, String coupon,
                                                              BigDecimal discount) {
        CartEntity cart = cartService.getOrCreateCart(userId);
        BigDecimal subtotal = cart.getItems().stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal delivery = BigDecimal.ZERO;
        BigDecimal disc = discount != null ? discount : BigDecimal.ZERO;
        BigDecimal total = subtotal.add(tax).add(delivery).subtract(disc)
                .max(BigDecimal.ZERO);
        return new CheckoutDto.CheckoutSummaryResponse(
                subtotal, tax, delivery, coupon, disc, total, cart.getCurrency());
    }
}
