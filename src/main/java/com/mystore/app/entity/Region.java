package com.mystore.app.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "regions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "region_id")
    private Integer regionId;

    @Column(name = "region_name", nullable = false, length = 100)
    private String regionName;

    @Column(name = "country", nullable = false, length = 100)
    private String country = "Mexico";

    @Column(name = "timezone", nullable = false, length = 50)
    private String timezone = "America/Mexico_City";
}