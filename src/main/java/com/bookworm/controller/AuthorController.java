package com.bookworm.controller;

import com.bookworm.dto.BookDto;
import com.bookworm.entity.AuthorEntity;
import com.bookworm.exception.ResourceNotFoundException;
import com.bookworm.repository.AuthorRepository;
import com.bookworm.service.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/authors")
public class AuthorController {

    private final AuthorRepository authorRepository;
    private final BookService bookService;

    public AuthorController(AuthorRepository authorRepository, BookService bookService) {
        this.authorRepository = authorRepository;
        this.bookService = bookService;
    }

    @GetMapping("/{authorId}")
    public ResponseEntity<?> getAuthor(@PathVariable UUID authorId) {
        AuthorEntity author = authorRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found: " + authorId));
        var dto = new java.util.LinkedHashMap<String, Object>();
        dto.put("authorId", author.getId());
        dto.put("name", author.getName());
        dto.put("bio", author.getBio());
        dto.put("photoUrl", author.getPhotoUrl());
        dto.put("totalBooks", author.getBooks().size());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{authorId}/books")
    public ResponseEntity<BookDto.BookListResponse> getBooksByAuthor(
            @PathVariable UUID authorId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ResponseEntity.ok(bookService.getBooksByAuthor(authorId, page, pageSize));
    }
}
