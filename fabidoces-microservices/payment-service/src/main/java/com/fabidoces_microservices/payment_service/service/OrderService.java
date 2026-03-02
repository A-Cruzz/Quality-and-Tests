package com.fabidoces_microservices.payment_service.service;

import com.fabidoces_microservices.payment_service.events.OrderCreatedEvent;
import com.fabidoces_microservices.payment_service.events.dto.ClientQueryEvent;
import com.fabidoces_microservices.payment_service.events.dto.ClientResponseEvent;
import com.fabidoces_microservices.payment_service.model.dto.*;
import com.fabidoces_microservices.payment_service.model.entity.Order;
import com.fabidoces_microservices.payment_service.model.entity.OrderItem;
import com.fabidoces_microservices.payment_service.repository.OrderItemRepository;
import com.fabidoces_microservices.payment_service.repository.OrderRepository;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;


@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    @Value("${app.rabbitmq.timeout.client-query}")
    private String clientTimeout;

    @Value("${app.rabbitmq.queues.client.query}")
    private String clientQueryQueue;

    @Value("${app.rabbitmq.queues.client.response}")
    private String clientResponseQueue;

    @Value("${app.rabbitmq.queues.order.created}")
    private String orderCreatedQueue;

    @Value("${app.rabbitmq.queues.email.order}")
    private String orderEmailQueue;

    @Value("${app.rabbitmq.queues.logistics.request}")
    private String logisticsRequestQueue;

    @Value("${order.payment.timeout-minutes}")
    private int paymentTimeoutMinutes;

    @Value("${order.email.notifications-enabled}")
    private boolean emailNotificationsEnabled;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${app.gateway.url:http://localhost:8080}")
    private String gatewayUrl;

    private final ConcurrentHashMap<String, CompletableFuture<ClientResponseEvent>> pendingRequests = new ConcurrentHashMap<>();

    private ClientResponseEvent getClientDataViaGateway(Long clientId) {
        try {
            String url = gatewayUrl + "/api/client/internal/" + clientId;
            ResponseEntity<ClientResponseEvent> response = restTemplate.getForEntity(url, ClientResponseEvent.class);
            return response.getBody();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar dados do cliente: " + clientId, e);
        }
    }

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO orderRequest) throws MPException, MPApiException {
        BigDecimal totalAmount = calculateTotalAmount(orderRequest.getItems());

        Order order = createOrderEntity(orderRequest, totalAmount);
        Order savedOrder = orderRepository.save(order);
        createAndSaveOrderItems(orderRequest.getItems(), savedOrder);
        ClientResponseEvent clientData = getClientDataViaGateway(orderRequest.getClientId());

        if (!clientData.isFound()) {
            throw new RuntimeException("Cliente não encontrado: " + orderRequest.getClientId());
        }

        PixResponse pixResponse = paymentService.processPixPayment(
                totalAmount,
                clientData.getEmail(),
                clientData.getFirstName(),
                clientData.getCpf()
        );

        savedOrder.setTransactionId(pixResponse.getTransactionId());
        orderRepository.save(savedOrder);

        triggerOrderCreatedEvent(savedOrder, clientData);
        return buildOrderResponse(savedOrder, pixResponse, orderRequest.getItems());
    }

    private ClientResponseEvent getClientDataViaEvent(Long clientId) {
        String correlationId = UUID.randomUUID().toString();

        CompletableFuture<ClientResponseEvent> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, future);

        ClientQueryEvent queryEvent = new ClientQueryEvent();
        queryEvent.setClientId(clientId);
        queryEvent.setCorrelationId(correlationId);

        try {
            rabbitTemplate.convertAndSend(clientQueryQueue, queryEvent);
            int timeoutSeconds = Integer.parseInt(clientTimeout.replace("s", ""));
            return future.get(timeoutSeconds, TimeUnit.SECONDS);

        } catch (TimeoutException e) {
            pendingRequests.remove(correlationId);
            throw new RuntimeException("Timeout ao buscar cliente: " + clientId, e);
        } catch (Exception e) {
            pendingRequests.remove(correlationId);
            throw new RuntimeException("Erro ao buscar cliente: " + clientId, e);
        }
    }

    public void handleClientResponse(ClientResponseEvent responseEvent) {
        if (responseEvent.getCorrelationId() == null) {
            return;
        }

        CompletableFuture<ClientResponseEvent> future = pendingRequests.remove(responseEvent.getCorrelationId());
        if (future != null) {
            future.complete(responseEvent);
        }
    }


    @Transactional
    public void processPaymentWebhook(PaymentWebhookDTO webhookDTO) {
        String transactionId = webhookDTO.getData().getId();
        String paymentStatus = webhookDTO.getData().getStatus();

        Order order = orderRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado para transaction: " + transactionId));

        order.setPaymentStatus(mapPaymentStatus(paymentStatus));

        if ("approved".equals(paymentStatus)) {
            order.setOrderStatus("confirmed");
            triggerOrderUpdatedEvent(order, "confirmed");
        }

        orderRepository.save(order);

        if (emailNotificationsEnabled) {
            triggerEmailEvent(order, paymentStatus);
        }
    }

    public List<Order> getOrdersByClient(Long clientId) {
        return orderRepository.findByClientId(clientId);
    }

    public List<Order> getOrdersByPaymentStatus(String status) {
        return orderRepository.findByPaymentStatus(status);
    }

    public List<Order> getOrdersByOrderStatus(String status) {
        return orderRepository.findByOrderStatus(status);
    }

    @Transactional
    public void updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado: " + orderId));

        order.setOrderStatus(newStatus);
        orderRepository.save(order);

        triggerOrderUpdatedEvent(order, newStatus);
    }

    @Transactional
    public void updateEmailStatus(Long orderId, String emailStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado: " + orderId));

        order.setEmailStatus(emailStatus);
        orderRepository.save(order);
    }

    public List<Order> getPendingEmailNotifications() {
        return orderRepository.findByEmailStatus("not_sent");
    }

    // ========== MÉTODOS PRIVADOS ==========

    private BigDecimal calculateTotalAmount(List<OrderItemDTO> items) {
        return items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Order createOrderEntity(OrderRequestDTO orderRequest, BigDecimal totalAmount) {
        Order order = new Order();
        order.setClientId(orderRequest.getClientId());
        order.setTotalAmount(totalAmount);
        order.setPaymentStatus("pending");
        order.setOrderStatus("pending");
        order.setEmailStatus("not_sent");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        return order;
    }

    private void createAndSaveOrderItems(List<OrderItemDTO> itemDTOs, Order order) {
        List<OrderItem> orderItems = itemDTOs.stream()
                .map(itemDTO -> createOrderItem(itemDTO, order))
                .collect(Collectors.toList());

        orderItemRepository.saveAll(orderItems);

        System.out.println("✅ " + orderItems.size() + " itens salvos para o pedido " + order.getId());
    }

    private OrderItem createOrderItem(OrderItemDTO itemDTO, Order order) {
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProductId(itemDTO.getProductId());
        item.setVendorId(itemDTO.getVendorId());
        item.setProductName(itemDTO.getProductName());
        item.setQuantity(itemDTO.getQuantity());
        item.setUnitPrice(itemDTO.getUnitPrice());
        item.setSubtotal(itemDTO.getUnitPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
        return item;
    }

    private void triggerOrderCreatedEvent(Order order, ClientResponseEvent clientData) {
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId(order.getId());
        event.setClientId(order.getClientId());
        event.setClientEmail(clientData.getEmail());

        String clientName = clientData.getFirstName() +
                (clientData.getLastName() != null && !clientData.getLastName().isEmpty() ?
                        " " + clientData.getLastName() : "");
        event.setClientName(clientName.trim());

        event.setTotalAmount(order.getTotalAmount());
        event.setCreatedAt(order.getCreatedAt());

        rabbitTemplate.convertAndSend(orderCreatedQueue, event);
        System.out.println("✅ OrderCreatedEvent enviado para: " + orderCreatedQueue);
    }

    private void triggerOrderUpdatedEvent(Order order, String newStatus) {
        // Implementar quando necessário
        System.out.println("📝 OrderUpdatedEvent disparado - Order: " + order.getId() + ", Status: " + newStatus);
    }

    private void triggerEmailEvent(Order order, String paymentStatus) {
        if (emailNotificationsEnabled) {
            // Implementar evento de email
        }
    }

    private String mapPaymentStatus(String mpStatus) {
        return switch (mpStatus) {
            case "pending" -> "pending";
            case "approved" -> "approved";
            case "rejected" -> "rejected";
            case "cancelled" -> "cancelled";
            default -> "unknown";
        };
    }

    private OrderResponseDTO buildOrderResponse(Order order, PixResponse pixResponse, List<OrderItemDTO> originalItems) {
        OrderResponseDTO response = new OrderResponseDTO();
        response.setOrderId(order.getId());
        response.setClientId(order.getClientId());
        response.setTransactionId(order.getTransactionId());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setOrderStatus(order.getOrderStatus());
        response.setEmailStatus(order.getEmailStatus());
        response.setTotalAmount(order.getTotalAmount());

        System.out.println("=== PIX DATA NO BUILD RESPONSE ===");
        System.out.println("QR Code: " + (pixResponse.getQrCode() != null ? "PRESENTE" : "AUSENTE"));
        System.out.println("QR Code Base64: " + (pixResponse.getQrCodeBase64() != null ? "PRESENTE" : "AUSENTE"));
        System.out.println("Copy Paste Code: " + (pixResponse.getCopyPasteCode() != null ? "PRESENTE" : "AUSENTE"));

        response.setQrCode(pixResponse.getQrCode());
        response.setQrCodeBase64(pixResponse.getQrCodeBase64());
        response.setCopyPasteCode(pixResponse.getCopyPasteCode());
        response.setExpirationTime(pixResponse.getExpirationTime());

        response.setItems(originalItems);

        return response;
    }

    private OrderItemDTO convertToItemDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setProductId(item.getProductId());
        dto.setVendorId(item.getVendorId());
        dto.setProductName(item.getProductName());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());


        BigDecimal subtotal = item.getSubtotal() != null ?
                item.getSubtotal() :
                item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        dto.setSubtotal(subtotal);

        return dto;
    }
}