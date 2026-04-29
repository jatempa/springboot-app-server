package com.mystore.app.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponseDTO {
    private Integer employeeId;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private RegionResponseDTO region;
    private LocalDate hireDate;
    private BigDecimal salary;
    private Integer managerId;
}