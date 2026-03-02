package com.fabidoces_microservices.email_service.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailEvent{
    private String email;
    private String nome;
    private Long clientId;
    private String tipoEmail;
}