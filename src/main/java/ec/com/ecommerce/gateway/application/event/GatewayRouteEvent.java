package ec.com.ecommerce.gateway.application.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GatewayRouteEvent {
    private String id;
    private String uri;
    private String predicates;
    private String filters;
}

