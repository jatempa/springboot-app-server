package com.mystore.app.dto.mapper;

import com.mystore.app.dto.*;
import com.mystore.app.entity.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {OrderMapper.class})
public interface PaymentMapper {

    @Mapping(target = "paymentId", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "paidAt", ignore = true)
    @Mapping(source = "method", target = "method", qualifiedByName = "stringToMethod")
    @Mapping(source = "status", target = "status", qualifiedByName = "stringToStatus")
    Payment toEntity(PaymentRequestDTO dto);

    @Mapping(source = "order", target = "order")
    PaymentResponseDTO toResponse(Payment entity);

    @Named("stringToMethod")
    default PaymentMethod stringToMethod(String value) {
        return value != null ? PaymentMethod.valueOf(value) : null;
    }

    @Named("stringToStatus")
    default PaymentStatus stringToStatus(String value) {
        return value != null ? PaymentStatus.valueOf(value) : null;
    }
}