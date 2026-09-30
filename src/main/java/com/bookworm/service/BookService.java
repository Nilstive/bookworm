package com.bookworm.service;

import com.bookworm.dto.BookDto;
import com.bookworm.entity.BookEntity;
import com.bookworm.exception.ResourceNotFoundException;
import com.bookworm.repository.BookRepository;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public BookDto.BookListResponse searchBooks(BookDto.BookSearchParams params) {
        Pageable pageable = buildPageable(params.page(), params.pageSize(), params.sortBy());
        Specification<BookEntity> spec = buildSpec(params);
        Page<BookEntity> page = bookRepository.findAll(spec, pageable);
        return toListResponse(page);
    }

    public BookDto.BookListResponse getRecommended(int pageSize) {
        // Simple fallback: return top-rated books; personalisation hooks in here
        Pageable pageable = PageRequest.of(0, pageSize, Sort.by("rating").descending());
        Page<BookEntity> page = bookRepository.findAll(pageable);
        return toListResponse(page);
    }

    public BookDto.BookListResponse getBestsellers(int pageSize) {
        Pageable pageable = PageRequest.of(0, pageSize, Sort.by("totalSales").descending());
        Page<BookEntity> page = bookRepository.findByIsBestsellerTrue(pageable);
        return toListResponse(page);
    }

    public BookDto.BookListResponse getNewLaunches(int pageSize) {
        Pageable pageable = PageRequest.of(0, pageSize, Sort.by("createdAt").descending());
        Page<BookEntity> page = bookRepository.findByIsNewLaunchTrue(pageable);
        return toListResponse(page);
    }

    public BookDto.BookDetailDto getBookDetail(UUID bookId) {
        BookEntity book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + bookId));

        List<BookDto.BookSummaryDto> related = bookRepository
                .findByAuthorId(book.getAuthor().getId(),
                        PageRequest.of(0, 5))
                .stream()
                .filter(b -> !b.getId().equals(bookId))
                .map(this::toSummary)
                .toList();

        return new BookDto.BookDetailDto(
                book.getId(), book.getTitle(), toAuthorRef(book),
                book.getCoverImageUrl(), book.getFormat().name(),
                book.getCategories(), book.getPrice(), book.getCurrency(),
                book.getEstimatedDelivery(), book.getRating(),
                book.getDescription(), book.getPublisherName(),
                book.getPublishedDate() != null ? book.getPublishedDate().toString() : null,
                book.getLanguage(), book.getIsbn(),
                book.getTotalSales(), book.getTotalReviews(),
                book.getAuthor().getBio(), related
        );
    }

    public BookDto.BookListResponse getBooksByAuthor(UUID authorId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<BookEntity> result = bookRepository.findByAuthorId(authorId, pageable);
        return toListResponse(result);
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private Specification<BookEntity> buildSpec(BookDto.BookSearchParams p) {
        return (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (p.q() != null && !p.q().isBlank()) {
                String like = "%" + p.q().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.join("author").get("name")), like)
                ));
            }
            if (p.category() != null && !p.category().isBlank()) {
                predicates.add(cb.isMember(p.category(), root.get("categories")));
            }
            if (p.language() != null && !p.language().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("language")), p.language().toLowerCase()));
            }
            if (p.format() != null && !p.format().isBlank()) {
                predicates.add(cb.equal(root.get("format"),
                        BookEntity.BookFormat.valueOf(p.format().toUpperCase())));
            }
            if (p.priceMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), p.priceMin()));
            }
            if (p.priceMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), p.priceMax()));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private Pageable buildPageable(int page, int pageSize, String sortBy) {
        Sort sort = switch (sortBy == null ? "relevance" : sortBy) {
            case "price_asc"  -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "rating"     -> Sort.by("rating").descending();
            case "newest"     -> Sort.by("createdAt").descending();
            default           -> Sort.by("title").ascending();
        };
        return PageRequest.of(Math.max(page - 1, 0), pageSize, sort);
    }

    private BookDto.BookListResponse toListResponse(Page<BookEntity> page) {
        List<BookDto.BookSummaryDto> data = page.getContent().stream()
                .map(this::toSummary).toList();
        var pagination = new com.bookworm.dto.PaginationDto(
                page.getNumber() + 1, page.getSize(),
                page.getTotalElements(), page.getTotalPages());
        return new BookDto.BookListResponse(data, pagination);
    }

    public BookDto.BookSummaryDto toSummary(BookEntity b) {
        return new BookDto.BookSummaryDto(
                b.getId(), b.getTitle(), toAuthorRef(b),
                b.getCoverImageUrl(), b.getFormat().name(),
                b.getCategories(), b.getPrice(), b.getCurrency(),
                b.getEstimatedDelivery(), b.getRating());
    }

    private BookDto.AuthorRefDto toAuthorRef(BookEntity b) {
        return new BookDto.AuthorRefDto(b.getAuthor().getId(), b.getAuthor().getName());
    }
}
