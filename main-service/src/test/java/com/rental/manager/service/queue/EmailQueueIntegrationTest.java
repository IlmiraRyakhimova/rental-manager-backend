package com.rental.manager.service.queue;

import com.rental.manager.IntegrationTestBase;
import com.rental.manager.dto.requestdto.EmailTaskRequestDto;
import com.rental.manager.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;


class EmailQueueIntegrationTest extends IntegrationTestBase {

    @Autowired
    private EmailQueueProducer producer;

    @Test

    void emailTaskTravelsThroughKafkaToHandler() {
        producer.enqueueEmailTask(EmailTaskRequestDto.builder()
                .taskType(EmailTaskType.EMAIL_VERIFICATION)
                .to("integration@test.com")
                .build());

        verify(emailService, timeout(10_000)).createAndSendVerificationToken("integration@test.com");
    }

}
