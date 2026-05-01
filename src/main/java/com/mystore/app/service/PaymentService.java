package com.mystore.app.service;

import com.mystore.app.dto.PagedPaymentResponseDTO;
import com.mystore.app.dto.PaymentRequestDTO;
import com.mystore.app.dto.PaymentResponseDTO;
import com.mystore.app.dto.mapper.PaymentMapper;
import com.mystore.app.entity.*;
import com.mystore.app.repository.*;
import com.mystore.app.util.CursorUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
        return repository.findAllWithOrder().stream()
            .map(mapper::toResponse)
            .toList();
    }

    public PagedPaymentResponseDTO findAllPaged(int pageSize, String cursor) {
        PageRequest pageRequest = PageRequest.of(0, pageSize + 1);

        Page<Payment> page;
        if (cursor == null || cursor.isBlank()) {
            page = repository.findAllOrderByPaidAtDescPaymentIdDesc(pageRequest);
        } else {
            CursorUtils.CursorData cursorData = CursorUtils.decodeCursor(cursor);
            page = repository.findByKeyset(cursorData.timestamp(), cursorData.id(), pageRequest);
        }

        List<Payment> payments = page.getContent();
        boolean hasMore = payments.size() > pageSize;

        if (hasMore) {
            payments = payments.subList(0, pageSize);
        }

        String nextCursor = null;
        if (hasMore && !payments.isEmpty()) {
            Payment last = payments.get(payments.size() - 1);
            nextCursor = CursorUtils.encodeCursor(last.getPaidAt(), last.getPaymentId());
        }

        return new PagedPaymentResponseDTO(
            payments.stream().map(mapper::toResponse).toList(),
            nextCursor,
            hasMore,
            pageSize
        );
    }

    public PaymentResponseDTO findById(Integer id) {
        return repository.findByIdWithOrder(id)
            .map(mapper::toResponse)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
    }

    public List<PaymentResponseDTO> findByOrderId(Integer orderId) {
        return repository.findByOrderIdWithOrder(orderId).stream()
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
        Payment entity = repository.findByIdWithOrder(id)
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
