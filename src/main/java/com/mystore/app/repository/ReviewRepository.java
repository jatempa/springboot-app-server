package com.mystore.app.repository;

import com.mystore.app.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    @Query("SELECT r FROM Review r JOIN FETCH r.product JOIN FETCH r.client " +
           "ORDER BY r.reviewedAt DESC, r.reviewId DESC")
    Page<Review> findAllOrderByReviewedAtDescReviewIdDesc(Pageable pageable);

    @Query("SELECT r FROM Review r JOIN FETCH r.product JOIN FETCH r.client " +
           "WHERE (r.reviewedAt < :reviewedAt OR (r.reviewedAt = :reviewedAt AND r.reviewId < :reviewId)) " +
           "ORDER BY r.reviewedAt DESC, r.reviewId DESC")
    Page<Review> findByKeyset(@Param("reviewedAt") Instant reviewedAt, @Param("reviewId") Integer reviewId, Pageable pageable);

    @Query("SELECT r FROM Review r JOIN FETCH r.product JOIN FETCH r.client WHERE r.product.productId = :productId")
    List<Review> findByProduct_ProductId(@Param("productId") Integer productId);

    @Query("SELECT r FROM Review r JOIN FETCH r.product JOIN FETCH r.client WHERE r.client.clientId = :clientId")
    List<Review> findByClient_ClientId(@Param("clientId") Integer clientId);
}