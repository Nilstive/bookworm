package com.bookworm.controller;

import com.bookworm.dto.BookDto;
import com.bookworm.dto.ReviewDto;
import com.bookworm.service.BookService;
import com.bookworm.service.ReviewService;
import com.bookworm.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class BookController {

    private final BookService bookService;
    private final ReviewService reviewService;

    public BookController(BookService bookService, ReviewService reviewService) {
        this.bookService = bookService;
        this.reviewService = reviewService;
    }

    @GetMapping("/books")
    public ResponseEntity<BookDto.BookListResponse> listBooks(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String format,
            @RequestParam(required = false) BigDecimal priceMin,
            @RequestParam(required = false) BigDecimal priceMax,
            @RequestParam(defaultValue = "relevance") String sortBy,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        var params = new BookDto.BookSearchParams(q, category, language, format,
                priceMin, priceMax, sortBy, page, pageSize);
        return ResponseEntity.ok(bookService.searchBooks(params));
    }

    @GetMapping("/books/recommended")
    public ResponseEntity<BookDto.BookListResponse> recommended(
            @RequestParam(defaultValue = "12") int pageSize) {
        return ResponseEntity.ok(bookService.getRecommended(pageSize));
    }

    @GetMapping("/books/bestsellers")
    public ResponseEntity<BookDto.BookListResponse> bestsellers(
            @RequestParam(defaultValue = "12") int pageSize) {
        return ResponseEntity.ok(bookService.getBestsellers(pageSize));
    }

    @GetMapping("/books/new-launches")
    public ResponseEntity<BookDto.BookListResponse> newLaunches(
            @RequestParam(defaultValue = "12") int pageSize) {
        return ResponseEntity.ok(bookService.getNewLaunches(pageSize));
    }

    @GetMapping("/books/{bookId}")
    public ResponseEntity<BookDto.BookDetailDto> getBook(@PathVariable UUID bookId) {
        return ResponseEntity.ok(bookService.getBookDetail(bookId));
    }

    @GetMapping("/books/{bookId}/reviews")
    public ResponseEntity<ReviewDto.ReviewListResponse> listReviews(
            @PathVariable UUID bookId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ResponseEntity.ok(reviewService.listReviews(bookId, page, pageSize));
    }

    @PostMapping("/books/{bookId}/reviews")
    public ResponseEntity<ReviewDto.ReviewResponse> createReview(
            @PathVariable UUID bookId,
            @Valid @RequestBody ReviewDto.ReviewRequest req) {
        UUID userId = SecurityUtils.currentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.createReview(bookId, userId, req));
    }
}
