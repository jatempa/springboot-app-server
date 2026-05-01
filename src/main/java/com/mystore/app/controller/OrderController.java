package com.mystore.app.controller;

import com.mystore.app.dto.OrderRequestDTO;
import com.mystore.app.dto.OrderResponseDTO;
import com.mystore.app.dto.OrderItemRequestDTO;
import com.mystore.app.dto.PagedOrderResponseDTO;
import com.mystore.app.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

private final OrderService service;

@GetMapping
public PagedOrderResponseDTO findAll(
        @RequestParam(defaultValue = "20") int pageSize,
        @RequestParam(required = false) String cursor) {
    return service.findAllPaged(pageSize, cursor);
}

@GetMapping("/{id}")
public OrderResponseDTO findById(@PathVariable Integer id) {
    return service.findById(id);
}

    @GetMapping("/client/{clientId}")
    public List<OrderResponseDTO> findByClientId(@PathVariable Integer clientId) {
        return service.findByClientId(clientId);
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> save(@RequestBody OrderRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(dto));
    }

    @PutMapping("/{id}")
    public OrderResponseDTO update(@PathVariable Integer id, @RequestBody OrderRequestDTO dto) {
        return service.update(id, dto);
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<OrderResponseDTO> addItem(@PathVariable Integer id, @RequestBody OrderItemRequestDTO itemDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addItem(id, itemDto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}