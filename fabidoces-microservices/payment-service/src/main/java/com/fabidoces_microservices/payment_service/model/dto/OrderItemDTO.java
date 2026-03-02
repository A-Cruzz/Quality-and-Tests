package com.fabidoces_microservices.payment_service.model.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemDTO {
    private Long productId;
    private Long vendorId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;

    public BigDecimal getSubtotal() {
        if (subtotal != null) {
            return subtotal;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}