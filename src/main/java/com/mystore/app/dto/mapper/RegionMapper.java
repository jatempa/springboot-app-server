package com.mystore.app.dto.mapper;

import com.mystore.app.dto.*;
import com.mystore.app.entity.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface RegionMapper {

    @Mapping(target = "regionId", ignore = true)
    Region toEntity(RegionRequestDTO dto);

    RegionResponseDTO toResponse(Region entity);
}