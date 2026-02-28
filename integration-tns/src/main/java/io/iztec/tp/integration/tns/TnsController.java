package io.iztec.tp.integration.tns;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iztec.tp.commons.core.dto.ApiResponse;
import io.iztec.tp.commons.database.entity.IntegrationEvent;
import io.iztec.tp.commons.database.repository.IntegrationEventRepository;
import io.iztec.tp.integration.tns.dto.TnsNotificationRequest;
import io.iztec.tp.integration.tns.dto.TnsNotificationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/tns/notifications")
@RequiredArgsConstructor
public class TnsController {

    private static final String INTEGRATION_NAME = "tns";

    private final IntegrationEventRepository eventRepository;
    private final ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<IntegrationEvent>>> listNotifications() {
        List<IntegrationEvent> events = eventRepository.findByIntegrationName(INTEGRATION_NAME);
        return ResponseEntity.ok(ApiResponse.ok(events));
    }

    @PostMapping
    @SneakyThrows
    public ResponseEntity<ApiResponse<TnsNotificationResponse>> sendNotification(
            @Valid @RequestBody TnsNotificationRequest request) {

        Instant now = Instant.now();

        IntegrationEvent event = new IntegrationEvent();
        event.setIntegrationName(INTEGRATION_NAME);
        event.setEventType("NOTIFICATION_SENT");
        event.setPayload(objectMapper.writeValueAsString(request));
        event.setOccurredAt(now);

        IntegrationEvent saved = eventRepository.save(event);

        TnsNotificationResponse response = new TnsNotificationResponse(
                saved.getId(),
                request.recipient(),
                "SENT",
                now
        );

        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}

