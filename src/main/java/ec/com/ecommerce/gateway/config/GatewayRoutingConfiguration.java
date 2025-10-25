package ec.com.ecommerce.gateway.config;

import ec.com.ecommerce.gateway.adapter.persistence.DatabaseRouteDefinitionRepository;
import ec.com.ecommerce.gateway.adapter.persistence.RouteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration for Gateway routing - Use database as PRIMARY route source
 */
@Slf4j
@Configuration
public class GatewayRoutingConfiguration {

    /**
     * Our custom route definition repository (NOT marked as Primary)
     */
    @Bean("databaseRouteDefinitionRepository")
    public RouteDefinitionRepository databaseRouteDefinitionRepository(RouteRepository repository) {
        log.info("Creating DatabaseRouteDefinitionRepository");
        return new DatabaseRouteDefinitionRepository(repository);
    }

    /**
     * Our PRIMARY route definition locator - this is what Gateway really needs
     * This will override all the competing @Primary beans
     */
    @Bean
    @Primary
    public RouteDefinitionLocator routeDefinitionLocator(RouteDefinitionRepository databaseRouteDefinitionRepository) {
        log.info("Creating PRIMARY RouteDefinitionLocator using database repository");
        // Simply delegate to our database repository
        return databaseRouteDefinitionRepository::getRouteDefinitions;
    }
}