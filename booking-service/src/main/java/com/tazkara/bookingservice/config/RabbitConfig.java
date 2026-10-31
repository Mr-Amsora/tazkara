package com.tazkara.bookingservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "tazkara.events";
    public static final String EVENT_CANCELLED_QUEUE = "booking.event-cancelled";
    public static final String EVENT_CANCELLED_KEY = "event.cancelled";

    @Bean
    public TopicExchange tazkaraExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue eventCancelledQueue() {
        return QueueBuilder.durable(EVENT_CANCELLED_QUEUE).build();
    }

    @Bean
    public Binding eventCancelledBinding(Queue eventCancelledQueue, TopicExchange tazkaraExchange) {
        return BindingBuilder.bind(eventCancelledQueue).to(tazkaraExchange).with(EVENT_CANCELLED_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setAlwaysConvertToInferredType(true);
        return converter;
    }
}