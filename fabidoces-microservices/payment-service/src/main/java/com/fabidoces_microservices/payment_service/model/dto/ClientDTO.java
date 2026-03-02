package com.fabidoces_microservices.payment_service.model.dto;

import lombok.Data;

@Data
public class ClientDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String cpf;
    private String phone;
}