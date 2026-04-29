package com.mystore.app.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponseDTO {
    private Integer orderId;
    private Integer productId;
    private ProductResponseDTO product;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountPct;
}