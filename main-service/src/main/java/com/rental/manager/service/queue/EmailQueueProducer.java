package com.rental.manager.service.queue;

import com.rental.manager.dto.requestdto.EmailTaskRequestDto;
import com.rental.manager.service.queue.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailQueueProducer {
    private final KafkaTemplate<String, EmailTaskRequestDto> kafkaTemplate;

    public void enqueueEmailTask(EmailTaskRequestDto dto) {
       kafkaTemplate.send(KafkaConfig.EMAIL_TASKS_TOPIC, dto.getTo(), dto)
               .whenComplete((result, ex) -> {
                   if (ex != null) {
                       log.error("Не удалось отправить email-задачу {} для {} в Kafka: {}",
                               dto.getTaskType(), dto.getTo(), ex.getMessage());
                   } else {
                       RecordMetadata meta = result.getRecordMetadata();
                       log.info("Email-задача {} для {} отправлена в {} (партиция {}, офсет {})",
                               dto.getTaskType(), dto.getTo(), meta.topic(), meta.partition(), meta.offset());
                   }
               });
    }
}
