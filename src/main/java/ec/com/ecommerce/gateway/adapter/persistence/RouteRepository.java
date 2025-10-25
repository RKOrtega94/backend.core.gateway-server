package ec.com.ecommerce.gateway.adapter.persistence;

import ec.com.ecommerce.gateway.domain.entity.RouteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<RouteEntity, String> {
    
    /**
     * Find all enabled routes
     */
    List<RouteEntity> findByEnabledTrue();
    
    /**
     * Find routes by service name
     */
    List<RouteEntity> findByServiceName(String serviceName);
    
    /**
     * Find enabled routes by service name
     */
    List<RouteEntity> findByServiceNameAndEnabledTrue(String serviceName);
    
    /**
     * Delete routes by service name
     */
    void deleteByServiceName(String serviceName);
}
