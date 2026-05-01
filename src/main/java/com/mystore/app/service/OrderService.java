package com.mystore.app.service;

import com.mystore.app.dto.OrderRequestDTO;
import com.mystore.app.dto.OrderResponseDTO;
import com.mystore.app.dto.PagedOrderResponseDTO;
import com.mystore.app.dto.OrderItemRequestDTO;
import com.mystore.app.dto.mapper.OrderMapper;
import com.mystore.app.dto.mapper.OrderItemMapper;
import com.mystore.app.entity.*;
import com.mystore.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

public PagedOrderResponseDTO findAllPaged(int pageSize, String cursor) {
    PageRequest pageRequest = PageRequest.of(0, pageSize + 1);

    List<Integer> ids;
    if (cursor == null || cursor.isBlank()) {
        ids = repository.findOrderIdsPaged(pageRequest);
    } else {
        CursorData cursorData = decodeCursor(cursor);
        ids = repository.findOrderIdsByKeyset(cursorData.orderDate(), cursorData.orderId(), pageRequest);
    }

    boolean hasMore = ids.size() > pageSize;
    if (hasMore) {
        ids = ids.subList(0, pageSize);
    }

    String nextCursor = null;
    List<OrderResponseDTO> dtos = List.of();

    if (!ids.isEmpty()) {
        Map<Integer, Order> orderMap = repository.findWithToOnesByIds(ids).stream()
                .collect(Collectors.toMap(Order::getOrderId, o -> o));
        repository.findWithItemsByIds(ids);
        repository.findWithPaymentsByIds(ids);

        List<Order> orders = ids.stream().map(orderMap::get).toList();

        if (hasMore) {
            Order last = orders.get(orders.size() - 1);
            nextCursor = encodeCursor(last.getOrderDate(), last.getOrderId());
        }

        dtos = orders.stream().map(mapper::toResponse).toList();
    }

    return new PagedOrderResponseDTO(dtos, nextCursor, hasMore, pageSize);
}

private String encodeCursor(Instant orderDate, Integer orderId) {
    String raw = orderDate.getEpochSecond() + ":" + orderDate.getNano() + ":" + orderId;
    return Base64.getUrlEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
}

private CursorData decodeCursor(String cursor) {
    try {
        String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
        String[] parts = raw.split(":");
        if (parts.length != 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid cursor format");
        }
        long epochSecond = Long.parseLong(parts[0]);
        int nano = Integer.parseInt(parts[1]);
        int orderId = Integer.parseInt(parts[2]);
        return new CursorData(Instant.ofEpochSecond(epochSecond, nano), orderId);
    } catch (Exception e) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid cursor", e);
    }
}

private record CursorData(Instant orderDate, Integer orderId) {}
}