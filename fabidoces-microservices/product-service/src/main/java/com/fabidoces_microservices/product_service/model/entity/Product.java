package com.fabidoces_microservices.product_service.model.entity;

import com.fabidoces_microservices.product_service.utils.StringConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Entity
@Data
@Table(name = "Produto")
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "Id_Vendedor")
    private Long vendorId;

    @Column(name = "Nome", nullable = false, length = 100)
    private String name;

    @Column(name = "Preco")
    private BigDecimal price;

    @Column(name = "Tamanho", columnDefinition = "json", length = 10)
    @Convert(converter = StringConverter.class)
    private List<String> productSize;

    @Column(name = "Cor", columnDefinition = "json", length = 50)
    @Convert(converter = StringConverter.class)
    private List<String> colour;

    @Column(name = "Descricao", length = 500)
    private String description;

}