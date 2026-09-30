package com.bookworm.repository;

import com.bookworm.entity.ReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, UUID> {

    Page<ReviewEntity> findByBookId(UUID bookId, Pageable pageable);

    boolean existsByBookIdAndUserId(UUID bookId, UUID userId);

    Optional<ReviewEntity> findByBookIdAndUserId(UUID bookId, UUID userId);
}
