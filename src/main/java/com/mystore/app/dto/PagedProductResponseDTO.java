package com.mystore.app.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagedProductResponseDTO {
    private List<ProductResponseDTO> products;
    private String nextCursor;
    private boolean hasMore;
    private int pageSize;
}