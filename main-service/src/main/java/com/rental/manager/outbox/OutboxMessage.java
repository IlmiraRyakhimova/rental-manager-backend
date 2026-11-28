package com.rental.manager.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_messages")
@Getter
@NoArgsConstructor
public class OutboxMessage {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String topic;

    @Column(name = "message_key")
    private String messageKey;

    @Column(name = "payload_type", nullable = false)
    private String payloadType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    public OutboxMessage(String topic, String messageKey, String payloadType, String payload) {
        this.topic = topic;
        this.messageKey = messageKey;
        this.payloadType = payloadType;
        this.payload = payload;
        this.createdAt = Instant.now();
    }

    public void markPublished() {
        this.publishedAt = Instant.now();
    }
}


