package com.fabidoces_microservices.client_service.repository;

import com.fabidoces_microservices.client_service.model.entity.Client;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    boolean existsByCpf(String cpf);

    @Modifying
    @Transactional
    @Query("UPDATE Client c SET c.statusCliente = 1 WHERE c.id = :id")
    int atualizaStatusCliente(@Param("id") Long id);

    Optional<Client> findByEmail(String email);

    @Modifying
    @Transactional
    @Query("update Client c set c.senha = :senha where c.id = :id")
    int updatePasswordById(@Param("id") Long id, @Param("senha") String senha);
}
