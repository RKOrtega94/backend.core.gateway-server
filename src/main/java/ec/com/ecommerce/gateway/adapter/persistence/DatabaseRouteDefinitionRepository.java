package ec.com.ecommerce.gateway.adapter.persistence;

import ec.com.ecommerce.gateway.domain.entity.RouteEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public record DatabaseRouteDefinitionRepository(RouteRepository repository) implements RouteDefinitionRepository {
    private static final String SWAGGER_AGGREGATOR_PATH = "/swagger-aggregator";

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        log.debug("Loading route definitions from database");
        return Flux.defer(() -> {
            try {
                List<RouteEntity> entities = repository.findAll();
                log.info("Found {} route entities in database", entities.size());

                return Flux.fromIterable(entities).filter(entity -> entity.getEnabled() != null && entity.getEnabled()).map(this::convertToRouteDefinition).doOnNext(route -> log.debug("Loaded route: {} -> {}", route.getId(), route.getUri()));
            } catch (Exception e) {
                log.error("Error loading routes from database", e);
                return Flux.empty();
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private RouteDefinition convertToRouteDefinition(RouteEntity entity) {
        try {
            RouteDefinition rd = new RouteDefinition();
            rd.setId(entity.getId());

            // Set URI
            if (entity.getUri() != null) {
                rd.setUri(URI.create(entity.getUri()));
            }

            // Set predicates
            if (entity.getPredicates() != null && !entity.getPredicates().isEmpty()) {
                List<PredicateDefinition> predicates = parsePredicates(entity.getPredicates());
                rd.setPredicates(predicates);
                log.debug("Set predicates for route {}: {}", entity.getId(), predicates);
            }

            // Set filters
            if (entity.getFilters() != null && !entity.getFilters().isEmpty()) {
                List<FilterDefinition> filters = parseFilters(entity.getFilters());
                rd.setFilters(filters);
                log.debug("Set filters for route {}: {}", entity.getId(), filters);
            }

            // Set order
            if (entity.getOrderNum() != null) {
                rd.setOrder(entity.getOrderNum());
            }

            log.debug("Successfully converted route entity {} to route definition", entity.getId());
            return rd;
        } catch (Exception e) {
            log.error("Error converting route entity {} to route definition", entity.getId(), e);
            return null;
        }
    }

    private List<PredicateDefinition> parsePredicates(String predicatesStr) {
        if (predicatesStr == null || predicatesStr.isEmpty()) {
            return Collections.emptyList();
        }

        return Arrays.stream(predicatesStr.split(",")).map(String::trim).filter(predicate -> !predicate.isEmpty()).map(this::parsePredicateDefinition).toList();
    }

    private PredicateDefinition parsePredicateDefinition(String predicate) {
        PredicateDefinition pd = new PredicateDefinition();

        // Format: Name[key1=value1;key2=value2]
        int bracketStart = predicate.indexOf('[');
        int bracketEnd = predicate.indexOf(']');
        if (bracketStart > 0 && bracketEnd > bracketStart) {
            pd.setName(predicate.substring(0, bracketStart));
            String argsStr = predicate.substring(bracketStart + 1, bracketEnd);
            String[] args = argsStr.split(";");
            for (String arg : args) {
                String[] kv = arg.split("=", 2);
                if (kv.length == 2) {
                    pd.addArg(kv[0], kv[1]);
                }
            }
        } else {
            // fallback to old format for backward compatibility
            if (predicate.startsWith("Path=")) {
                pd.setName("Path");
                pd.addArg("pattern", predicate.substring(5));
            } else if (predicate.startsWith("Method=")) {
                pd.setName("Method");
                pd.addArg("methods", predicate.substring(7));
            } else {
                pd.setName("Path");
                pd.addArg("pattern", predicate);
            }
        }

        return pd;
    }

    private List<FilterDefinition> parseFilters(String filtersStr) {
        if (filtersStr == null || filtersStr.isEmpty()) {
            return Collections.emptyList();
        }

        return Arrays.stream(filtersStr.split(",")).map(String::trim).filter(filter -> !filter.isEmpty()).map(this::parseFilterDefinition).toList();
    }

    private FilterDefinition parseFilterDefinition(String filter) {
        FilterDefinition fd = new FilterDefinition();

        if (filter.startsWith("StripPrefix=")) {
            fd.setName("StripPrefix");
            fd.addArg("parts", filter.substring(12));
        } else if (filter.startsWith("RewritePath=")) {
            fd.setName("RewritePath");
            String rewriteValue = filter.substring(12);
            // Parse RewritePath=/swagger-ui.*,/swagger-aggregator format
            String[] parts = rewriteValue.split(",", 2);
            if (parts.length == 2) {
                fd.addArg("regexp", parts[0]);
                fd.addArg("replacement", parts[1]);
            } else {
                log.warn("Invalid RewritePath format: {}, skipping filter", filter);
                // Return a simple pass-through filter instead of causing errors
                fd.setName("StripPrefix");
                fd.addArg("parts", "0");
            }
        } else if (filter.startsWith("RewritePath=/")) {
            // Handle malformed RewritePath filters from database - skip them
            log.warn("Invalid RewritePath format: {}, treating as simple redirect filter", filter);
            fd.setName("SetPath");
            fd.addArg("template", SWAGGER_AGGREGATOR_PATH);
        } else if (filter.equals(SWAGGER_AGGREGATOR_PATH)) {
            // Handle simple path filters
            fd.setName("SetPath");
            fd.addArg("template", SWAGGER_AGGREGATOR_PATH);
        } else {
            // Default filter - check if it's a valid filter name
            if (filter.contains("=")) {
                String[] parts = filter.split("=", 2);
                fd.setName(parts[0]);
                if (parts.length > 1 && !parts[1].isEmpty()) {
                    fd.addArg("_value", parts[1]);
                }
            } else {
                fd.setName(filter);
            }
        }

        return fd;
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return route.flatMap(rd -> Mono.fromRunnable(() -> {
            RouteEntity entity = new RouteEntity();
            entity.setId(rd.getId());
            entity.setUri(rd.getUri() != null ? rd.getUri().toString() : null);
            entity.setPredicates(convertPredicatesToString(rd.getPredicates()));
            entity.setFilters(convertFiltersToString(rd.getFilters()));
            entity.setOrderNum(rd.getOrder());
            entity.setEnabled(true);
            repository.save(entity);
        }).subscribeOn(Schedulers.boundedElastic())).then();
    }

    private String convertPredicatesToString(List<PredicateDefinition> predicates) {
        if (predicates == null || predicates.isEmpty()) {
            return "";
        }

        return predicates.stream().map(p -> {
            String args = p.getArgs().entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(";"));
            return p.getName() + "[" + args + "]";
        }).collect(Collectors.joining(","));
    }

    private String convertFiltersToString(List<FilterDefinition> filters) {
        if (filters == null || filters.isEmpty()) {
            return "";
        }

        return filters.stream().map(f -> {
            if (f.getArgs() == null || f.getArgs().isEmpty()) {
                return f.getName();
            }
            return f.getName() + "=" + String.join(",", f.getArgs().values());
        }).collect(Collectors.joining(","));
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return routeId.flatMap(id -> Mono.fromRunnable(() -> repository.deleteById(id)).subscribeOn(Schedulers.boundedElastic())).then();
    }
}
