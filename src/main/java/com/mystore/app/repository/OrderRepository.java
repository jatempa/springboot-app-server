package com.mystore.app.repository;

import com.mystore.app.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByClient_ClientId(Integer clientId);
    Optional<Order> findByClient_ClientIdAndOrderId(Integer clientId, Integer orderId);

    Page<Order> findAllByOrderByOrderDateDescOrderIdDesc(Pageable pageable);

    @Query("SELECT o FROM Order o WHERE (o.orderDate < :orderDate OR (o.orderDate = :orderDate AND o.orderId < :orderId)) ORDER BY o.orderDate DESC, o.orderId DESC")
    Page<Order> findByKeyset(@Param("orderDate") Instant orderDate, @Param("orderId") Integer orderId, Pageable pageable);

    // --- Paginated ID-only queries (no COUNT) ---

    @Query("SELECT o.orderId FROM Order o ORDER BY o.orderDate DESC, o.orderId DESC")
    List<Integer> findOrderIdsPaged(Pageable pageable);

    @Query("SELECT o.orderId FROM Order o WHERE (o.orderDate < :orderDate OR (o.orderDate = :orderDate AND o.orderId < :orderId)) ORDER BY o.orderDate DESC, o.orderId DESC")
    List<Integer> findOrderIdsByKeyset(@Param("orderDate") Instant orderDate, @Param("orderId") Integer orderId, Pageable pageable);

    // --- JOIN FETCH queries that populate the full graph via Hibernate L1 cache ---

    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.client c LEFT JOIN FETCH c.region LEFT JOIN FETCH o.employee e LEFT JOIN FETCH e.region WHERE o.orderId IN :ids")
    List<Order> findWithToOnesByIds(@Param("ids") List<Integer> ids);

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product p LEFT JOIN FETCH p.category WHERE o.orderId IN :ids")
    List<Order> findWithItemsByIds(@Param("ids") List<Integer> ids);

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.payments WHERE o.orderId IN :ids")
    List<Order> findWithPaymentsByIds(@Param("ids") List<Integer> ids);
}