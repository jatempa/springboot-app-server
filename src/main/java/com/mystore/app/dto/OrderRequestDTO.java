package com.mystore.app.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDTO {
    private Integer clientId;
    private Integer employeeId;
    private Instant shipDate;
    private String status;
    private String channel;
    private BigDecimal discountPct;
    private BigDecimal shippingCost;
    private String notes;
}