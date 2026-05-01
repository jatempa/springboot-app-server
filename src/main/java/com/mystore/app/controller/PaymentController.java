package com.mystore.app.controller;

import com.mystore.app.dto.PagedPaymentResponseDTO;
import com.mystore.app.dto.PaymentRequestDTO;
import com.mystore.app.dto.PaymentResponseDTO;
import com.mystore.app.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService service;

    @GetMapping
    public PagedPaymentResponseDTO findAll(
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String cursor) {
        return service.findAllPaged(pageSize, cursor);
    }

    @GetMapping("/{id}")
    public PaymentResponseDTO findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @GetMapping("/order/{orderId}")
    public List<PaymentResponseDTO> findByOrderId(@PathVariable Integer orderId) {
        return service.findByOrderId(orderId);
    }

    @PostMapping
    public ResponseEntity<PaymentResponseDTO> save(@RequestBody PaymentRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(dto));
    }

    @PutMapping("/{id}")
    public PaymentResponseDTO update(@PathVariable Integer id, @RequestBody PaymentRequestDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}