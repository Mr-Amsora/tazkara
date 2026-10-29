package com.tazkara.bookingservice.client;

import com.tazkara.bookingservice.exception.BookingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

@Component
@Slf4j
public class EventClient {

    private final RestClient restClient;

    public EventClient(RestClient.Builder builder,
                       @Value("${app.event-service.url}") String baseUrl,
                       @Value("${app.internal.api-key}") String internalKey) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(2000);
        requestFactory.setReadTimeout(5000);

        this.restClient = builder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader("X-Internal-Key", internalKey)
                .build();
    }

    public EventInfo getEvent(Long eventId) {
        return call(() -> restClient.get()
                .uri("/api/internal/events/{id}", eventId)
                .retrieve()
                .onStatus(status -> status.value() == 404, (req, res) -> {
                    throw new BookingException(HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND", "Event not found");
                })
                .body(EventInfo.class));
    }

    public List<ReservedLine> reserve(Long eventId, List<Line> lines) {
        return call(() -> restClient.post()
                .uri("/api/internal/events/{id}/reserve", eventId)
                .body(new LinesRequest(lines))
                .retrieve()
                .onStatus(status -> status.value() == 409, (req, res) -> {
                    throw new BookingException(HttpStatus.CONFLICT, "TICKETS_UNAVAILABLE",
                            "Not enough tickets available");
                })
                .onStatus(status -> status.value() == 422, (req, res) -> {
                    throw new BookingException(HttpStatus.UNPROCESSABLE_ENTITY, "RESERVATION_REJECTED",
                            "The tickets could not be reserved for this event");
                })
                .body(new ParameterizedTypeReference<List<ReservedLine>>() {}));
    }

    public boolean release(Long eventId, List<Line> lines) {
        try {
            restClient.post()
                    .uri("/api/internal/events/{id}/release", eventId)
                    .body(new LinesRequest(lines))
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RestClientException e) {
            log.error("Could not release tickets of event {}: {}", eventId, e.getMessage());
            return false;
        }
    }

    private <T> T call(Supplier<T> request) {
        try {
            return request.get();
        } catch (RestClientException e) {
            log.error("Event service call failed: {}", e.getMessage());
            throw new BookingException(HttpStatus.SERVICE_UNAVAILABLE, "EVENT_SERVICE_UNAVAILABLE",
                    "Event service is not available, please try again later");
        }
    }

    public record EventInfo(Long id, Long organizerId, String title, String status,
                            Instant startTime, List<TicketTypeInfo> ticketTypes) {}

    public record TicketTypeInfo(Long id, String name, BigDecimal price, int quantity) {}

    public record Line(Long ticketTypeId, int quantity) {}

    public record ReservedLine(Long ticketTypeId, String ticketTypeName, BigDecimal unitPrice, int quantity) {}

    public record LinesRequest(List<Line> lines) {}
}