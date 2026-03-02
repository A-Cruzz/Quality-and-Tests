package com.fabidoces_microservices.product_service.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@ToString
public class ProductImageDTO {
    private Long id;
    private String originalName;
    private String cloudinaryUrl;
    private String storedName;
    private Long fileSize;
    private LocalDateTime createdAt;
}
