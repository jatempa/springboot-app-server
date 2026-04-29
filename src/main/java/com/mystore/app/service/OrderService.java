package com.mystore.app.service;

import com.mystore.app.dto.OrderRequestDTO;
import com.mystore.app.dto.OrderResponseDTO;
import com.mystore.app.dto.OrderItemRequestDTO;
import com.mystore.app.dto.mapper.OrderMapper;
import com.mystore.app.dto.mapper.OrderItemMapper;
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
public class OrderService {

    private final OrderRepository repository;
    private final OrderMapper mapper;
    private final OrderItemMapper orderItemMapper;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    public List<OrderResponseDTO> findAll() {
        return repository.findAll().stream()
            .map(mapper::toResponse)
            .toList();
    }

    public OrderResponseDTO findById(Integer id) {
        return repository.findById(id)
            .map(mapper::toResponse)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    public List<OrderResponseDTO> findByClientId(Integer clientId) {
        return repository.findByClient_ClientId(clientId).stream()
            .map(mapper::toResponse)
            .toList();
    }

    public OrderResponseDTO save(OrderRequestDTO dto) {
        Order entity = mapper.toEntity(dto);

        Client client = clientRepository.findById(dto.getClientId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Client not found"));
        entity.setClient(client);

        if (dto.getEmployeeId() != null) {
            Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee not found"));
            entity.setEmployee(employee);
        }

        entity.setOrderDate(Instant.now());
        entity = repository.save(entity);

        return mapper.toResponse(entity);
    }

    public OrderResponseDTO update(Integer id, OrderRequestDTO dto) {
        Order entity = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (dto.getShipDate() != null) {
            entity.setShipDate(dto.getShipDate());
        }
        if (dto.getStatus() != null) {
            entity.setStatus(OrderStatus.valueOf(dto.getStatus()));
        }
        if (dto.getChannel() != null) {
            entity.setChannel(Channel.valueOf(dto.getChannel()));
        }
        if (dto.getDiscountPct() != null) {
            entity.setDiscountPct(dto.getDiscountPct());
        }
        if (dto.getShippingCost() != null) {
            entity.setShippingCost(dto.getShippingCost());
        }
        if (dto.getNotes() != null) {
            entity.setNotes(dto.getNotes());
        }

        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    public OrderResponseDTO addItem(Integer orderId, OrderItemRequestDTO itemDto) {
        Order order = repository.findById(orderId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        Product product = productRepository.findById(itemDto.getProductId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found"));

        OrderItem item = new OrderItem();
        OrderItemId itemId = OrderItemId.of(orderId, itemDto.getProductId());
        item.setId(itemId);
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(itemDto.getQuantity());
        item.setUnitPrice(itemDto.getUnitPrice());
        item.setDiscountPct(itemDto.getDiscountPct());

        orderItemRepository.save(item);

        return mapper.toResponse(order);
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }
        repository.deleteById(id);
    }
}