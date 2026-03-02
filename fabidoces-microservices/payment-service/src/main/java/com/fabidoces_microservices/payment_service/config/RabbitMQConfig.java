package com.fabidoces_microservices.payment_service.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.queues.client.query:clientQueryQueue}")
    private String clientQueryQueue;

    @Value("${app.rabbitmq.queues.client.response:clientResponseQueue}")
    private String clientResponseQueue;

    @Value("${app.rabbitmq.queues.order.created:orderCreatedQueue}")
    private String orderCreatedQueue;

    @Value("${app.rabbitmq.queues.order.updated:orderUpdatedQueue}")
    private String orderUpdatedQueue;

    @Value("${app.rabbitmq.queues.payment.webhook:paymentWebhookQueue}")
    private String paymentWebhookQueue;

    @Value("${app.rabbitmq.queues.email.order:orderEmailQueue}")
    private String orderEmailQueue;

    @Value("${app.rabbitmq.queues.logistics.request:logisticsRequestQueue}")
    private String logisticsRequestQueue;

    @Bean
    public Queue clientQueryQueue() {
        return new Queue(clientQueryQueue, true);
    }

    @Bean
    public Queue clientResponseQueue() {
        return new Queue(clientResponseQueue, true);
    }

    @Bean
    public Queue orderCreatedQueue() {
        return new Queue(orderCreatedQueue, true);
    }

    @Bean
    public Queue orderUpdatedQueue() {
        return new Queue(orderUpdatedQueue, true);
    }

    @Bean
    public Queue paymentWebhookQueue() {
        return new Queue(paymentWebhookQueue, true);
    }

    @Bean
    public Queue orderEmailQueue() {
        return new Queue(orderEmailQueue, true);
    }

    @Bean
    public Queue logisticsRequestQueue() {
        return new Queue(logisticsRequestQueue, true);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        rabbitTemplate.setChannelTransacted(true);
        rabbitTemplate.setReplyTimeout(30000);
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setUseDirectReplyToContainer(false);
        return rabbitTemplate;
    }
}