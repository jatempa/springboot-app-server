package com.mystore.app.service;

import com.mystore.app.dto.ProductRequestDTO;
import com.mystore.app.dto.ProductResponseDTO;
import com.mystore.app.dto.mapper.ProductMapper;
import com.mystore.app.entity.Category;
import com.mystore.app.entity.Product;
import com.mystore.app.repository.CategoryRepository;
import com.mystore.app.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
}