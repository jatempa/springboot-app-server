package com.mystore.app.controller;

import com.mystore.app.dto.OrderItemRequestDTO;
import com.mystore.app.dto.OrderItemResponseDTO;
import com.mystore.app.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders/{orderId}/items")
@RequiredArgsConstructor
public class OrderItemController {

    private final OrderItemService service;

    @GetMapping
    public List<OrderItemResponseDTO> findByOrderId(@PathVariable Integer orderId) {
        return service.findByOrderId(orderId);
    }

    @PostMapping
    public ResponseEntity<OrderItemResponseDTO> save(@PathVariable Integer orderId, @RequestBody OrderItemRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(orderId, dto));
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer orderId, @PathVariable Integer productId) {
        service.delete(orderId, productId);
    }
}