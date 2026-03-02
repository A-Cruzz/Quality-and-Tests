package com.fabidoces_microservices.client_service.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.C;

@Entity
@Data
@Table(name = "Carrinho",uniqueConstraints = @UniqueConstraint(columnNames = {"id_cliente", "id_produto"}))
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    @JsonIgnore
    private Long id;

    @Column(name = "Id_Cliente")
    private Long clientId;

    @Column(name = "Id_produto")
    private Long productId;

    @Column(name = "Quantidade")
    private Long quantity;

    public Cart(Long clientId, Long productId, Long quantity) {
        this.clientId = clientId;
        this.productId = productId;
        this.quantity = quantity;
    }

}
