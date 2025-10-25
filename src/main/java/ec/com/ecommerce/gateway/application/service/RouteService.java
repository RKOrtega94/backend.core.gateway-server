package ec.com.ecommerce.gateway.application.service;

import ec.com.ecommerce.gateway.adapter.persistence.RouteRepository;
import ec.com.ecommerce.gateway.domain.entity.RouteEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Save or update a route
     */
    public RouteEntity saveRoute(RouteEntity route) {
        log.info("Saving route: {}", route.getId());
        RouteEntity savedRoute = routeRepository.save(route);
        
        // Trigger route refresh
        refreshRoutes();
        
        return savedRoute;
    }

    /**
     * Get all enabled routes
     */
    public List<RouteEntity> getAllEnabledRoutes() {
        return routeRepository.findByEnabledTrue();
    }

    /**
     * Get routes by service name
     */
    public List<RouteEntity> getRoutesByServiceName(String serviceName) {
        return routeRepository.findByServiceName(serviceName);
    }

    /**
     * Get route by ID
     */
    public Optional<RouteEntity> getRouteById(String routeId) {
        return routeRepository.findById(routeId);
    }

    /**
     * Delete route by ID
     */
    public void deleteRoute(String routeId) {
        log.info("Deleting route: {}", routeId);
        routeRepository.deleteById(routeId);
        
        // Trigger route refresh
        refreshRoutes();
    }

    /**
     * Enable/disable route
     */
    public void toggleRoute(String routeId, boolean enabled) {
        Optional<RouteEntity> routeOpt = routeRepository.findById(routeId);
        if (routeOpt.isPresent()) {
            RouteEntity route = routeOpt.get();
            route.setEnabled(enabled);
            routeRepository.save(route);
            
            log.info("Route {} {}", routeId, enabled ? "enabled" : "disabled");
            
            // Trigger route refresh
            refreshRoutes();
        }
    }

    /**
     * Refresh gateway routes
     */
    public void refreshRoutes() {
        log.info("Triggering route refresh");
        eventPublisher.publishEvent(new RefreshRoutesEvent(this));
    }
}