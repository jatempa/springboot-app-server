package com.mystore.app.controller;

import com.mystore.app.dto.ClientRequestDTO;
import com.mystore.app.dto.ClientResponseDTO;
import com.mystore.app.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService service;

    @GetMapping
    public List<ClientResponseDTO> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ClientResponseDTO findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @GetMapping("/email/{email}")
    public ClientResponseDTO findByEmail(@PathVariable String email) {
        return service.findByEmail(email);
    }

    @PostMapping
    public ResponseEntity<ClientResponseDTO> save(@RequestBody ClientRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(dto));
    }

    @PutMapping("/{id}")
    public ClientResponseDTO update(@PathVariable Integer id, @RequestBody ClientRequestDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}