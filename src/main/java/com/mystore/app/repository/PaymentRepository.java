package com.mystore.app.repository;

import com.mystore.app.entity.Payment;
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
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    @Query("SELECT p FROM Payment p JOIN FETCH p.order")
    List<Payment> findAllWithOrder();

    @Query(value = "SELECT p FROM Payment p JOIN FETCH p.order ORDER BY p.paidAt DESC, p.paymentId DESC",
           countQuery = "SELECT COUNT(p) FROM Payment p")
    Page<Payment> findAllOrderByPaidAtDescPaymentIdDesc(Pageable pageable);

    @Query(value = "SELECT p FROM Payment p JOIN FETCH p.order " +
                   "WHERE (p.paidAt < :paidAt OR (p.paidAt = :paidAt AND p.paymentId < :paymentId)) " +
                   "ORDER BY p.paidAt DESC, p.paymentId DESC",
           countQuery = "SELECT COUNT(p) FROM Payment p " +
                        "WHERE (p.paidAt < :paidAt OR (p.paidAt = :paidAt AND p.paymentId < :paymentId))")
    Page<Payment> findByKeyset(@Param("paidAt") Instant paidAt, @Param("paymentId") Integer paymentId, Pageable pageable);

    @Query("SELECT p FROM Payment p JOIN FETCH p.order WHERE p.order.orderId = :orderId")
    List<Payment> findByOrderIdWithOrder(@Param("orderId") Integer orderId);

    @Query("SELECT p FROM Payment p JOIN FETCH p.order WHERE p.paymentId = :id")
    Optional<Payment> findByIdWithOrder(@Param("id") Integer id);
}
