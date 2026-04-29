package com.mystore.app.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponseDTO {
    private Integer clientId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate birthDate;
    private String gender;
    private RegionResponseDTO region;
    private String city;
    private String address;
    private Integer loyaltyPts;
    private String segment;
    private Instant createdAt;
    private Instant lastLogin;
}