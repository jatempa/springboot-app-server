package com.mystore.app.controller;

import com.mystore.app.dto.RegionRequestDTO;
import com.mystore.app.dto.RegionResponseDTO;
import com.mystore.app.service.RegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/regions")
@RequiredArgsConstructor
public class RegionController {

    private final RegionService service;

    @GetMapping
    public List<RegionResponseDTO> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public RegionResponseDTO findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<RegionResponseDTO> save(@RequestBody RegionRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(dto));
    }

    @PutMapping("/{id}")
    public RegionResponseDTO update(@PathVariable Integer id, @RequestBody RegionRequestDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}