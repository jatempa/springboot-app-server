package com.mystore.app.service;

import com.mystore.app.dto.OrderItemRequestDTO;
import com.mystore.app.dto.OrderItemResponseDTO;
import com.mystore.app.dto.mapper.OrderItemMapper;
import com.mystore.app.entity.*;
import com.mystore.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderItemService {

    private final OrderItemRepository repository;
    private final OrderItemMapper mapper;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public List<OrderItemResponseDTO> findByOrderId(Integer orderId) {
        return repository.findById_OrderId(orderId).stream()
            .map(mapper::toResponse)
            .toList();
    }

    public OrderItemResponseDTO save(Integer orderId, OrderItemRequestDTO dto) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        Product product = productRepository.findById(dto.getProductId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        OrderItem item = mapper.toEntity(dto);
        item.setId(OrderItemId.of(orderId, dto.getProductId()));
        item.setOrder(order);
        item.setProduct(product);

        item = repository.save(item);
        return mapper.toResponse(item);
    }

    public void delete(Integer orderId, Integer productId) {
        OrderItemId id = OrderItemId.of(orderId, productId);
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "OrderItem not found");
        }
        repository.deleteById(id);
    }
}