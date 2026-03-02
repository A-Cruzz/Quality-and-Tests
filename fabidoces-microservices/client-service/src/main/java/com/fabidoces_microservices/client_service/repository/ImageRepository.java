package com.fabidoces_microservices.client_service.repository;

import com.fabidoces_microservices.client_service.model.entity.ClientProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<ClientProfileImage, Long> {
    Optional<ClientProfileImage> findByCloudinaryPublicId(String publicId);
    List<ClientProfileImage> findAllByOrderByCreatedAtDesc();
    List<ClientProfileImage> findByClientId(Long clientId);
    Optional<ClientProfileImage> findByClientIdAndId(Long clientId, Long id);
}
