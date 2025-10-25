package ec.com.ecommerce.gateway.application.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GatewayRouteScanner {

    private final DiscoveryClient discoveryClient;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @EventListener(ContextRefreshedEvent.class)
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            List<String> services = discoveryClient.getServices();
            log.info("Discovered services: {}", services);
            for (String serviceId : services) {
                try {
                    GatewayRouteEvent routeEvent = new GatewayRouteEvent(
                            serviceId,
                            "lb://" + serviceId,
                            "[{\"name\":\"Path\",\"args\":{\"pattern\":\"/" + serviceId + "/**\"}}]",
                            "[]"
                    );
                    String payload = objectMapper.writeValueAsString(routeEvent);
                    kafkaTemplate.send("gateway-topic", serviceId, payload);
                    log.info("Published gateway-route event for service {}", serviceId);
                } catch (Exception e) {
                    log.error("Failed to publish route event for service {}", serviceId, e);
                }
            }
        } catch (Exception e) {
            log.error("Failed to scan services and publish gateway events", e);
        }
    }
}

