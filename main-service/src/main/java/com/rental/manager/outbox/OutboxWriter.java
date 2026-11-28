package com.rental.manager.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboxWriter {

    private final OutboxMessageRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void write(String topic, String key, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            repository.save(new OutboxMessage(topic, key, payload.getClass().getName(), json));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Не удалось сериализовать сообщение для outbox", e);
        }
    }
}
