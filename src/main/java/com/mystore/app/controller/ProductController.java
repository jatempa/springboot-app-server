package com.mystore.app.controller;

import com.mystore.app.dto.PagedProductResponseDTO;
import com.mystore.app.dto.ProductRequestDTO;
import com.mystore.app.dto.ProductResponseDTO;
import com.mystore.app.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    @GetMapping
    public PagedProductResponseDTO findAll(
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String cursor) {
        return service.findAllPaged(pageSize, cursor);
    }

    @GetMapping("/{id}")
    public ProductResponseDTO findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @GetMapping("/sku/{sku}")
    public ProductResponseDTO findBySku(@PathVariable String sku) {
        return service.findBySku(sku);
    }

    @PostMapping
    public ResponseEntity<ProductResponseDTO> save(@RequestBody ProductRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(dto));
    }

    @PutMapping("/{id}")
    public ProductResponseDTO update(@PathVariable Integer id, @RequestBody ProductRequestDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}