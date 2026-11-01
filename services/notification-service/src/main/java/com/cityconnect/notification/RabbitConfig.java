package com.cityconnect.notification;

import com.cityconnect.common.EventContract;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Os métodos @Bean são chamados pelo Spring, não pelo código: silencia o falso positivo "never used".
@SuppressWarnings("unused")
@Configuration
public class RabbitConfig {

    /** Exchange das mensagens que esgotaram as tentativas (dead-letter exchange). */
    static final String DEAD_LETTER_EXCHANGE = "cityconnect.events.dlx";

    /** Fila onde ficam guardadas, para análise, as mensagens que falharam definitivamente. */
    static final String TRANSIT_ALERTS_DLQ = EventContract.NOTIFICATIONS_TRANSIT_ALERTS_QUEUE + ".dlq";

    @Bean
    TopicExchange cityConnectExchange() {
        return new TopicExchange(EventContract.EXCHANGE, true, false);
    }

    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false);
    }

    /**
     * Fila durável: as mensagens sobrevivem enquanto este serviço estiver parado.
     * Mensagens rejeitadas sem requeue (após o retry) seguem para a dead-letter exchange.
     *
     * <p>Atenção: o RabbitMQ não permite alterar os argumentos de uma fila já existente
     * (erro PRECONDITION_FAILED). Ao aplicar esta versão pela primeira vez, apague a fila
     * antiga "notifications.transit-alerts" (painel do RabbitMQ) ou use "docker compose down -v".
     */
    @Bean
    Queue transitAlertsQueue() {
        return QueueBuilder.durable(EventContract.NOTIFICATIONS_TRANSIT_ALERTS_QUEUE)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(TRANSIT_ALERTS_DLQ)
                .build();
    }

    @Bean
    Queue transitAlertsDlq() {
        return QueueBuilder.durable(TRANSIT_ALERTS_DLQ).build();
    }

    @Bean
    Binding transitAlertsBinding(Queue transitAlertsQueue, TopicExchange cityConnectExchange) {
        return BindingBuilder.bind(transitAlertsQueue)
                .to(cityConnectExchange)
                .with(EventContract.TRANSIT_ALERT_ROUTING_KEY);
    }

    @Bean
    Binding transitAlertsDlqBinding(Queue transitAlertsDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(transitAlertsDlq)
                .to(deadLetterExchange)
                .with(TRANSIT_ALERTS_DLQ);
    }

    @Bean
    MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
