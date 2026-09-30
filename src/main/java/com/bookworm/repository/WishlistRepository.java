package com.bookworm.repository;

import com.bookworm.entity.WishlistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WishlistRepository extends JpaRepository<WishlistEntity, UUID> {

    Optional<WishlistEntity> findByUserId(UUID userId);
}
