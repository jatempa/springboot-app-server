package com.mystore.app.dto.mapper;

import com.mystore.app.dto.*;
import com.mystore.app.entity.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {ProductMapper.class, ClientMapper.class})
public interface ReviewMapper {

    @Mapping(target = "reviewId", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "reviewedAt", ignore = true)
    @Mapping(target = "helpfulVotes", ignore = true)
    Review toEntity(ReviewRequestDTO dto);

    @Mapping(source = "product", target = "product")
    @Mapping(source = "client", target = "client")
    ReviewResponseDTO toResponse(Review entity);
}