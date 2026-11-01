package com.cityconnect.notification;

import com.cityconnect.common.TransitAlertCreated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Canal de demonstração: apenas registra a notificação no log. */
@Component
public class LogNotificationChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(LogNotificationChannel.class);

    @Override
    public void send(TransitAlertCreated event) {
        log.info("NOTIFICAÇÃO linha={} mensagem='{}' (eventId={}, v{}, ocorrido em {})",
                event.routeId(), event.message(), event.eventId(), event.version(), event.occurredAt());
    }
}
