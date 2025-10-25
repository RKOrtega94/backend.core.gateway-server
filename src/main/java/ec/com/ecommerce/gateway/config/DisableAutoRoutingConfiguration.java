package ec.com.ecommerce.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.discovery.DiscoveryClientRouteDefinitionLocator;
import org.springframework.cloud.gateway.config.PropertiesRouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Disable unused route locators to avoid conflicts
 */
@Slf4j
@Configuration
public class DisableAutoRoutingConfiguration {

    /**
     * Disable discovery client route locator
     */
    @Bean
    @ConditionalOnProperty(name = "spring.cloud.gateway.discovery.locator.enabled", havingValue = "false", matchIfMissing = true)
    public String disableDiscoveryRouting() {
        log.info("Discovery-based routing is DISABLED - using database routes only");
        return "disabled";
    }
}