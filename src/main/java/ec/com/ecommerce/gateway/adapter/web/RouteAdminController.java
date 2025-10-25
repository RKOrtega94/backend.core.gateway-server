package ec.com.ecommerce.gateway.adapter.web;

import ec.com.ecommerce.gateway.application.service.RouteService;
import ec.com.ecommerce.gateway.domain.entity.RouteEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/admin/routes")
@RequiredArgsConstructor
public class RouteAdminController {

    private final RouteService routeService;

    @GetMapping
    public ResponseEntity<List<RouteEntity>> getAllRoutes() {
        List<RouteEntity> routes = routeService.getAllEnabledRoutes();
        return ResponseEntity.ok(routes);
    }

    @GetMapping("/{routeId}")
    public ResponseEntity<RouteEntity> getRoute(@PathVariable String routeId) {
        Optional<RouteEntity> route = routeService.getRouteById(routeId);
        return route.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/service/{serviceName}")
    public ResponseEntity<List<RouteEntity>> getRoutesByService(@PathVariable String serviceName) {
        List<RouteEntity> routes = routeService.getRoutesByServiceName(serviceName);
        return ResponseEntity.ok(routes);
    }

    @PutMapping("/{routeId}/toggle")
    public ResponseEntity<Void> toggleRoute(@PathVariable String routeId, @RequestParam boolean enabled) {
        routeService.toggleRoute(routeId, enabled);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{routeId}")
    public ResponseEntity<Void> deleteRoute(@PathVariable String routeId) {
        routeService.deleteRoute(routeId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshRoutes() {
        routeService.refreshRoutes();
        return ResponseEntity.ok().build();
    }
}