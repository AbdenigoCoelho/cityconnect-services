package com.cityconnect.transit;

import com.cityconnect.common.EventContract;
import com.cityconnect.common.TransitAlertCreated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publicação simples para demonstração. Em produção: outbox transacional,
 * publisher confirms, retry e dead-letter queue (ver README).
 */
@Component
public class AlertPublisher {

    private static final Logger log = LoggerFactory.getLogger(AlertPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public AlertPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public TransitAlertCreated publish(String routeId, String message) {
        TransitAlertCreated event = TransitAlertCreated.of(routeId, message);
        rabbitTemplate.convertAndSend(
                EventContract.EXCHANGE, EventContract.TRANSIT_ALERT_ROUTING_KEY, event);
        log.info("Evento publicado eventId={} routeId={}", event.eventId(), event.routeId());
        return event;
    }
}
