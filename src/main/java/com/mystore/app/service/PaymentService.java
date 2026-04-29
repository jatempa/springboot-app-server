package com.mystore.app.service;

import com.mystore.app.dto.PaymentRequestDTO;
import com.mystore.app.dto.PaymentResponseDTO;
import com.mystore.app.dto.mapper.PaymentMapper;
import com.mystore.app.entity.*;
import com.mystore.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository repository;
    private final PaymentMapper mapper;
    private final OrderRepository orderRepository;

    public List<PaymentResponseDTO> findAll() {
        return repository.findAll().stream()
            .map(mapper::toResponse)
            .toList();
    }

    public PaymentResponseDTO findById(Integer id) {
        return repository.findById(id)
            .map(mapper::toResponse)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
    }

    public List<PaymentResponseDTO> findByOrderId(Integer orderId) {
        return repository.findByOrder_OrderId(orderId).stream()
            .map(mapper::toResponse)
            .toList();
    }

    public PaymentResponseDTO save(PaymentRequestDTO dto) {
        Order order = orderRepository.findById(dto.getOrderId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order not found"));

        Payment entity = mapper.toEntity(dto);
        entity.setOrder(order);
        entity.setPaidAt(Instant.now());

        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    public PaymentResponseDTO update(Integer id, PaymentRequestDTO dto) {
        Payment entity = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));

        entity.setAmount(dto.getAmount());

        if (dto.getMethod() != null) {
            entity.setMethod(PaymentMethod.valueOf(dto.getMethod()));
        }
        if (dto.getStatus() != null) {
            entity.setStatus(PaymentStatus.valueOf(dto.getStatus()));
        }

        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found");
        }
        repository.deleteById(id);
    }
}