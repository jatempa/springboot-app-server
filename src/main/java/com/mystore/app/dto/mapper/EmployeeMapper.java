package com.mystore.app.dto.mapper;

import com.mystore.app.dto.*;
import com.mystore.app.entity.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {RegionMapper.class})
public interface EmployeeMapper {

    @Mapping(target = "employeeId", ignore = true)
    @Mapping(target = "region", ignore = true)
    @Mapping(target = "manager", ignore = true)
    @Mapping(source = "role", target = "role", qualifiedByName = "stringToRole")
    Employee toEntity(EmployeeRequestDTO dto);

    @Mapping(source = "region", target = "region")
    @Mapping(source = "manager.employeeId", target = "managerId")
    EmployeeResponseDTO toResponse(Employee entity);

    @Named("stringToRole")
    default Role stringToRole(String value) {
        return value != null ? Role.valueOf(value) : null;
    }
}