package com.mystore.app.service;

import com.mystore.app.dto.CategoryRequestDTO;
import com.mystore.app.dto.CategoryResponseDTO;
import com.mystore.app.dto.mapper.CategoryMapper;
import com.mystore.app.entity.Category;
import com.mystore.app.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.data.domain.Sort;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository repository;
    private final CategoryMapper mapper;

    public List<CategoryResponseDTO> findAll(String name, String sortDir) {
        Sort sort = "desc".equalsIgnoreCase(sortDir)
            ? Sort.by("categoryName").descending()
            : Sort.by("categoryName").ascending();

        List<Category> results = (name != null && !name.isBlank())
            ? repository.findByCategoryNameContainingIgnoreCase(name, sort)
            : repository.findAll(sort);

        return results.stream()
            .map(mapper::toResponse)
            .toList();
    }

    public CategoryResponseDTO findById(Integer id) {
        return repository.findById(id)
            .map(mapper::toResponse)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
    }

    public CategoryResponseDTO save(CategoryRequestDTO dto) {
        Category entity = mapper.toEntity(dto);
        if (dto.getParentId() != null) {
            Category parent = repository.findById(dto.getParentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent category not found"));
            entity.setParent(parent);
        }
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    public CategoryResponseDTO update(Integer id, CategoryRequestDTO dto) {
        Category entity = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        entity.setCategoryName(dto.getCategoryName());
        entity.setDescription(dto.getDescription());

        if (dto.getParentId() != null) {
            Category parent = repository.findById(dto.getParentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent category not found"));
            entity.setParent(parent);
        } else {
            entity.setParent(null);
        }

        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found");
        }
        repository.deleteById(id);
    }
}