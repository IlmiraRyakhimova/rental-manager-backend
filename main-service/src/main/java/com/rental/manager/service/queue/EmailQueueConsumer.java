package com.rental.manager.service.queue;

import com.rental.manager.dto.requestdto.EmailTaskRequestDto;
import com.rental.manager.service.queue.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailQueueConsumer {

    private final EmailTaskHandler emailTaskHandler;

    @KafkaListener(topics = KafkaConfig.EMAIL_TASKS_TOPIC)
    public void consume (EmailTaskRequestDto dto) {
        emailTaskHandler.handle(dto);
        log.info("Email-задача {} для {} обработана", dto.getTaskType(), dto.getTo());
    }





}
