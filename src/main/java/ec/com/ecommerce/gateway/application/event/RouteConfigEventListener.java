package ec.com.ecommerce.gateway.application.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.com.ecommerce.gateway.application.service.RouteService;
import ec.com.ecommerce.gateway.domain.entity.RouteEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RouteConfigEventListener {

    private final ObjectMapper objectMapper;
    private final RouteService routeService;

    @Value("${gateway.route.filter.ignore-webjars:true}")
    private boolean ignoreWebjars;

    @Value("${gateway.route.filter.ignore-empty-predicates:true}")
    private boolean ignoreEmptyPredicates;

    @Value("${gateway.route.filter.ignored-paths:/webjars/**,/swagger-resources/**}")
    private String ignoredPaths;

    @KafkaListener(id = "route-config-listener", topics = "gateway-route-config")
    public void listen(String message) {
        log.info("Received route configuration message: {}", message);
        try {
            RouteConfigMessage routeConfig = objectMapper.readValue(message, RouteConfigMessage.class);
            
            // Filter out routes with empty predicates if configured
            if (ignoreEmptyPredicates && (routeConfig.getPredicates() == null || routeConfig.getPredicates().isEmpty())) {
                log.debug("Ignoring route {} - empty predicates (filter enabled)", routeConfig.getRouteId());
                return;
            }
            
            // Filter out routes with ignored paths (like webjars)
            if (shouldIgnoreRoute(routeConfig)) {
                return;
            }
            
            // Filter out routes with empty predicate values if configured
            if (ignoreEmptyPredicates) {
                boolean hasEmptyPredicates = routeConfig.getPredicates().stream()
                        .anyMatch(predicate -> predicate == null || predicate.trim().isEmpty());
                
                if (hasEmptyPredicates) {
                    log.debug("Ignoring route {} - contains empty predicate values: {}", 
                            routeConfig.getRouteId(), routeConfig.getPredicates());
                    return;
                }
            }
            
            // Convert RouteConfigMessage to RouteEntity
            RouteEntity entity = RouteEntity.builder()
                    .id(routeConfig.getRouteId())
                    .uri(routeConfig.getUri())
                    .predicates(String.join(",", routeConfig.getPredicates() != null ? routeConfig.getPredicates() : List.of()))
                    .filters(String.join(",", routeConfig.getFilters() != null ? routeConfig.getFilters() : List.of()))
                    .orderNum(routeConfig.getOrderNum())
                    .description(routeConfig.getDescription())
                    .enabled(routeConfig.getEnabled())
                    .serviceName(routeConfig.getServiceName())
                    .build();
            
            // Save the route using the service (which will trigger refresh)
            routeService.saveRoute(entity);
            log.info("Processed route configuration: {} for service {}", entity.getId(), entity.getServiceName());
            
        } catch (Exception e) {
            log.error("Failed to process route configuration message: {}", message, e);
        }
    }

    /**
     * Check if route should be ignored based on configured ignored paths
     */
    private boolean shouldIgnoreRoute(RouteConfigMessage routeConfig) {
        if (!ignoreWebjars && ignoredPaths == null) {
            return false;
        }
        
        List<String> pathsToIgnore = Arrays.asList(ignoredPaths.split(","));
        
        for (String predicate : routeConfig.getPredicates()) {
            if (predicate == null) continue;
            
            for (String ignoredPath : pathsToIgnore) {
                String cleanPath = ignoredPath.trim();
                if (predicate.contains(cleanPath)) {
                    log.debug("Ignoring route {} - matches ignored path '{}' in predicate: {}", 
                            routeConfig.getRouteId(), cleanPath, predicate);
                    return true;
                }
            }
        }
        
        return false;
    }
}