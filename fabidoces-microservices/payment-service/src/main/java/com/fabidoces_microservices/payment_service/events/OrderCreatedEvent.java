package com.fabidoces_microservices.payment_service.events;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderCreatedEvent {
    private Long orderId;
    private Long clientId;
    private String clientEmail;
    private String clientName;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
}
