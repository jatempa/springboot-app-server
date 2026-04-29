package com.mystore.app.dto.mapper;

import com.mystore.app.dto.*;
import com.mystore.app.entity.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {RegionMapper.class})
public interface ClientMapper {

    @Mapping(target = "clientId", ignore = true)
    @Mapping(target = "region", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(source = "gender", target = "gender", qualifiedByName = "stringToGender")
    @Mapping(source = "segment", target = "segment", qualifiedByName = "stringToSegment")
    Client toEntity(ClientRequestDTO dto);

    @Mapping(source = "region", target = "region")
    ClientResponseDTO toResponse(Client entity);

    @Named("stringToGender")
    default Gender stringToGender(String value) {
        return value != null ? Gender.valueOf(value) : null;
    }

    @Named("stringToSegment")
    default ClientSegment stringToSegment(String value) {
        return value != null ? ClientSegment.valueOf(value) : null;
    }
}