package com.tazkara.bookingservice.messaging;

import com.tazkara.bookingservice.config.RabbitConfig;
import com.tazkara.bookingservice.dto.EventCancelledMessage;
import com.tazkara.bookingservice.service.EventCancellationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventCancelledListener {

    private final EventCancellationService cancellationService;

    @RabbitListener(queues = RabbitConfig.EVENT_CANCELLED_QUEUE)
    public void handle(EventCancelledMessage message) {
        log.info("Received event cancelled message for event {}", message.eventId());
        cancellationService.cancelBookingsOfEvent(message.eventId());
    }
}