package com.bookworm.controller;

import com.bookworm.dto.WishlistDto;
import com.bookworm.service.WishlistService;
import com.bookworm.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public ResponseEntity<WishlistDto.WishlistResponse> getWishlist() {
        return ResponseEntity.ok(wishlistService.getWishlist(SecurityUtils.currentUserId()));
    }

    @PostMapping("/items")
    public ResponseEntity<WishlistDto.WishlistResponse> addItem(
            @Valid @RequestBody WishlistDto.AddItemRequest req) {
        UUID bookId = UUID.fromString(req.bookId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(wishlistService.addItem(SecurityUtils.currentUserId(), bookId));
    }

    @DeleteMapping("/items/{bookId}")
    public ResponseEntity<WishlistDto.WishlistResponse> removeItem(@PathVariable UUID bookId) {
        return ResponseEntity.ok(wishlistService.removeItem(SecurityUtils.currentUserId(), bookId));
    }
}
