package com.fabidoces_microservices.client_service.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Data
@Table(name = "Cliente")
@NoArgsConstructor
@AllArgsConstructor
public class Client {

    @Id
    @GeneratedValue()
    @Column(name = "Id")
    private Long id;

    @Column(name = "CPF", nullable = false, length = 15, unique = true)
    private String cpf;

    @Column(name = "Nome", length = 60)
    private String nome;

    @Column(name = "Email", nullable = false, length = 150, unique = true)
    private String email;

    @Column(name = "Telefone", nullable = false, length = 20)
    private String telefone;

    @Column(name = "Senha", nullable = false, length = 200)
    private String senha;

    @Column(name = "Status_Cliente")
    @ColumnDefault("0")
    private int statusCliente;

    @Column(name = "Status_Termo")
    @ColumnDefault("0")
    private int statusTermo;
}
