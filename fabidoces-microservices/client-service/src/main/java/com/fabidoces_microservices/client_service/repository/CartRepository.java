package com.fabidoces_microservices.client_service.repository;

import com.fabidoces_microservices.client_service.model.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<List<Cart>> findAllByClientId(Long clientId);

    Cart findByProductId(Long productId);

    @Modifying
    @Query("UPDATE Cart c SET c.quantity = :quantity WHERE c.id = :id")
    void updateQuantity(
            @Param("id") Long id,
            @Param("quantity") Long quantity
    );

    @Modifying
    @Query(value = "INSERT INTO carrinho (id_cliente, id_produto, quantidade) VALUES (:clientId, :productId, :quantity) ON DUPLICATE KEY UPDATE quantidade = :quantity", nativeQuery = true)
    void updateOrInsert(
            @Param("clientId") Long clientId,
            @Param("productId") Long productId,
            @Param("quantity") Long quantity
    );

    @Query("SELECT c FROM Cart c WHERE c.clientId = :clientId AND c.productId = :productId")
    Cart findByClientIdAndProductId(
            @Param("clientId") Long clientId,
            @Param("productId") Long productId
    );

    @Modifying
    @Query("DELETE FROM Cart c WHERE c.clientId = :clientId AND c.productId = :productId")
    void deleteByClientIdAndProductId(
            @Param("clientId") Long clientId,
            @Param("productId") Long productId
    );

    boolean existsByClientIdAndProductId(Long clientId, Long productId);

    void deleteAllByClientId(Long clientId);
}


