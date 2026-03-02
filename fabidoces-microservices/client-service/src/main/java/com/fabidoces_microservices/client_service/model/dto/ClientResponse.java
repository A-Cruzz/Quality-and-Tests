package com.fabidoces_microservices.client_service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ClientResponse {
    private long id;
    private String nome;
    private String email;
    private String telefone;
    private int status;
}