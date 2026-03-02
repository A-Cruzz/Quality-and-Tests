package com.fabidoces_microservices.payment_service.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore; // ⬅️ IMPORTE ESTE
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = "itens_pedido")
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_item")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_pedido", nullable = false)
    @JsonIgnore
    private Order order;

    @Column(name = "id_produto", nullable = false)
    private Long productId;

    @Column(name = "vendor_id", nullable = false)
    private Long vendorId;

    @Column(name = "nome_produto")
    private String productName;

    @Column(name = "quantidade")
    private Integer quantity;

    @Column(name = "preco_unitario", precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;
}