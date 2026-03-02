package com.fabidoces_microservices.payment_service.events;

import ch.qos.logback.core.net.server.Client;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.cache.spi.support.AbstractReadWriteAccess;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class URequestEvent {
    @JsonFormat(pattern = "dd/MM/yyyy, HH:mm:ss")
    private LocalDateTime dateTime;
    private List<AbstractReadWriteAccess.Item> items;
    private Client client;
    private double price;
}
