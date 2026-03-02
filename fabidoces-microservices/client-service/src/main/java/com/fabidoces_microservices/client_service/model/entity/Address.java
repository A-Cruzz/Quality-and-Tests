package com.fabidoces_microservices.client_service.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.C;

@Entity
@Data
@Table(name = "endereco_cliente")
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Id
    @GeneratedValue
    @Column(name = "Id")
    Long id;

    @Column(name = "Id_Cliente")
    Long clientId;

    @Column(name = "Nome_Endereco")
    String addressName;

    @Column(name = "CEP")
    String zipCode;

    @Column(name = "Logradouro")
    String street;

    @Column(name = "Numero")
    Long number;

    @Column(name = "Complemento")
    String complement;

    @Column(name = "Bairro")
    String neighborhood;

    @Column(name = "Cidade")
    String city;

    @Column(name = "Estado")
    String state;

    @Column(name = "Endereco_Padrao")
    Boolean isDefault;

}
