package com.fabidoces_microservices.payment_service.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "pedidos")
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Long id;

    @Column(name = "id_cliente", nullable = false)
    private Long clientId;

    @Column(name = "transaction_id")
    private String transactionId; // ID da transação do Mercado Pago

    @Column(name = "status_pagamento")
    private String paymentStatus; // pending, approved, rejected, cancelled

    @Column(name = "status_pedido")
    private String orderStatus; // pending, confirmed, preparing, ready, delivered, cancelled

    @Column(name = "status_email")
    private String emailStatus; // not_sent, sent, failed

    @Column(name = "valor_total", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "data_criacao")
    private LocalDateTime createdAt;

    @Column(name = "data_atualizacao")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}