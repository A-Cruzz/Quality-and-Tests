package com.fabidoces_microservices.product_service.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "imagem_produto")
@AllArgsConstructor
@NoArgsConstructor
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "Nome_Original")
    private String originalName;
    private String storedName;
    private String cloudinaryPublicId;
    private String cloudinaryUrl;
    private Long fileSize;

    @Column(name = "product_id")
    private Long productId;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
