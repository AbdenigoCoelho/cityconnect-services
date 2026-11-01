package com.cityconnect.notification;

import com.cityconnect.common.TransitAlertCreated;

/**
 * Canal de entrega de notificações (log, e-mail, SMS, push...).
 *
 * <p>Contrato: lançar exceção quando a entrega falhar. O listener não marca o evento como
 * processado nesse caso, e o Spring AMQP faz retry e, esgotadas as tentativas, envia a
 * mensagem para a dead-letter queue.
 */
public interface NotificationChannel {

    void send(TransitAlertCreated event);
}
