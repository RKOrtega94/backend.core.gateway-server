package ec.com.ecommerce.gateway.adapter.web;

import ec.com.ecommerce.gateway.adapter.persistence.RouteRepository;
import ec.com.ecommerce.gateway.domain.entity.RouteEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/diagnostics")
@RequiredArgsConstructor
public class DiagnosticsController {

    private final RouteRepository routeRepository;
    private final RouteDefinitionRepository routeDefinitionRepository;
    private final DiscoveryClient discoveryClient;

    @GetMapping("/gateway-status")
    public ResponseEntity<Map<String, Object>> getGatewayStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // Database routes
            List<RouteEntity> dbRoutes = routeRepository.findAll();
            status.put("database_routes_count", dbRoutes.size());
            status.put("database_routes", dbRoutes);
            
            // Gateway routes
            Flux<RouteDefinition> gatewayRoutes = routeDefinitionRepository.getRouteDefinitions();
            List<RouteDefinition> routeList = gatewayRoutes.collectList().block();
            status.put("gateway_routes_count", routeList != null ? routeList.size() : 0);
            status.put("gateway_routes", routeList);
            
            // Discovery services
            List<String> services = discoveryClient.getServices();
            status.put("discovery_services", services);
            status.put("discovery_services_count", services.size());
            
            status.put("status", "OK");
            
        } catch (Exception e) {
            log.error("Error generating diagnostics", e);
            status.put("status", "ERROR");
            status.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(status);
    }

    @GetMapping("/test-route")
    public ResponseEntity<Map<String, Object>> testRoute() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Create a test route directly
            RouteEntity testRoute = RouteEntity.builder()
                    .id("test-route-" + System.currentTimeMillis())
                    .uri("lb://global-service")
                    .predicates("Path=/test/**")
                    .filters("StripPrefix=0")
                    .orderNum(999)
                    .description("Test route created by diagnostics")
                    .enabled(true)
                    .serviceName("test")
                    .build();
            
            routeRepository.save(testRoute);
            result.put("status", "SUCCESS");
            result.put("message", "Test route created");
            result.put("route", testRoute);
            
        } catch (Exception e) {
            log.error("Error creating test route", e);
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
}