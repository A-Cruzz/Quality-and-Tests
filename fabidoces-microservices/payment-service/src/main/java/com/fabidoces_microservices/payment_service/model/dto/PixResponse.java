package com.fabidoces_microservices.payment_service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PixResponse {
    private String qrCode;
    private String qrCodeBase64;
    private String copyPasteCode;
    private BigDecimal amount;
    private String expirationTime;
    private String transactionId;
    private String status;
    private String paymentMethod;
}