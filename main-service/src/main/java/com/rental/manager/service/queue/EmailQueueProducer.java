package com.rental.manager.service.queue;

import com.rental.manager.dto.requestdto.EmailTaskRequestDto;
import com.rental.manager.outbox.OutboxWriter;
import com.rental.manager.service.queue.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailQueueProducer {

    private final OutboxWriter outboxWriter;
    public void enqueueEmailTask(EmailTaskRequestDto dto) {
       outboxWriter.write(KafkaConfig.EMAIL_TASKS_TOPIC, dto.getTo(), dto);
        log.info("Email-задача {} для {} записана в outbox", dto.getTaskType(), dto.getTo());

    }
}
