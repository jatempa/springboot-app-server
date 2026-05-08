package com.mystore.app.service;

import com.mystore.app.dto.PagedProductResponseDTO;
import com.mystore.app.dto.ProductRequestDTO;
import com.mystore.app.dto.ProductResponseDTO;
import com.mystore.app.dto.mapper.ProductMapper;
import com.mystore.app.entity.Category;
import com.mystore.app.entity.Product;
import com.mystore.app.repository.CategoryRepository;
import com.mystore.app.repository.ProductRepository;
import com.mystore.app.repository.ProductSpecification;
import com.mystore.app.util.CursorUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
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

    @Cacheable(cacheNames = "products", key = "#pageSize + ':' + (#cursor ?: 'first') + ':' + (#sku ?: '') + ':' + (#productName ?: '') + ':' + (#categoryName ?: '') + ':' + (#sortBy ?: 'createdAt') + ':' + (#sortDir ?: 'desc')")
    public PagedProductResponseDTO findAllPaged(int pageSize, String cursor,
            String sku, String productName, String categoryName,
            String sortBy, String sortDir) {

        boolean useSpec = (sku != null && !sku.isBlank())
            || (productName != null && !productName.isBlank())
            || (categoryName != null && !categoryName.isBlank())
            || (sortBy != null && !sortBy.equals("createdAt"));

        if (!useSpec) {
            return findAllPagedDefault(pageSize, cursor);
        }

        String resolvedSortBy = (sortBy == null || sortBy.isBlank()) ? "createdAt" : sortBy;
        boolean ascending = "asc".equalsIgnoreCase(sortDir);

        String cursorValue = null;
        Integer cursorId = null;
        if (cursor != null && !cursor.isBlank()) {
            if ("createdAt".equals(resolvedSortBy)) {
                CursorUtils.CursorData data = CursorUtils.decodeCursor(cursor);
                cursorValue = data.timestamp().getEpochSecond() + ":" + data.timestamp().getNano();
                cursorId = data.id();
            } else {
                CursorUtils.StringCursorData data = CursorUtils.decodeStringCursor(cursor);
                cursorValue = data.value();
                cursorId = data.id();
            }
        }

        Specification<Product> spec = ProductSpecification.withFiltersAndCursor(
            sku, productName, categoryName, resolvedSortBy, ascending, cursorValue, cursorId);

        List<Product> products = new ArrayList<>(
            repository.findAll(spec, PageRequest.of(0, pageSize + 1)).getContent());

        boolean hasMore = products.size() > pageSize;
        if (hasMore) {
            products = products.subList(0, pageSize);
        }

        String nextCursor = null;
        if (hasMore && !products.isEmpty()) {
            Product last = products.get(products.size() - 1);
            if ("createdAt".equals(resolvedSortBy)) {
                nextCursor = CursorUtils.encodeCursor(last.getCreatedAt(), last.getProductId());
            } else {
                String fieldValue = switch (resolvedSortBy) {
                    case "productName" -> last.getProductName();
                    case "categoryName" -> last.getCategory().getCategoryName();
                    default -> last.getSku();
                };
                nextCursor = CursorUtils.encodeCursor(fieldValue, last.getProductId());
            }
        }

        return new PagedProductResponseDTO(
            products.stream().map(mapper::toResponse).toList(),
            nextCursor,
            hasMore,
            pageSize
        );
    }

    private PagedProductResponseDTO findAllPagedDefault(int pageSize, String cursor) {
        PageRequest pageRequest = PageRequest.of(0, pageSize + 1);

        Page<Product> page;
        if (cursor == null || cursor.isBlank()) {
            page = repository.findAllByOrderByCreatedAtDescProductIdDesc(pageRequest);
        } else {
            CursorUtils.CursorData cursorData = CursorUtils.decodeCursor(cursor);
            page = repository.findByKeyset(cursorData.timestamp(), cursorData.id(), pageRequest);
        }

        List<Product> products = page.getContent();
        boolean hasMore = products.size() > pageSize;
        if (hasMore) {
            products = products.subList(0, pageSize);
        }

        String nextCursor = null;
        if (hasMore && !products.isEmpty()) {
            Product last = products.get(products.size() - 1);
            nextCursor = CursorUtils.encodeCursor(last.getCreatedAt(), last.getProductId());
        }

        return new PagedProductResponseDTO(
            products.stream().map(mapper::toResponse).toList(),
            nextCursor,
            hasMore,
            pageSize
        );
    }

    @Cacheable(cacheNames = "product", key = "#id")
    public ProductResponseDTO findById(Integer id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    @Cacheable(cacheNames = "product", key = "'sku:' + #sku")
    public ProductResponseDTO findBySku(String sku) {
        return repository.findBySku(sku)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    @CacheEvict(cacheNames = "products", allEntries = true)
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

    @Caching(evict = {
        @CacheEvict(cacheNames = "product", allEntries = true),
        @CacheEvict(cacheNames = "products", allEntries = true)
    })
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

    @Caching(evict = {
        @CacheEvict(cacheNames = "product", allEntries = true),
        @CacheEvict(cacheNames = "products", allEntries = true)
    })
    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }

        repository.deleteById(id);
    }
}