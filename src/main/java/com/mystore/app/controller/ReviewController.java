package com.mystore.app.controller;

import com.mystore.app.dto.ReviewRequestDTO;
import com.mystore.app.dto.ReviewResponseDTO;
import com.mystore.app.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService service;

    @GetMapping
    public List<ReviewResponseDTO> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ReviewResponseDTO findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @GetMapping("/product/{productId}")
    public List<ReviewResponseDTO> findByProductId(@PathVariable Integer productId) {
        return service.findByProductId(productId);
    }

    @GetMapping("/client/{clientId}")
    public List<ReviewResponseDTO> findByClientId(@PathVariable Integer clientId) {
        return service.findByClientId(clientId);
    }

    @PostMapping
    public ResponseEntity<ReviewResponseDTO> save(@RequestBody ReviewRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(dto));
    }

    @PutMapping("/{id}")
    public ReviewResponseDTO update(@PathVariable Integer id, @RequestBody ReviewRequestDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}