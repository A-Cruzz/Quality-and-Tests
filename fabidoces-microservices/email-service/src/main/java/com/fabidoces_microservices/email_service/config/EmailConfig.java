package com.fabidoces_microservices.email_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailConfig {

    @Value("${app.rabbitmq.queues.email:email-queue}")
    private String emailQueueName;

    @Value("${app.rabbitmq.exchanges.email:email-exchange}")
    private String emailExchangeName;

    @Value("${app.rabbitmq.routing-keys.email:email-routing-key}")
    private String emailRoutingKey;

    // Fila
    @Bean
    public Queue emailQueue() {
        return new Queue(emailQueueName, true, false, false);
    }

    // Exchange
    @Bean
    public DirectExchange emailExchange() {
        return new DirectExchange(emailExchangeName, true, false);
    }

    // Binding
    @Bean
    public Binding emailBinding() {
        return BindingBuilder.bind(emailQueue())
                .to(emailExchange())
                .with(emailRoutingKey);
    }

    // Declarables para criar tudo automaticamente
    @Bean
    public Declarables declarables() {
        return new Declarables(
                emailQueue(),
                emailExchange(),
                emailBinding()
        );
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        rabbitTemplate.setExchange(emailExchangeName);
        rabbitTemplate.setRoutingKey(emailRoutingKey);
        return rabbitTemplate;
    }
}
