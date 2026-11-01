package com.cityconnect.transit;

import com.cityconnect.common.TransitAlertCreated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transit")
public class TransitController {

    private static final Logger log = LoggerFactory.getLogger(TransitController.class);
    private static final String SERVED_BY = "X-Served-By";
    private static final List<String> STOPS = List.of("Terminal Central", "Praça da Matriz", "Hospital Municipal");

    private final AlertPublisher publisher;
    private final String instanceName;

    public TransitController(AlertPublisher publisher, @Value("${instance.name}") String instanceName) {
        this.publisher = publisher;
        this.instanceName = instanceName;
    }

    public record Arrival(String stop, int etaMinutes) {
    }

    public record ArrivalsResponse(String routeId, Instant generatedAt, List<Arrival> arrivals, String servedBy) {
    }

    public record CreateAlertRequest(
            @NotBlank String routeId,
            @NotBlank @Size(max = 280) String message) {
    }

    public record AlertAccepted(String eventId, int version, String routeId, String servedBy) {
    }

    /** Dados demonstrativos e determinísticos por rota (integração externa simulada). */
    @GetMapping("/routes/{routeId}/arrivals")
    public ResponseEntity<ArrivalsResponse> arrivals(@PathVariable String routeId) {
        int seed = routeId.hashCode();
        List<Arrival> arrivals = java.util.stream.IntStream.range(0, STOPS.size())
                .mapToObj(i -> new Arrival(STOPS.get(i), 3 + Math.floorMod(seed + i * 7, 12)))
                .toList();
        log.info("arrivals routeId={} servedBy={}", routeId, instanceName);
        return ResponseEntity.ok()
                .header(SERVED_BY, instanceName)
                .body(new ArrivalsResponse(routeId, Instant.now(), arrivals, instanceName));
    }

    @PostMapping("/alerts")
    public ResponseEntity<AlertAccepted> createAlert(@Valid @RequestBody CreateAlertRequest request) {
        TransitAlertCreated event = publisher.publish(request.routeId(), request.message());
        log.info("alert accepted routeId={} servedBy={}", request.routeId(), instanceName);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .header(SERVED_BY, instanceName)
                .body(new AlertAccepted(event.eventId(), event.version(), event.routeId(), instanceName));
    }
}
