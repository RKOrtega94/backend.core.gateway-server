package ec.com.ecommerce.gateway.config;

import ec.com.ecommerce.gateway.adapter.persistence.RouteRepository;
import ec.com.ecommerce.gateway.domain.entity.RouteEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Initialize sample routes for testing when no routes exist
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RouteDataInitializer implements CommandLineRunner {

    private final RouteRepository routeRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking if routes need to be initialized...");
        
        long routeCount = routeRepository.count();
        log.info("Found {} existing routes in database", routeCount);
        
        if (routeCount == 0) {
            log.info("No routes found, initializing with sample route for global-service");
            initializeSampleRoutes();
        } else {
            log.info("Routes already exist, skipping initialization");
        }
    }

    private void initializeSampleRoutes() {
        // Create a sample route for global-service countries endpoint
        RouteEntity countriesRoute = RouteEntity.builder()
                .id("global-countries-manual")
                .uri("lb://global-service")
                .predicates("Path=/api/v1/countries/**")
                .filters("StripPrefix=0")
                .orderNum(100)
                .description("Manual route for global service countries endpoint")
                .enabled(true)
                .serviceName("global")
                .build();

        routeRepository.save(countriesRoute);
        log.info("Created sample route: {}", countriesRoute.getId());

        // Create route for the admin endpoints
        RouteEntity adminRoute = RouteEntity.builder()
                .id("gateway-admin-routes")
                .uri("http://localhost:8080")
                .predicates("Path=/admin/**")
                .filters("StripPrefix=0")
                .orderNum(1)
                .description("Gateway admin routes")
                .enabled(true)
                .serviceName("gateway")
                .build();

        routeRepository.save(adminRoute);
        log.info("Created admin route: {}", adminRoute.getId());
    }
}