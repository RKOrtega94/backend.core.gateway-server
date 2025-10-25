package ec.com.ecommerce.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.net.URI;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

/**
 * WebFlux configuration for Swagger UI and redirects
 */
@Configuration
public class SwaggerWebConfig {

    @Bean
    public RouterFunction<ServerResponse> swaggerRouterFunction() {
        return RouterFunctions
                // Redirect root paths to swagger aggregator
                .route(GET("/"), request -> 
                    ServerResponse.temporaryRedirect(URI.create("/swagger-aggregator")).build())
                .andRoute(GET("/docs"), request -> 
                    ServerResponse.temporaryRedirect(URI.create("/swagger-aggregator")).build())
                .andRoute(GET("/swagger-ui.html"), request -> 
                    ServerResponse.temporaryRedirect(URI.create("/swagger-aggregator")).build());
    }
}