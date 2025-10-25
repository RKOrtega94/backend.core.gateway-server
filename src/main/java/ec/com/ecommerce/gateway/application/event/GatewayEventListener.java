package ec.com.ecommerce.gateway.application.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.com.ecommerce.gateway.adapter.persistence.RouteRepository;
import ec.com.ecommerce.gateway.domain.entity.RouteEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GatewayEventListener {

    private final ObjectMapper objectMapper;
    private final RouteRepository routeRepository;

    @KafkaListener(id = "gateway-listener", topics = "gateway-topic")
    public void listen(String message) {
        log.info("Received message: {}", message);
        try {
            GatewayRouteEvent event = objectMapper.readValue(message, GatewayRouteEvent.class);
            RouteEntity entity = RouteEntity.builder()
                    .id(event.getId())
                    .uri(event.getUri())
                    .predicates(event.getPredicates())
                    .filters(event.getFilters())
                    .build();
            routeRepository.save(entity);
            log.info("Saved route entity with id={} uri={}", entity.getId(), entity.getUri());
        } catch (Exception e) {
            log.error("Failed to process gateway event message", e);
        }
    }
}
