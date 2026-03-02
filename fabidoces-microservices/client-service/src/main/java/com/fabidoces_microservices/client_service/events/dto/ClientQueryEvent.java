package com.fabidoces_microservices.client_service.events.dto;

import lombok.Data;

@Data
public class ClientQueryEvent {
    private Long clientId;
    private String correlationId;
}
