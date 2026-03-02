package com.fabidoces_microservices.client_service.model.dto;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddressDTO {
    private Long clientId;
    private String addressName;
    private String zipCode;
    private String street;
    private String neighborhood;
    private String city;
    private String state;
}