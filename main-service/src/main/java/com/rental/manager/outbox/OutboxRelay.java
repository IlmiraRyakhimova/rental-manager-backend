package com.rental.manager.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelay {

    public static final String MESSAGE_ID_HEADER = "messageId";

    private final OutboxMessageRepository repository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:1000}")
    @Transactional
    public void publishPending() {
        List<OutboxMessage> batch = repository.findTop100ByPublishedAtIsNullOrderByCreatedAtAsc();

        for (OutboxMessage message : batch) {
            try {
                Object payload = objectMapper.readValue(message.getPayload(), Class.forName(message.getPayloadType()));

                ProducerRecord<String, Object> record =
                        new ProducerRecord<>(message.getTopic(), message.getMessageKey(), payload);
                record.headers().add(MESSAGE_ID_HEADER, message.getId().toString().getBytes(StandardCharsets.UTF_8));

                kafkaTemplate.send(record).get(10, TimeUnit.SECONDS);
                message.markPublished();

                log.info("Outbox-сообщение {} отправлено в {}", message.getId(), message.getTopic());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Отправка outbox прервана: приложение останавливается");
                return;
            } catch (Exception e) {
                log.warn("Не удалось отправить outbox-сообщение {}: {}. Повторю на следующем цикле.",
                        message.getId(), e.getMessage());
                break;
            }
        }
    }
}