package com.fabidoces_microservices.payment_service.service;

import com.fabidoces_microservices.payment_service.model.dto.PaymentRequestDTO;
import com.fabidoces_microservices.payment_service.model.dto.PixResponse;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class PaymentService {

    @Value("${mercado-pago.access-token}")
    private String accessToken;

    @Value("${mercado-pago.pix.description}")
    private String description;

    @Value("${mercado-pago.pix.expiration-minutes}")
    private int expirationMinutes;

    public PixResponse processPixPayment(BigDecimal amount, String email, String firstName, String cpf) throws MPException, MPApiException {

        try {
            System.out.println("🔧 Iniciando processamento PIX - Amount: {}, Email: {}, Nome: {}, CPF: {} " + amount + email + firstName + cpf);

            // Verifica se o access token está configurado
            if (accessToken == null || accessToken.isEmpty()) {
                throw new RuntimeException("Access Token do Mercado Pago não configurado");
            }

            MercadoPagoConfig.setAccessToken(accessToken);
            System.out.println("✅ Access Token configurado");

            PaymentClient client = new PaymentClient();
            OffsetDateTime expirationTime = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(expirationMinutes);

            // Log dos dados da requisição
            System.out.println("📦 Criando requisição PIX - Valor: {}, Descrição: {}, Expiração: {} minutos" + amount + description + expirationMinutes);

            PaymentCreateRequest paymentCreateRequest = PaymentCreateRequest.builder()
                    .transactionAmount(amount)
                    .description(description)
                    .paymentMethodId("pix")
                    .dateOfExpiration(expirationTime)
                    .payer(PaymentPayerRequest.builder()
                            .email(email)
                            .firstName(firstName)
                            .identification(IdentificationRequest.builder()
                                    .type("CPF")
                                    .number(limparCpf(cpf)) // ⬅️ IMPORTANTE: limpar CPF
                                    .build())
                            .build())
                    .build();

            System.out.println("🚀 Enviando requisição para Mercado Pago...");
            Payment payment = client.create(paymentCreateRequest);
            System.out.println("✅ Pagamento criado com sucesso - ID: {}" + payment.getId());

            return buildPixResponse(payment, amount);

        } catch (MPApiException e) {
            throw e;
        } catch (MPException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar pagamento PIX", e);
        }
    }

    private PixResponse buildPixResponse(Payment payment, BigDecimal amount) {
        PixResponse response = new PixResponse();
        response.setTransactionId(payment.getId().toString());
        response.setAmount(amount);
        response.setExpirationTime(payment.getDateOfExpiration().toString());

        if (payment.getPointOfInteraction() != null
                && payment.getPointOfInteraction().getTransactionData() != null) {

            var transactionData = payment.getPointOfInteraction().getTransactionData();

            response.setQrCode(transactionData.getQrCode());
            response.setQrCodeBase64(transactionData.getQrCodeBase64());

            response.setCopyPasteCode(transactionData.getQrCode());

        } else {
            System.err.println("TransactionData ou PointOfInteraction está nulo!");
        }

        return response;
    }

    public PixResponse getPixPaymentData(String transactionId) throws MPException, MPApiException {
        try {
            MercadoPagoConfig.setAccessToken(accessToken);
            PaymentClient client = new PaymentClient();
            Payment payment = client.get(Long.parseLong(transactionId));
            return buildPixResponse(payment, payment.getTransactionAmount());

        } catch (MPApiException e) {
            throw e;
        } catch (MPException e) {
            throw e;
        } catch (NumberFormatException e) {
            throw new RuntimeException("TransactionId inválido: " + transactionId, e);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar dados PIX", e);
        }
    }

    private String limparCpf(String cpf) {
        if (cpf == null) return null;
        return cpf.replaceAll("[^0-9]", "");
    }
}