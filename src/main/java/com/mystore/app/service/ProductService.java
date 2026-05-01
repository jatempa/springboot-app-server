package com.mystore.app.service;

import com.mystore.app.dto.PagedProductResponseDTO;
import com.mystore.app.dto.ProductRequestDTO;
import com.mystore.app.dto.ProductResponseDTO;
import com.mystore.app.dto.mapper.ProductMapper;
import com.mystore.app.entity.Category;
import com.mystore.app.entity.Product;
import com.mystore.app.repository.CategoryRepository;
import com.mystore.app.repository.ProductRepository;
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

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository repository;
    private final ProductMapper mapper;
    private final CategoryRepository categoryRepository;

    public List<ProductResponseDTO> findAll() {
        return repository.findAll().stream()
            .map(mapper::toResponse)
            .toList();
    }

    public PagedProductResponseDTO findAllPaged(int pageSize, String cursor) {
        PageRequest pageRequest = PageRequest.of(0, pageSize + 1);

        Page<Product> page;
        if (cursor == null || cursor.isBlank()) {
            page = repository.findAllByOrderByCreatedAtDescProductIdDesc(pageRequest);
        } else {
            CursorData cursorData = decodeCursor(cursor);
            page = repository.findByKeyset(cursorData.createdAt(), cursorData.productId(), pageRequest);
        }

        List<Product> products = page.getContent();
        boolean hasMore = products.size() > pageSize;

        if (hasMore) {
            products = products.subList(0, pageSize);
        }

        String nextCursor = null;
        if (hasMore && !products.isEmpty()) {
            Product lastProduct = products.get(products.size() - 1);
            nextCursor = encodeCursor(lastProduct.getCreatedAt(), lastProduct.getProductId());
        }

        return new PagedProductResponseDTO(
            products.stream().map(mapper::toResponse).toList(),
            nextCursor,
            hasMore,
            pageSize
        );
    }

    public ProductResponseDTO findById(Integer id) {
        return repository.findById(id)
            .map(mapper::toResponse)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    public ProductResponseDTO findBySku(String sku) {
        return repository.findBySku(sku)
            .map(mapper::toResponse)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    public ProductResponseDTO save(ProductRequestDTO dto) {
        Product entity = mapper.toEntity(dto);
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found"));
            entity.setCategory(category);
        }
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    public ProductResponseDTO update(Integer id, ProductRequestDTO dto) {
        Product entity = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        entity.setSku(dto.getSku());
        entity.setProductName(dto.getProductName());
        entity.setUnitPrice(dto.getUnitPrice());
        entity.setCostPrice(dto.getCostPrice());
        entity.setStockQty(dto.getStockQty());
        entity.setWeightKg(dto.getWeightKg());
        entity.setIsActive(dto.getIsActive());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found"));
            entity.setCategory(category);
        }

        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        repository.deleteById(id);
    }

    private String encodeCursor(Instant createdAt, Integer productId) {
        String raw = createdAt.getEpochSecond() + ":" + createdAt.getNano() + ":" + productId;
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
            int productId = Integer.parseInt(parts[2]);
            return new CursorData(Instant.ofEpochSecond(epochSecond, nano), productId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid cursor", e);
        }
    }

    private record CursorData(Instant createdAt, Integer productId) {}
}