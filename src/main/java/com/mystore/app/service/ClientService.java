package com.mystore.app.service;

import com.mystore.app.dto.ClientRequestDTO;
import com.mystore.app.dto.ClientResponseDTO;
import com.mystore.app.dto.mapper.ClientMapper;
import com.mystore.app.entity.Client;
import com.mystore.app.entity.ClientSegment;
import com.mystore.app.entity.Gender;
import com.mystore.app.entity.Region;
import com.mystore.app.repository.ClientRepository;
import com.mystore.app.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientService {

    private final ClientRepository repository;
    private final ClientMapper mapper;
    private final RegionRepository regionRepository;

    public Page<ClientResponseDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    public ClientResponseDTO findById(Integer id) {
        return repository.findById(id)
            .map(mapper::toResponse)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));
    }

    public ClientResponseDTO findByEmail(String email) {
        return repository.findByEmail(email)
            .map(mapper::toResponse)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));
    }

    public ClientResponseDTO save(ClientRequestDTO dto) {
        Client entity = mapper.toEntity(dto);
        if (dto.getRegionId() != null) {
            Region region = regionRepository.findById(dto.getRegionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Region not found"));
            entity.setRegion(region);
        }
        entity.setCreatedAt(Instant.now());
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    public ClientResponseDTO update(Integer id, ClientRequestDTO dto) {
        Client entity = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));

        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setBirthDate(dto.getBirthDate());
        entity.setCity(dto.getCity());
        entity.setAddress(dto.getAddress());
        entity.setLoyaltyPts(dto.getLoyaltyPts() != null ? dto.getLoyaltyPts() : entity.getLoyaltyPts());

        if (dto.getGender() != null) {
            entity.setGender(Gender.valueOf(dto.getGender()));
        }
        if (dto.getSegment() != null) {
            entity.setSegment(ClientSegment.valueOf(dto.getSegment()));
        }
        if (dto.getRegionId() != null) {
            Region region = regionRepository.findById(dto.getRegionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Region not found"));
            entity.setRegion(region);
        }

        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found");
        }
        repository.deleteById(id);
    }
}
