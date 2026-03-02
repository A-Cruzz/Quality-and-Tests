package com.fabidoces_microservices.email_service.model.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    private String name;
    private int quantity;
    private double price;
    private String image;
}