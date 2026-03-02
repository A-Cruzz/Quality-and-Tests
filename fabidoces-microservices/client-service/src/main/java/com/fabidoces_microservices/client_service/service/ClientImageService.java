package com.fabidoces_microservices.client_service.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fabidoces_microservices.client_service.model.dto.ClientProfileImageDTO;
import com.fabidoces_microservices.client_service.model.entity.Client;
import com.fabidoces_microservices.client_service.model.entity.ClientProfileImage;
import com.fabidoces_microservices.client_service.repository.ClientRepository;
import com.fabidoces_microservices.client_service.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClientImageService {
    @Autowired
    Cloudinary cloudinary;

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    ClientRepository clientRepository;

    public ClientProfileImageDTO uploadProfileImage(MultipartFile file, Long clientId){
        try {

            Client client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            String originalFileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFileName);
            String storedName = UUID.randomUUID().toString() + fileExtension;

            // Upload pro Cloudinary
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", storedName,
                            "folder", "clients"
                    )
            );


            ClientProfileImage image = new ClientProfileImage();
            image.setOriginalName(originalFileName);
            image.setStoredName(storedName);
            image.setCloudinaryPublicId(uploadResult.get("public_id").toString());
            image.setCloudinaryUrl(uploadResult.get("secure_url").toString());
            image.setFileSize(file.getSize());
            image.setClientId(clientId);

            ClientProfileImage savedImage = imageRepository.save(image);

            return convertToDTO(savedImage);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao fazer upload da imagem: " + e.getMessage());
        }
    }

    public Optional<ClientProfileImageDTO> getImageByClientId(Long clientId) {
        List<ClientProfileImage> images = imageRepository.findByClientId(clientId);

        if (images == null || images.isEmpty()) {
            return Optional.empty();
        }

        return images.stream()
                .max(Comparator.comparing(ClientProfileImage::getCreatedAt))
                .map(this::convertToDTO);
    }


    public List<ClientProfileImageDTO> getAllImages() {
        return imageRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    public void deleteImage(Long id) {
        ClientProfileImage image = imageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Imagem não encontrada"));
        try {
            cloudinary.uploader().destroy(image.getCloudinaryPublicId(), ObjectUtils.emptyMap());
            imageRepository.delete(image);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao deletar imagem: " + e.getMessage());
        }
    }

    private String getFileExtension(String fileName) {
        return fileName != null && fileName.contains(".")
                ? fileName.substring(fileName.lastIndexOf("."))
                : "";
    }


    private ClientProfileImageDTO convertToDTO(ClientProfileImage entity) {
        ClientProfileImageDTO dto = new ClientProfileImageDTO();
        dto.setId(entity.getId());
        dto.setOriginalName(entity.getOriginalName());
        dto.setCloudinaryUrl(entity.getCloudinaryUrl());
        dto.setStoredName(entity.getStoredName());
        dto.setFileSize(entity.getFileSize());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    public boolean isValidImageType(MultipartFile file) {
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();

        boolean byContentType = contentType != null && (
                contentType.equals("image/jpeg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/gif") ||
                        contentType.equals("image/webp")
        );

        boolean byExtension = originalFilename != null && (
                originalFilename.toLowerCase().endsWith(".jpg") ||
                        originalFilename.toLowerCase().endsWith(".jpeg") ||
                        originalFilename.toLowerCase().endsWith(".png") ||
                        originalFilename.toLowerCase().endsWith(".gif") ||
                        originalFilename.toLowerCase().endsWith(".webp")
        );

        return byContentType || byExtension;
    }
}
