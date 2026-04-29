package com.mystore.app.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {
    private String sku;
    private String productName;
    private Integer categoryId;
    private BigDecimal unitPrice;
    private BigDecimal costPrice;
    private Integer stockQty;
    private BigDecimal weightKg;
    private Boolean isActive;
}