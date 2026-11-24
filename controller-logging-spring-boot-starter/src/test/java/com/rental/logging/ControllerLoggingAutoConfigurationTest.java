package com.rental.logging;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class ControllerLoggingAutoConfigurationTest {
    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ControllerLoggingAutoConfiguration.class));

    @Test
    void createsAspectByDefault() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(ControllerLoggingAspect.class));
    }

    @Test
    void doesNotCreateAspectWhenDisabled() {
        contextRunner
                .withPropertyValues("app.logging.enabled=false")
                .run(context ->
                        assertThat(context).doesNotHaveBean(ControllerLoggingAspect.class));

    }

    @Test
    void doesNotCreateAspectInNonWebApplication() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ControllerLoggingAutoConfiguration.class))
                .run(context -> assertThat(context).doesNotHaveBean(ControllerLoggingAspect.class));
    }

    @Test
    void backsOffWhenUserDefinesOwnAspect() {
        contextRunner
                .withUserConfiguration(UserConfig.class)
                .run(context ->
                        assertThat(context)
                                .hasSingleBean(ControllerLoggingAspect.class)
                                .hasBean("customAspect"));
    }

    @Configuration
    static class UserConfig {

        @Bean
        ControllerLoggingAspect customAspect() {
            return new ControllerLoggingAspect();
        }
    }

}
