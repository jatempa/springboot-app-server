package com.mystore.app.service;

import com.mystore.app.dto.EmployeeRequestDTO;
import com.mystore.app.dto.EmployeeResponseDTO;
import com.mystore.app.dto.mapper.EmployeeMapper;
import com.mystore.app.entity.Employee;
import com.mystore.app.entity.Region;
import com.mystore.app.entity.Role;
import com.mystore.app.repository.EmployeeRepository;
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
public class EmployeeService {

    private final EmployeeRepository repository;
    private final EmployeeMapper mapper;
    private final RegionRepository regionRepository;

    public List<EmployeeResponseDTO> findAll() {
        return repository.findAll().stream()
            .map(mapper::toResponse)
            .toList();
    }

    public EmployeeResponseDTO findById(Integer id) {
        return repository.findById(id)
            .map(mapper::toResponse)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
    }

    public List<EmployeeResponseDTO> findByManagerId(Integer managerId) {
        return repository.findByManager_EmployeeId(managerId).stream()
            .map(mapper::toResponse)
            .toList();
    }

    public EmployeeResponseDTO save(EmployeeRequestDTO dto) {
        Employee entity = mapper.toEntity(dto);
        if (dto.getRegionId() != null) {
            Region region = regionRepository.findById(dto.getRegionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Region not found"));
            entity.setRegion(region);
        }
        if (dto.getManagerId() != null) {
            Employee manager = repository.findById(dto.getManagerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Manager not found"));
            entity.setManager(manager);
        }
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    public EmployeeResponseDTO update(Integer id, EmployeeRequestDTO dto) {
        Employee entity = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setEmail(dto.getEmail());
        entity.setHireDate(dto.getHireDate());
        entity.setSalary(dto.getSalary());

        if (dto.getRole() != null) {
            entity.setRole(Role.valueOf(dto.getRole()));
        }
        if (dto.getRegionId() != null) {
            Region region = regionRepository.findById(dto.getRegionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Region not found"));
            entity.setRegion(region);
        }
        if (dto.getManagerId() != null) {
            Employee manager = repository.findById(dto.getManagerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Manager not found"));
            entity.setManager(manager);
        }

        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
        }
        repository.deleteById(id);
    }
}