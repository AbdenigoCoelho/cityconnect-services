package com.cityconnect.common;

/** Nomes de exchange, fila e routing keys compartilhados entre produtor e consumidores. */
public final class EventContract {

    public static final String EXCHANGE = "cityconnect.events";
    public static final String TRANSIT_ALERT_ROUTING_KEY = "transit.alert.created";
    public static final String NOTIFICATIONS_TRANSIT_ALERTS_QUEUE = "notifications.transit-alerts";

    private EventContract() {
    }
}
