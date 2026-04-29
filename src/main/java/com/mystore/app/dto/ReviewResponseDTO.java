package com.mystore.app.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDTO {
    private Integer reviewId;
    private ProductResponseDTO product;
    private ClientResponseDTO client;
    private Short rating;
    private String title;
    private String body;
    private Instant reviewedAt;
    private Integer helpfulVotes;
}