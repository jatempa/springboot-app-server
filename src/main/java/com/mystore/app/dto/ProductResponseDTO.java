package com.mystore.app.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {
    private Integer productId;
    private String sku;
    private String productName;
    private CategoryResponseDTO category;
    private BigDecimal unitPrice;
    private BigDecimal costPrice;
    private Integer stockQty;
    private BigDecimal weightKg;
    private Boolean isActive;
    private Instant createdAt;
}