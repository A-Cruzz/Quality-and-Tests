package com.fabidoces_microservices.client_service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CartDTO {
    private Long clientId;
    private Long productId;
    private Long quantity;
}
