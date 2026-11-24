package com.rental.logging;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.logging")
public class ControllerLoggingProperties {
    private boolean enabled = true;
}
