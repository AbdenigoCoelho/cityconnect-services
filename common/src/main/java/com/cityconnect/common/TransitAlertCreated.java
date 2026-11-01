package com.cityconnect.common;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento publicado quando um alerta de trânsito é criado.
 *
 * <p>{@code eventId} permite deduplicação no consumidor (entrega at-least-once) e
 * {@code version} identifica a versão do contrato para evolução compatível.
 */
public record TransitAlertCreated(
        String eventId,
        int version,
        Instant occurredAt,
        String routeId,
        String message) {

    public static final int CURRENT_VERSION = 1;

    public static TransitAlertCreated of(String routeId, String message) {
        return new TransitAlertCreated(
                UUID.randomUUID().toString(), CURRENT_VERSION, Instant.now(), routeId, message);
    }
}
