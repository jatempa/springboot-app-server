package com.mystore.app.controller;

import com.mystore.app.dto.CategoryRequestDTO;
import com.mystore.app.dto.CategoryResponseDTO;
import com.mystore.app.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService service;

    @GetMapping
    public List<CategoryResponseDTO> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public CategoryResponseDTO findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> save(@RequestBody CategoryRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(dto));
    }

    @PutMapping("/{id}")
    public CategoryResponseDTO update(@PathVariable Integer id, @RequestBody CategoryRequestDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}