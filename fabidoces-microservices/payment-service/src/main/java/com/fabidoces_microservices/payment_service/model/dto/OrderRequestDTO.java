package com.fabidoces_microservices.payment_service.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderRequestDTO {
    private Long clientId;
    private List<OrderItemDTO> items;
}
