package ec.com.ecommerce.gateway.adapter.web;

import ec.com.ecommerce.gateway.application.service.SwaggerAggregatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/swagger-aggregator")
@RequiredArgsConstructor
public class SwaggerAggregatorController {

    private final SwaggerAggregatorService swaggerAggregatorService;

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getSwaggerAggregatorPage() {
        List<String> servicesWithSwagger = swaggerAggregatorService.getServicesWithSwagger();
        
        String html = generateSwaggerAggregatorHTML(servicesWithSwagger);
        return ResponseEntity.ok(html);
    }

    @GetMapping("/api/services")
    public ResponseEntity<Map<String, Object>> getServicesInfo() {
        List<String> servicesWithSwagger = swaggerAggregatorService.getServicesWithSwagger();
        
        Map<String, Object> response = new HashMap<>();
        response.put("services", servicesWithSwagger);
        response.put("count", servicesWithSwagger.size());
        response.put("gatewayUrl", "/docs");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/generate")
    public ResponseEntity<Map<String, String>> generateRoutes() {
        try {
            swaggerAggregatorService.generateAggregatedSwaggerRoutes();
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Swagger routes generated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating Swagger routes", e);
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Error generating Swagger routes: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    private String generateSwaggerAggregatorHTML(List<String> services) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n")
            .append("<html lang=\"en\">\n")
            .append("<head>\n")
            .append("    <meta charset=\"UTF-8\">\n")
            .append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
            .append("    <title>API Documentation - Service Aggregator</title>\n")
            .append("    <style>\n")
            .append("        body { font-family: Arial, sans-serif; margin: 40px; background-color: #f5f5f5; }\n")
            .append("        .container { max-width: 1200px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }\n")
            .append("        h1 { color: #1f4e79; border-bottom: 3px solid #1f4e79; padding-bottom: 10px; }\n")
            .append("        .service-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 20px; margin-top: 30px; }\n")
            .append("        .service-card { border: 1px solid #ddd; border-radius: 8px; padding: 20px; background: #f9f9f9; transition: transform 0.2s; }\n")
            .append("        .service-card:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.15); }\n")
            .append("        .service-name { font-size: 18px; font-weight: bold; color: #1f4e79; margin-bottom: 15px; }\n")
            .append("        .btn { display: inline-block; padding: 8px 16px; margin: 5px; text-decoration: none; border-radius: 4px; font-weight: bold; transition: background-color 0.2s; }\n")
            .append("        .btn-primary { background-color: #007bff; color: white; }\n")
            .append("        .btn-primary:hover { background-color: #0056b3; }\n")
            .append("        .btn-secondary { background-color: #6c757d; color: white; }\n")
            .append("        .btn-secondary:hover { background-color: #545b62; }\n")
            .append("        .info-box { background: #e7f3ff; border: 1px solid #b8daff; border-radius: 4px; padding: 15px; margin: 20px 0; }\n")
            .append("        .refresh-btn { background-color: #28a745; color: white; border: none; padding: 10px 20px; border-radius: 4px; cursor: pointer; margin-bottom: 20px; }\n")
            .append("        .refresh-btn:hover { background-color: #218838; }\n")
            .append("    </style>\n")
            .append("</head>\n")
            .append("<body>\n")
            .append("    <div class=\"container\">\n")
            .append("        <h1>🚀 E-Commerce Platform - API Documentation</h1>\n")
            .append("        <div class=\"info-box\">\n")
            .append("            <strong>📊 Services Overview:</strong> This gateway aggregates documentation from all microservices. \n")
            .append("            Access individual service documentation using the links below.\n")
            .append("        </div>\n")
            .append("        <button class=\"refresh-btn\" onclick=\"refreshRoutes()\">🔄 Refresh Routes</button>\n")
            .append("        <div class=\"service-grid\">\n");

        if (services.isEmpty()) {
            html.append("            <div class=\"service-card\">\n")
                .append("                <div class=\"service-name\">No services found</div>\n")
                .append("                <p>No services with Swagger documentation are currently registered.</p>\n")
                .append("                <p>Make sure your services are running and have the route scanner enabled.</p>\n")
                .append("            </div>\n");
        } else {
            for (String service : services) {
                if (!"gateway".equals(service)) {
                    html.append("            <div class=\"service-card\">\n")
                        .append("                <div class=\"service-name\">📦 ").append(service.toUpperCase()).append(" Service</div>\n")
                        .append("                <p>API documentation and interactive testing interface</p>\n")
                        .append("                <div>\n")
                        .append("                    <a href=\"/docs/").append(service).append("/swagger-ui/index.html\" class=\"btn btn-primary\" target=\"_blank\">📖 Swagger UI</a>\n")
                        .append("                    <a href=\"/docs/").append(service).append("/v3/api-docs\" class=\"btn btn-secondary\" target=\"_blank\">📄 OpenAPI JSON</a>\n")
                        .append("                    <a href=\"/").append(service).append("/swagger-ui/index.html\" class=\"btn btn-secondary\" target=\"_blank\">🔗 Direct Access</a>\n")
                        .append("                </div>\n")
                        .append("            </div>\n");
                }
            }
        }

        html.append("        </div>\n")
            .append("        <div class=\"info-box\" style=\"margin-top: 30px;\">\n")
            .append("            <strong>🔧 Gateway Information:</strong><br>\n")
            .append("            • Gateway URL: <code>").append("http://localhost:8080").append("</code><br>\n")
            .append("            • Route Management: <code><a href=\"/admin/routes\">/admin/routes</a></code><br>\n")
            .append("            • H2 Console: <code><a href=\"/h2-console\">/h2-console</a></code><br>\n")
            .append("            • Total Services: <strong>").append(services.size()).append("</strong>\n")
            .append("        </div>\n")
            .append("    </div>\n")
            .append("    <script>\n")
            .append("        async function refreshRoutes() {\n")
            .append("            try {\n")
            .append("                const response = await fetch('/swagger-aggregator/generate');\n")
            .append("                const result = await response.json();\n")
            .append("                if (result.status === 'success') {\n")
            .append("                    alert('✅ Routes refreshed successfully!');\n")
            .append("                    location.reload();\n")
            .append("                } else {\n")
            .append("                    alert('❌ Error: ' + result.message);\n")
            .append("                }\n")
            .append("            } catch (error) {\n")
            .append("                alert('❌ Error refreshing routes: ' + error.message);\n")
            .append("            }\n")
            .append("        }\n")
            .append("    </script>\n")
            .append("</body>\n")
            .append("</html>");

        return html.toString();
    }
}