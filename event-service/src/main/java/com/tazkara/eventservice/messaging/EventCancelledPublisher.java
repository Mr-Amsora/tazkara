package com.tazkara.eventservice.messaging;

import com.tazkara.eventservice.config.RabbitConfig;
import com.tazkara.eventservice.dto.EventCancelledEvent;
import com.tazkara.eventservice.dto.EventCancelledMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventCancelledPublisher {

    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(EventCancelledEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitConfig.EXCHANGE,
                    RabbitConfig.EVENT_CANCELLED_KEY,
                    new EventCancelledMessage(event.eventId()));
            log.info("Published event cancelled message for event {}", event.eventId());
        } catch (AmqpException e) {
            log.error("Could not publish event cancelled message for event {}: {}",
                    event.eventId(), e.getMessage());
        }
    }
}