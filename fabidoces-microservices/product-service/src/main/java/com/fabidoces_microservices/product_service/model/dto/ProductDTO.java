package com.fabidoces_microservices.product_service.model.dto;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@ToString
public class ProductDTO {
    private Long id;
    private Long vendorId;
    private String name;
    private BigDecimal price;
    private List<String> productSize;
    private List<String> colour;
    private String description;
    private String imageUrl;
}
