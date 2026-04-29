package com.mystore.app.repository;

import com.mystore.app.entity.OrderItem;
import com.mystore.app.entity.OrderItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemId> {
    List<OrderItem> findById_OrderId(Integer orderId);
}