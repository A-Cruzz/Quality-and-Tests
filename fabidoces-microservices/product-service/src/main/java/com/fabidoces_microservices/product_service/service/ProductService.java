package com.fabidoces_microservices.product_service.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fabidoces_microservices.product_service.model.dto.ProductDTO;
import com.fabidoces_microservices.product_service.model.dto.ProductImageDTO;
import com.fabidoces_microservices.product_service.model.entity.Product;
import com.fabidoces_microservices.product_service.model.entity.ProductImage;
import com.fabidoces_microservices.product_service.repository.ImageRepository;
import com.fabidoces_microservices.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private Cloudinary cloudinary;

    public Product saveProduct(Product product, MultipartFile file) {
        Product savedProduct = productRepository.save(product);
        if (file != null && !file.isEmpty()) {
            uploadProductImage(file, savedProduct.getId());
        }
        return savedProduct;
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public Boolean saveMultipleProducts(List<Product> products, List<MultipartFile> files) {
        if (products == null || products.isEmpty()) {
            return false;
        }

        try {
            List<Product> savedProducts = productRepository.saveAll(products);
            if (files != null && !files.isEmpty()) {
                for (int i = 0; i < Math.min(files.size(), savedProducts.size()); i++) {
                    MultipartFile file = files.get(i);
                    Product product = savedProducts.get(i);

                    if (file != null && !file.isEmpty()) {
                        uploadProductImage(file, product.getId());
                    }
                }
            }

            return true;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar múltiplos produtos: " + e.getMessage());
        }
    }

    public Boolean saveMultipleProducts(List<Product> products) {
        return saveMultipleProducts(products, null);
    }

    public Long retrieveVendorID(Long productId){
        Optional<Product> product = productRepository.findById(productId);
        if (product.isPresent()){
            return product.get().getVendorId();
        }
        else {
            return (long) -1;
        }
    }

    public ProductImageDTO uploadProductImage(MultipartFile file, Long productId) {
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + productId));

            if (!isValidImageType(file)) {
                throw new RuntimeException("Tipo de arquivo não suportado: " + file.getContentType());
            }

            String originalFileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFileName);
            String storedName = UUID.randomUUID().toString() + fileExtension;

            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", storedName,
                            "folder", "products"
                    )
            );

            ProductImage image = new ProductImage();
            image.setOriginalName(originalFileName);
            image.setStoredName(storedName);
            image.setCloudinaryPublicId(uploadResult.get("public_id").toString());
            image.setCloudinaryUrl(uploadResult.get("secure_url").toString());
            image.setFileSize(file.getSize());
            image.setProductId(productId);

            ProductImage savedImage = imageRepository.save(image);

            return convertToDTO(savedImage);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao fazer upload da imagem: " + e.getMessage());
        }
    }

    public List<ProductImageDTO> uploadMultipleProductImages(List<MultipartFile> files, Long productId) {
        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }

        return files.stream()
                .filter(file -> file != null && !file.isEmpty())
                .map(file -> uploadProductImage(file, productId))
                .collect(Collectors.toList());
    }

    public Optional<ProductImageDTO> getImageByProductId(Long productId) {
        List<ProductImage> images = imageRepository.findAllByProductId(productId);

        if (images == null || images.isEmpty()) {
            return Optional.empty();
        }

        return images.stream()
                .max(Comparator.comparing(ProductImage::getCreatedAt))
                .map(this::convertToDTO);
    }

    public List<ProductImageDTO> getAllImagesByProductId(Long productId) {
        return imageRepository.findAllByProductId(productId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public boolean isValidImageType(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();

        boolean byContentType = contentType != null && (
                contentType.equals("image/jpeg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/gif") ||
                contentType.equals("image/webp") ||
                contentType.equals("image/jpg")
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

    private String getFileExtension(String fileName) {
        return fileName != null && fileName.contains(".")
                ? fileName.substring(fileName.lastIndexOf("."))
                : "";
    }

    private ProductImageDTO convertToDTO(ProductImage entity) {
        ProductImageDTO dto = new ProductImageDTO();
        dto.setId(entity.getId());
        dto.setOriginalName(entity.getOriginalName());
        dto.setCloudinaryUrl(entity.getCloudinaryUrl());
        dto.setStoredName(entity.getStoredName());
        dto.setFileSize(entity.getFileSize());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    public void deleteProduct(Long productId) {
        List<ProductImage> images = imageRepository.findAllByProductId(productId);
        if (images != null && !images.isEmpty()) {
            images.forEach(image -> {
                try {
                    cloudinary.uploader().destroy(image.getCloudinaryPublicId(), ObjectUtils.emptyMap());
                } catch (Exception e) {
                    System.err.println("Erro ao deletar imagem do Cloudinary: " + e.getMessage());
                }
            });
            imageRepository.deleteAll(images);
        }
        productRepository.deleteById(productId);
    }

    public void deleteAlVendorProduct(Long vendorId) {
        List<ProductDTO> products = retrieveVendorProducts(vendorId);
        for (ProductDTO vendorProduct: products){

            List<ProductImage> images = imageRepository.findAllByProductId(vendorProduct.getId());
            if (images != null && !images.isEmpty()) {
                images.forEach(image -> {
                    try {
                        cloudinary.uploader().destroy(image.getCloudinaryPublicId(), ObjectUtils.emptyMap());
                    } catch (Exception e) {
                        System.err.println("Erro ao deletar imagem do Cloudinary: " + e.getMessage());
                    }
                });
                imageRepository.deleteAll(images);
            }
            productRepository.deleteById(vendorProduct.getId());
        }
    }



    private int deleteImage(Long productId){
        List<ProductImage> images = imageRepository.findAllByProductId(productId);
        if (images != null && !images.isEmpty()) {
            images.forEach(image -> {
                try {
                    cloudinary.uploader().destroy(image.getCloudinaryPublicId(), ObjectUtils.emptyMap());
                } catch (Exception e) {
                    System.err.println("Erro ao deletar imagem do Cloudinary: " + e.getMessage());
                }
            });
            imageRepository.deleteAll(images);
            return 1;
        }
        return -1;
    }

    private List<Product> retrieveData() {return productRepository.findAll();}

    public List<ProductDTO> retrieveVendorProducts(Long vendorId){
        List<Product> list = productRepository.findByVendorId(vendorId);
        List<ProductDTO> producList = new ArrayList<>();

        for (Product product : list){
            Optional<ProductImageDTO> image = getImageByProductId(product.getId());
            String url = image.map(ProductImageDTO::getCloudinaryUrl).orElse(null);

            ProductDTO dto = new ProductDTO();

            dto.setVendorId(vendorId);
            dto.setId(product.getId());
            dto.setName(product.getName());
            dto.setPrice(product.getPrice());
            dto.setProductSize(product.getProductSize());
            dto.setColour(product.getColour());
            dto.setDescription(product.getDescription());
            dto.setImageUrl(url);

            producList.add(dto);
        }
        return producList;
    }

    public List<ProductDTO> retriveCartProducts(List<Long> productIds) {
        List<ProductDTO> productList = new ArrayList<>();

        if (productIds == null || productIds.isEmpty()) {
            return productList;
        }

        for (Long productId : productIds) {
            Optional<Product> dbProduct = productRepository.findById(productId);

            if (dbProduct.isPresent()) {
                Product product = dbProduct.get();
                ProductDTO dto = new ProductDTO();

                dto.setVendorId(product.getVendorId());
                dto.setId(product.getId());
                dto.setName(product.getName());
                dto.setPrice(product.getPrice());
                dto.setProductSize(product.getProductSize());
                dto.setColour(product.getColour());
                dto.setDescription(product.getDescription());

                Optional<ProductImageDTO> image = getImageByProductId(productId);
                image.ifPresent(productImageDTO -> dto.setImageUrl(productImageDTO.getCloudinaryUrl()));

                productList.add(dto);
            }
        }

        return productList;
    }

    public List<ProductDTO> retriveProducts(){
        List<Product> list = retrieveData();
        List<ProductDTO> productDto = new ArrayList<>();

        for (Product product : list) {
            Optional<ProductImageDTO> image = getImageByProductId(product.getId());
            String url = image.map(ProductImageDTO::getCloudinaryUrl).orElse(null);

            ProductDTO dto = new ProductDTO();

            dto.setId(product.getId());
            dto.setName(product.getName());
            dto.setPrice(product.getPrice());
            dto.setProductSize(product.getProductSize());
            dto.setColour(product.getColour());
            dto.setDescription(product.getDescription());
            dto.setVendorId(product.getVendorId());
            dto.setImageUrl(url);

            productDto.add(dto);
        }

        return productDto;
    }

    public int updateProduct(ProductDTO product){
        Optional<Product> productInfo = productRepository.findById(product.getId());

        if (productInfo.isPresent()) {
            Product existingProduct = productInfo.get();

            if (product.getName() != null) {
                existingProduct.setName(product.getName());
            }
            if (product.getPrice() != null) {
                existingProduct.setPrice(product.getPrice());
            }
            if (product.getProductSize() != null) {
                existingProduct.setProductSize(product.getProductSize());
            }
            if (product.getColour() != null) {
                existingProduct.setColour(product.getColour());
            }
            if (product.getDescription() != null) {
                existingProduct.setDescription(product.getDescription());
            }
            productRepository.save(existingProduct);
            return 1;
        } else {
            return -1;
        }
    }

    public int updateProduct(ProductDTO product, MultipartFile imageFile) {
        Optional<Product> productInfo = productRepository.findById(product.getId());

        if (productInfo.isPresent()) {
            Product existingProduct = productInfo.get();

            if (product.getName() != null) {
                existingProduct.setName(product.getName());
            }
            if (product.getPrice() != null) {
                existingProduct.setPrice(product.getPrice());
            }
            if (product.getProductSize() != null) {
                existingProduct.setProductSize(product.getProductSize());
            }
            if (product.getColour() != null) {
                existingProduct.setColour(product.getColour());
            }
            if (product.getDescription() != null) {
                existingProduct.setDescription(product.getDescription());
            }

            try {
                deleteImage(product.getId());
                uploadProductImage(imageFile, product.getId());
            } catch (Exception e) {
                return -2;
            }

            productRepository.save(existingProduct);
            return 1;

        } else {
            return -1;
        }
    }

}