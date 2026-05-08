package com.mystore.app.controller;

import com.mystore.app.dto.ClientRequestDTO;
import com.mystore.app.dto.ClientResponseDTO;
import com.mystore.app.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService service;

    @GetMapping
    public Page<ClientResponseDTO> findAll(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String segment,
            @RequestParam(required = false) Integer regionId,
            @RequestParam(required = false) String regionName,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findAll(firstName, lastName, email, city, segment, regionId, regionName, pageable);
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
