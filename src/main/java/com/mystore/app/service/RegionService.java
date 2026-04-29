package com.mystore.app.service;

import com.mystore.app.dto.RegionRequestDTO;
import com.mystore.app.dto.RegionResponseDTO;
import com.mystore.app.dto.mapper.RegionMapper;
import com.mystore.app.entity.Region;
import com.mystore.app.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RegionService {

    private final RegionRepository repository;
    private final RegionMapper mapper;

    public List<RegionResponseDTO> findAll() {
        return repository.findAll().stream()
            .map(mapper::toResponse)
            .toList();
    }

    public RegionResponseDTO findById(Integer id) {
        return repository.findById(id)
            .map(mapper::toResponse)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Region not found"));
    }

    public RegionResponseDTO save(RegionRequestDTO dto) {
        Region entity = mapper.toEntity(dto);
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    public RegionResponseDTO update(Integer id, RegionRequestDTO dto) {
        Region entity = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Region not found"));

        entity.setRegionName(dto.getRegionName());
        entity.setCountry(dto.getCountry());
        entity.setTimezone(dto.getTimezone());

        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Region not found");
        }
        repository.deleteById(id);
    }
}