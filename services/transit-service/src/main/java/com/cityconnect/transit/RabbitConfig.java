package com.cityconnect.transit;

import com.cityconnect.common.EventContract;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    /** Exchange durável; declarado de forma idempotente (o consumidor declara o mesmo). */
    @Bean
    TopicExchange cityConnectExchange() {
        return new TopicExchange(EventContract.EXCHANGE, true, false);
    }

    /** Serializa eventos como JSON, reaproveitando o ObjectMapper do Spring Boot (datas ISO-8601). */
    @Bean
    MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
