package com.fabidoces_microservices.client_service.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventConfig {

    @Value("${app.rabbitmq.queues.client.query:clientQueryQueue}")
    private String clientQueryQueue;

    @Value("${app.rabbitmq.queues.client.response:clientResponseQueue}")
    private String clientResponseQueue;

    @Value("${app.rabbitmq.queues.email.name:emailQueue}")
    private String emailQueue;

    @Bean
    public Queue clientQueryQueue() {
        return new Queue(clientQueryQueue, true);
    }

    @Bean
    public Queue clientResponseQueue() {
        return new Queue(clientResponseQueue, true);
    }

    @Bean
    public Queue emailQueue() {
        return new Queue(emailQueue, true);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        return converter;
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