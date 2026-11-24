package com.rental.logging;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "app.logging", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ControllerLoggingProperties.class)
public class ControllerLoggingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ControllerLoggingAspect controllerLoggingAspect() {
        return new ControllerLoggingAspect();
    }
}
