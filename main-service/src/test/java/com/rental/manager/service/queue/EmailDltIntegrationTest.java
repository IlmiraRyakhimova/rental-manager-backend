package com.rental.manager.service.queue;

import com.rental.manager.IntegrationTestBase;
import com.rental.manager.dto.requestdto.EmailTaskRequestDto;
import com.rental.manager.service.EmailService;
import com.rental.manager.service.queue.config.KafkaConfig;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class EmailDltIntegrationTest extends IntegrationTestBase {

    @Autowired
    private EmailQueueProducer producer;

    @Autowired
    private ConsumerFactory<String, EmailTaskRequestDto> consumerFactory;

    @Test
    void permanentErrorGoesToDltWithoutRetries() {
        doThrow(new IllegalArgumentException("Email не подтвержден"))
                .when(emailService).createAndSendPasswordResetToken("dlt@test.com");

        producer.enqueueEmailTask(EmailTaskRequestDto.builder()
                .taskType(EmailTaskType.FORGOT_PASSWORD)
                .to("dlt@test.com")
                .build());

        try (Consumer<String, EmailTaskRequestDto> dltConsumer =
                     consumerFactory.createConsumer("dlt-test-group", null)) {
            dltConsumer.subscribe(List.of(KafkaConfig.EMAIL_TASKS_DLT));

            ConsumerRecord<String, EmailTaskRequestDto> record =
                    KafkaTestUtils.getSingleRecord(dltConsumer, KafkaConfig.EMAIL_TASKS_DLT, Duration.ofSeconds(15));

            assertThat(record.value().getTo()).isEqualTo("dlt@test.com");
        }

        verify(emailService, times(1)).createAndSendPasswordResetToken("dlt@test.com");
    }

}
