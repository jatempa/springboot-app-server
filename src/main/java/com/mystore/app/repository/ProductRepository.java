package com.mystore.app.repository;

import com.mystore.app.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    Optional<Product> findBySku(String sku);

    Page<Product> findAllByOrderByCreatedAtDescProductIdDesc(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE (p.createdAt < :createdAt OR (p.createdAt = :createdAt AND p.productId < :productId)) ORDER BY p.createdAt DESC, p.productId DESC")
    Page<Product> findByKeyset(@Param("createdAt") Instant createdAt, @Param("productId") Integer productId, Pageable pageable);
}