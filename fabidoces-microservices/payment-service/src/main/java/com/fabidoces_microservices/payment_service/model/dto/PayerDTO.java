package com.fabidoces_microservices.payment_service.model.dto;

import lombok.Data;

@Data
public class PayerDTO {
    private String email;
    private String firstName;
    private String lastName;
    private IdentificationDTO identification;
}
