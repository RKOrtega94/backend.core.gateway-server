package ec.com.ecommerce.gateway.adapter.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * Controller to handle Swagger UI access on the gateway
 */
@Slf4j
@Controller
@RequestMapping("/swagger-ui")
public class SwaggerUIController {

    /**
     * Redirect swagger-ui/index.html to the swagger aggregator
     */
    @GetMapping({"/index.html", "/"})
    public Mono<Void> redirectToSwaggerAggregator(ServerHttpResponse response) {
        log.info("Redirecting Swagger UI access to aggregator");
        response.setStatusCode(HttpStatus.FOUND);
        response.getHeaders().setLocation(URI.create("/swagger-aggregator"));
        return response.setComplete();
    }

    /**
     * Handle swagger-ui/** paths and redirect to aggregator
     */
    @GetMapping("/**")
    public Mono<Void> redirectSwaggerUIToAggregator(ServerHttpResponse response) {
        log.info("Redirecting Swagger UI path to aggregator");
        response.setStatusCode(HttpStatus.FOUND);
        response.getHeaders().setLocation(URI.create("/swagger-aggregator"));
        return response.setComplete();
    }
}