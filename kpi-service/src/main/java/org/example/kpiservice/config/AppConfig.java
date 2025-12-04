package org.example.kpiservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {

    private Jwt jwt;
    private Cors cors;

    @Data
    public static class Jwt {
        private String secret;
        private Long expiration;
    }

    @Data
    public static class Cors {
        private List<String> allowedOrigins;
    }
}
