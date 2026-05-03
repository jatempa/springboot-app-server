package com.mystore.app.controller;

import com.mystore.app.dto.OrderRequestDTO;
import com.mystore.app.dto.OrderResponseDTO;
import com.mystore.app.dto.OrderItemRequestDTO;
import com.mystore.app.dto.PagedOrderResponseDTO;
import com.mystore.app.messaging.OrderReportPublisher;
import com.mystore.app.service.OrderReportService;
import com.mystore.app.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

private final OrderService service;
private final OrderReportService orderReportService;
private final OrderReportPublisher orderReportPublisher;

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
        var savedOrder = service.save(dto);
        orderReportPublisher.publishOrderReport(savedOrder.getOrderId());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedOrder);
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
    

    @PostMapping("/{id}/report")
    public ResponseEntity<Map<String, String>> enqueueReport(@PathVariable Integer id) {
        service.findById(id); // validates the order exists before enqueuing
        orderReportPublisher.publishOrderReport(id);
        return ResponseEntity.accepted()
                .body(Map.of(
                        "message", "Report generation queued for order " + id,
                        "statusUrl", "/api/orders/" + id + "/report"
                ));
    }

    @GetMapping("/{id}/report")
    public ResponseEntity<?> downloadReport(@PathVariable Integer id) {
        Path reportPath = orderReportService.resolveReportPath(id);
        if (!Files.exists(reportPath)) {
            return ResponseEntity.accepted()
                    .body(Map.of("message", "Report is being generated, please try again shortly"));
        }
        Resource resource = new FileSystemResource(reportPath);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"order-" + id + ".pdf\"")
                .body(resource);
    }
}