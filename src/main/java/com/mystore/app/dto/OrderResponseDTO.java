package com.mystore.app.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {
    private Integer orderId;
    private ClientResponseDTO client;
    private EmployeeResponseDTO employee;
    private Instant orderDate;
    private Instant shipDate;
    private String status;
    private String channel;
    private BigDecimal discountPct;
    private BigDecimal shippingCost;
    private String notes;
    private List<OrderItemResponseDTO> items;
    private List<PaymentResponseDTO> payments;
}