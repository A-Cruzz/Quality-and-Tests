package com.fabidoces_microservices.email_service.model.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Client {
    private String name;
    private String email;
    private String tel;
}