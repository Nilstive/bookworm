package com.bookworm.service;

import com.bookworm.dto.PaginationDto;
import com.bookworm.dto.ReviewDto;
import com.bookworm.entity.BookEntity;
import com.bookworm.entity.ReviewEntity;
import com.bookworm.entity.UserEntity;
import com.bookworm.exception.ConflictException;
import com.bookworm.exception.ResourceNotFoundException;
import com.bookworm.repository.BookRepository;
import com.bookworm.repository.ReviewRepository;
import com.bookworm.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         BookRepository bookRepository,
                         UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    public ReviewDto.ReviewListResponse listReviews(UUID bookId, int page, int pageSize) {
        if (!bookRepository.existsById(bookId)) {
            throw new ResourceNotFoundException("Book not found: " + bookId);
        }
        Page<ReviewEntity> result = reviewRepository.findByBookId(
                bookId, PageRequest.of(page - 1, pageSize, Sort.by("createdAt").descending()));

        var data = result.getContent().stream().map(this::toResponse).toList();
        var pagination = new PaginationDto(result.getNumber() + 1, result.getSize(),
                result.getTotalElements(), result.getTotalPages());
        return new ReviewDto.ReviewListResponse(data, pagination);
    }

    @Transactional
    public ReviewDto.ReviewResponse createReview(UUID bookId, UUID userId,
                                                  ReviewDto.ReviewRequest req) {
        if (reviewRepository.existsByBookIdAndUserId(bookId, userId)) {
            throw new ConflictException("You have already reviewed this book");
        }
        BookEntity book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + bookId));
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        ReviewEntity review = ReviewEntity.builder()
                .book(book).user(user)
                .rating((short) req.rating()).body(req.body())
                .build();
        return toResponse(reviewRepository.save(review));
    }

    private ReviewDto.ReviewResponse toResponse(ReviewEntity r) {
        return new ReviewDto.ReviewResponse(
                r.getId(), r.getBook().getId(), r.getUser().getId(),
                r.getUser().getFirstName() + " " + r.getUser().getLastName(),
                r.getRating(), r.getBody(), r.getCreatedAt());
    }
}
