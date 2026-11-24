package com.rental.logging;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@Data
@ConfigurationProperties(prefix = "app.logging")
public class ControllerLoggingProperties {
    private boolean enabled = true;
    private Set<String> maskedFields = Set.of("password", "token", "secret");
}
