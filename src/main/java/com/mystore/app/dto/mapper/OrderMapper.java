package com.mystore.app.dto.mapper;

import com.mystore.app.dto.*;
import com.mystore.app.entity.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {ClientMapper.class, EmployeeMapper.class, OrderItemMapper.class, PaymentMapper.class})
public interface OrderMapper {

    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "orderDate", ignore = true)
    @Mapping(source = "status", target = "status", qualifiedByName = "stringToStatus")
    @Mapping(source = "channel", target = "channel", qualifiedByName = "stringToChannel")
    Order toEntity(OrderRequestDTO dto);

    @Mapping(source = "client", target = "client")
    @Mapping(source = "employee", target = "employee")
    OrderResponseDTO toResponse(Order entity);

    @Named("stringToStatus")
    default OrderStatus stringToStatus(String value) {
        return value != null ? OrderStatus.valueOf(value) : null;
    }

    @Named("stringToChannel")
    default Channel stringToChannel(String value) {
        return value != null ? Channel.valueOf(value) : null;
    }
}