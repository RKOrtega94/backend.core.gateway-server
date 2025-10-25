package ec.com.ecommerce.gateway.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "routes")
public class RouteEntity {
    @Id
    private String id;
    
    @Column(nullable = false)
    private String uri;
    
    @Column(length = 1000)
    private String predicates;
    
    @Column(length = 1000)
    private String filters;
    
    @Column(name = "order_num")
    private Integer orderNum;
    
    @Column(length = 500)
    private String description;
    
    @Column
    private Boolean enabled;
    
    @Column(name = "service_name")
    private String serviceName;
}

