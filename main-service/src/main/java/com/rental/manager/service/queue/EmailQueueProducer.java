package com.rental.manager.service.queue;

import com.rental.manager.dto.requestdto.EmailTaskRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailQueueProducer {
    private final RedisTemplate<String, Object> redisTemplate;

    public void enqueueEmailTask(EmailTaskRequestDto dto) {
        try {
            redisTemplate.opsForList().rightPush(EmailQueueConsumer.EMAIL_QUEUE_KEY, dto);
            log.info("Email-задача {} для {} поставлена в очередь", dto.getTaskType(), dto.getTo());
        } catch (DataAccessException e) {
            log.error("Не удалось поставить email-задачу {} для {} в очередь: {}",
                    dto.getTaskType(), dto.getTo(), e.getMessage());
            throw new IllegalStateException(
                    "Сервис отправки писем временно недоступен. Попробуйте позже.", e);
        }
    }
}
