package com.rental.manager.service.queue.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    public static final String EMAIL_TASKS_TOPIC = "email-tasks";

    @Bean
    public NewTopic emailTasksTopic() {
        return TopicBuilder.name(EMAIL_TASKS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
