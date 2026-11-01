package com.cityconnect.notification;

import com.cityconnect.common.EventContract;
import com.cityconnect.common.TransitAlertCreated;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TransitAlertListener {

    private static final Logger log = LoggerFactory.getLogger(TransitAlertListener.class);

    private final NotificationChannel channel;

    /**
     * Deduplicação por eventId (a entrega é at-least-once). Em memória apenas para o
     * protótipo; em produção use armazenamento durável com expiração.
     */
    private final Set<String> processed = ConcurrentHashMap.newKeySet();

    public TransitAlertListener(NotificationChannel channel) {
        this.channel = channel;
    }

    @RabbitListener(queues = EventContract.NOTIFICATIONS_TRANSIT_ALERTS_QUEUE)
    public void onTransitAlert(TransitAlertCreated event) {
        if (processed.contains(event.eventId())) {
            log.warn("Evento duplicado ignorado eventId={}", event.eventId());
            return;
        }

        // Se o envio lançar exceção, o evento NÃO é marcado como processado: o retry do
        // Spring AMQP tenta de novo e, esgotadas as tentativas, a mensagem vai para a DLQ.
        channel.send(event);

        // Marca somente depois do sucesso.
        processed.add(event.eventId());
    }
}
