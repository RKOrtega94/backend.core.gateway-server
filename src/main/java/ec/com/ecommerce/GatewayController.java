package ec.com.ecommerce;

import ec.com.ecommerce.gateway.adapter.persistence.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GatewayController {
    private final RouteRepository repository;

    @GetMapping("/routes/count")
    public Long getRoutesCount() {
        return repository.count();
    }

    @GetMapping("/routes")
    public Object getAllRoutes() {
        return repository.findAll();
    }
}
