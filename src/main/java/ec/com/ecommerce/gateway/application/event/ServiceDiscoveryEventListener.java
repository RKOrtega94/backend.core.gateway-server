package ec.com.ecommerce.gateway.application.event;

import ec.com.ecommerce.gateway.application.service.SwaggerAggregatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listener for service discovery events to automatically generate Swagger routes
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceDiscoveryEventListener {

    private final SwaggerAggregatorService swaggerAggregatorService;
    private boolean initialRouteGeneration = false;
    private int heartbeatCounter = 0;

    @EventListener(ContextRefreshedEvent.class)
    public void onContextRefreshed(ContextRefreshedEvent event) {
        if (!initialRouteGeneration) {
            log.info("Gateway context refreshed, generating initial Swagger routes");
            swaggerAggregatorService.generateAggregatedSwaggerRoutes();
            initialRouteGeneration = true;
        }
    }

    @EventListener(HeartbeatEvent.class)
    public void onHeartbeat(HeartbeatEvent event) {
        // Heartbeat events are frequent, so we'll check periodically
        // but not on every heartbeat to avoid performance issues
        if (shouldUpdateRoutes()) {
            log.debug("Checking for new services and updating Swagger routes");
            swaggerAggregatorService.generateAggregatedSwaggerRoutes();
        }
    }

    private boolean shouldUpdateRoutes() {
        // Update every 10th heartbeat
        heartbeatCounter++;
        if (heartbeatCounter >= 10) {
            heartbeatCounter = 0;
            return true;
        }
        return false;
    }
}