package com.mystore.app.dto.mapper;

import com.mystore.app.dto.*;
import com.mystore.app.entity.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "categoryId", ignore = true)
    @Mapping(target = "parent", ignore = true)
    Category toEntity(CategoryRequestDTO dto);

    @Mapping(target = "parentId", source = "parent.categoryId")
    CategoryResponseDTO toResponse(Category entity);
}