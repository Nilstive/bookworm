package com.bookworm.repository;

import com.bookworm.entity.BookEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<BookEntity, UUID>,
        JpaSpecificationExecutor<BookEntity> {

    Page<BookEntity> findByIsBestsellerTrue(Pageable pageable);

    Page<BookEntity> findByIsNewLaunchTrue(Pageable pageable);

    Page<BookEntity> findByAuthorId(UUID authorId, Pageable pageable);
}
