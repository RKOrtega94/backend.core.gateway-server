package ec.com.ecommerce.gateway.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.com.ecommerce.gateway.adapter.persistence.RouteRepository;
import ec.com.ecommerce.gateway.application.event.RouteConfigMessage;
import ec.com.ecommerce.gateway.domain.entity.RouteEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service to manage Swagger/OpenAPI documentation aggregation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SwaggerAggregatorService {

    private final DiscoveryClient discoveryClient;
    private final RouteRepository routeRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final RouteService routeService;

    /**
     * Generate aggregated Swagger routes for all discovered services
     */
    public void generateAggregatedSwaggerRoutes() {
        try {
            log.info("Generating aggregated Swagger routes for discovered services");
            
            List<String> services = discoveryClient.getServices().stream()
                    .filter(service -> !service.equalsIgnoreCase("gateway-server"))
                    .filter(service -> !service.equalsIgnoreCase("discovery-server"))
                    .filter(service -> !service.equalsIgnoreCase("config-server"))
                    .collect(Collectors.toList());

            log.info("Found services for Swagger aggregation: {}", services);

            // Generate main Swagger aggregator route
            generateMainSwaggerRoute();
            
            // Generate individual service documentation routes
            for (String serviceName : services) {
                generateServiceSwaggerRoutes(serviceName);
            }

        } catch (Exception e) {
            log.error("Error generating aggregated Swagger routes", e);
        }
    }

    /**
     * Generate main Swagger aggregator route that lists all services
     */
    private void generateMainSwaggerRoute() {
        RouteEntity mainSwaggerRoute = RouteEntity.builder()
                .id("gateway-swagger-aggregator")
                .uri("http://localhost:8080") // Gateway's own URL
                .predicates("Path=/swagger-ui.html,Path=/swagger-ui/**,Path=/swagger-ui")
                .filters("RewritePath=/swagger-ui.*,/swagger-aggregator")
                .orderNum(1)
                .description("Main Swagger UI aggregator for all services")
                .enabled(true)
                .serviceName("gateway")
                .build();

        routeService.saveRoute(mainSwaggerRoute);
        log.info("Generated main Swagger aggregator route");
    }

    /**
     * Generate Swagger routes for a specific service
     */
    private void generateServiceSwaggerRoutes(String serviceName) {
        String cleanServiceName = serviceName.replaceAll("(?i)[-_]service$", "");
        
        // Route for service's Swagger UI (accessible via /docs/{service})
        RouteEntity serviceSwaggerUI = RouteEntity.builder()
                .id("swagger-" + cleanServiceName + "-ui")
                .uri("lb://" + serviceName)
                .predicates("Path=/docs/" + cleanServiceName + "/swagger-ui/**")
                .filters("StripPrefix=2")
                .orderNum(10)
                .description("Swagger UI for " + cleanServiceName + " service")
                .enabled(true)
                .serviceName(cleanServiceName)
                .build();

        routeService.saveRoute(serviceSwaggerUI);

        // Route for service's OpenAPI docs (accessible via /docs/{service}/api-docs)
        RouteEntity serviceApiDocs = RouteEntity.builder()
                .id("swagger-" + cleanServiceName + "-api-docs")
                .uri("lb://" + serviceName)
                .predicates("Path=/docs/" + cleanServiceName + "/v3/api-docs/**")
                .filters("StripPrefix=2")
                .orderNum(11)
                .description("OpenAPI docs for " + cleanServiceName + " service")
                .enabled(true)
                .serviceName(cleanServiceName)
                .build();

        routeService.saveRoute(serviceApiDocs);

        // Route for direct access to service's own Swagger UI
        RouteEntity directSwaggerUI = RouteEntity.builder()
                .id("direct-swagger-" + cleanServiceName)
                .uri("lb://" + serviceName)
                .predicates("Path=/" + cleanServiceName + "/swagger-ui/**")
                .filters("StripPrefix=1")
                .orderNum(12)
                .description("Direct Swagger UI access for " + cleanServiceName + " service")
                .enabled(true)
                .serviceName(cleanServiceName)
                .build();

        routeService.saveRoute(directSwaggerUI);

        log.info("Generated Swagger routes for service: {}", cleanServiceName);
    }

    /**
     * Generate Swagger configuration message and send to Kafka
     */
    public void publishSwaggerRoute(String serviceName, String routePath, String description) {
        try {
            RouteConfigMessage swaggerRoute = RouteConfigMessage.builder()
                    .routeId("swagger-" + serviceName + "-" + routePath.hashCode())
                    .uri("lb://" + serviceName + "-service")
                    .predicates(Arrays.asList("Path=" + routePath))
                    .filters(Arrays.asList("StripPrefix=0"))
                    .orderNum(5)
                    .description(description)
                    .enabled(true)
                    .serviceName(serviceName)
                    .build();

            String jsonMessage = objectMapper.writeValueAsString(swaggerRoute);
            kafkaTemplate.send("gateway-route-config", serviceName, jsonMessage);
            log.info("Published Swagger route: {} for service: {}", routePath, serviceName);

        } catch (Exception e) {
            log.error("Error publishing Swagger route for service: {}", serviceName, e);
        }
    }

    /**
     * Get list of services with Swagger documentation
     */
    public List<String> getServicesWithSwagger() {
        return routeRepository.findAll().stream()
                .filter(route -> route.getId().contains("swagger"))
                .map(RouteEntity::getServiceName)
                .distinct()
                .collect(Collectors.toList());
    }
}