package com.bookworm.service;

import com.bookworm.dto.WishlistDto;
import com.bookworm.entity.BookEntity;
import com.bookworm.entity.UserEntity;
import com.bookworm.entity.WishlistEntity;
import com.bookworm.exception.ResourceNotFoundException;
import com.bookworm.repository.BookRepository;
import com.bookworm.repository.UserRepository;
import com.bookworm.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public WishlistService(WishlistRepository wishlistRepository,
                           BookRepository bookRepository,
                           UserRepository userRepository) {
        this.wishlistRepository = wishlistRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    public WishlistDto.WishlistResponse getWishlist(UUID userId) {
        return toResponse(getOrCreate(userId));
    }

    @Transactional
    public WishlistDto.WishlistResponse addItem(UUID userId, UUID bookId) {
        WishlistEntity wishlist = getOrCreate(userId);
        BookEntity book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + bookId));
        if (!wishlist.getBooks().contains(book)) {
            wishlist.getBooks().add(book);
            wishlistRepository.save(wishlist);
        }
        return toResponse(wishlist);
    }

    @Transactional
    public WishlistDto.WishlistResponse removeItem(UUID userId, UUID bookId) {
        WishlistEntity wishlist = getOrCreate(userId);
        wishlist.getBooks().removeIf(b -> b.getId().equals(bookId));
        wishlistRepository.save(wishlist);
        return toResponse(wishlist);
    }

    // ── helpers ────────────────────────────────────────────────────────────

    @Transactional
    private WishlistEntity getOrCreate(UUID userId) {
        return wishlistRepository.findByUserId(userId).orElseGet(() -> {
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
            return wishlistRepository.save(WishlistEntity.builder().user(user).build());
        });
    }

    private WishlistDto.WishlistResponse toResponse(WishlistEntity w) {
        var books = w.getBooks().stream().map(b ->
                new com.bookworm.dto.BookDto.BookSummaryDto(
                        b.getId(), b.getTitle(),
                        new com.bookworm.dto.BookDto.AuthorRefDto(b.getAuthor().getId(), b.getAuthor().getName()),
                        b.getCoverImageUrl(), b.getFormat().name(),
                        b.getCategories(), b.getPrice(), b.getCurrency(),
                        b.getEstimatedDelivery(), b.getRating())
        ).toList();
        return new WishlistDto.WishlistResponse(w.getId(), books);
    }
}
