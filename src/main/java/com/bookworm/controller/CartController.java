package com.bookworm.controller;

import com.bookworm.dto.CartDto;
import com.bookworm.service.CartService;
import com.bookworm.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartDto.CartResponse> getCart() {
        return ResponseEntity.ok(cartService.getCart(SecurityUtils.currentUserId()));
    }

    @PostMapping("/items")
    public ResponseEntity<CartDto.CartResponse> addItem(@Valid @RequestBody CartDto.AddItemRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cartService.addItem(SecurityUtils.currentUserId(), req));
    }

    @PatchMapping("/items/{itemId}")
    public ResponseEntity<CartDto.CartResponse> updateItem(
            @PathVariable UUID itemId,
            @Valid @RequestBody CartDto.UpdateItemRequest req) {
        return ResponseEntity.ok(cartService.updateItem(SecurityUtils.currentUserId(), itemId, req));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartDto.CartResponse> removeItem(@PathVariable UUID itemId) {
        return ResponseEntity.ok(cartService.removeItem(SecurityUtils.currentUserId(), itemId));
    }
}
