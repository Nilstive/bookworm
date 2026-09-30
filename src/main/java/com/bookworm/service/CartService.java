package com.bookworm.service;

import com.bookworm.dto.CartDto;
import com.bookworm.entity.*;
import com.bookworm.exception.BadRequestException;
import com.bookworm.exception.ResourceNotFoundException;
import com.bookworm.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       BookRepository bookRepository,
                       UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    public CartDto.CartResponse getCart(UUID userId) {
        CartEntity cart = getOrCreateCart(userId);
        return toResponse(cart);
    }

    @Transactional
    public CartDto.CartResponse addItem(UUID userId, CartDto.AddItemRequest req) {
        CartEntity cart = getOrCreateCart(userId);
        UUID bookId = UUID.fromString(req.bookId());
        BookEntity book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + bookId));

        CartItemEntity item = cartItemRepository
                .findByCartIdAndBookId(cart.getId(), bookId)
                .orElseGet(() -> CartItemEntity.builder()
                        .cart(cart).book(book).unitPrice(book.getPrice()).quantity(0).build());

        item.setQuantity(item.getQuantity() + req.quantity());
        cartItemRepository.save(item);
        return toResponse(cartRepository.findById(cart.getId()).orElseThrow());
    }

    @Transactional
    public CartDto.CartResponse updateItem(UUID userId, UUID itemId, CartDto.UpdateItemRequest req) {
        CartEntity cart = getOrCreateCart(userId);
        CartItemEntity item = cartItemRepository.findById(itemId)
                .filter(i -> i.getCart().getId().equals(cart.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + itemId));
        item.setQuantity(req.quantity());
        cartItemRepository.save(item);
        return toResponse(cartRepository.findById(cart.getId()).orElseThrow());
    }

    @Transactional
    public CartDto.CartResponse removeItem(UUID userId, UUID itemId) {
        CartEntity cart = getOrCreateCart(userId);
        CartItemEntity item = cartItemRepository.findById(itemId)
                .filter(i -> i.getCart().getId().equals(cart.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + itemId));
        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        return toResponse(cartRepository.findById(cart.getId()).orElseThrow());
    }

    // ── helpers ────────────────────────────────────────────────────────────

    @Transactional
    public CartEntity getOrCreateCart(UUID userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
            return cartRepository.save(CartEntity.builder().user(user).build());
        });
    }

    public CartDto.CartResponse toResponse(CartEntity cart) {
        List<CartDto.CartItemResponse> items = cart.getItems().stream()
                .map(i -> new CartDto.CartItemResponse(
                        i.getId(),
                        toBookSummary(i.getBook()),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity()))
                )).toList();
        BigDecimal subtotal = items.stream()
                .map(CartDto.CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartDto.CartResponse(cart.getId(), items, items.size(), subtotal, cart.getCurrency());
    }

    private com.bookworm.dto.BookDto.BookSummaryDto toBookSummary(BookEntity b) {
        return new com.bookworm.dto.BookDto.BookSummaryDto(
                b.getId(), b.getTitle(),
                new com.bookworm.dto.BookDto.AuthorRefDto(b.getAuthor().getId(), b.getAuthor().getName()),
                b.getCoverImageUrl(), b.getFormat().name(),
                b.getCategories(), b.getPrice(), b.getCurrency(),
                b.getEstimatedDelivery(), b.getRating());
    }
}
