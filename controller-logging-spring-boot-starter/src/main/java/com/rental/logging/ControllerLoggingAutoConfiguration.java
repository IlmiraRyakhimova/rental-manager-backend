package com.rental.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
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
    public ControllerLoggingAspect controllerLoggingAspect(SensitiveDataMasker masker) {
        return new ControllerLoggingAspect(masker);
    }

    @Bean
    @ConditionalOnMissingBean
    public SensitiveDataMasker sensitiveDataMasker(ControllerLoggingProperties properties, ObjectProvider<ObjectMapper> objectMapper) {
        return new SensitiveDataMasker(objectMapper.getIfAvailable(ObjectMapper::new), properties.getMaskedFields());
    }
}
