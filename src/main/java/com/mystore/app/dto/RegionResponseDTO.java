package com.mystore.app.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegionResponseDTO {
    private Integer regionId;
    private String regionName;
    private String country;
    private String timezone;
}