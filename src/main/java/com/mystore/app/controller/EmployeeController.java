package com.mystore.app.controller;

import com.mystore.app.dto.EmployeeRequestDTO;
import com.mystore.app.dto.EmployeeResponseDTO;
import com.mystore.app.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService service;

    @GetMapping
    public List<EmployeeResponseDTO> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public EmployeeResponseDTO findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @GetMapping("/manager/{managerId}")
    public List<EmployeeResponseDTO> findByManagerId(@PathVariable Integer managerId) {
        return service.findByManagerId(managerId);
    }

    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> save(@RequestBody EmployeeRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(dto));
    }

    @PutMapping("/{id}")
    public EmployeeResponseDTO update(@PathVariable Integer id, @RequestBody EmployeeRequestDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}