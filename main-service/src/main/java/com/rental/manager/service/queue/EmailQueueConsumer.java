package com.rental.manager.service.queue;

import com.rental.manager.dto.requestdto.EmailTaskRequestDto;
import com.rental.manager.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailQueueConsumer {

    public static final String EMAIL_QUEUE_KEY = "emailQueue";

    private final RedisTemplate<String, Object> redisTemplate;
    private final EmailService emailService;

    @Value("${app.email.max-attempts:3}")
    private int maxAttempts;

    @Scheduled(fixedDelay = 1000)
    public void consume() {
        Object task;
        try {
            task = redisTemplate.opsForList().leftPop(EMAIL_QUEUE_KEY);
        } catch (RedisConnectionFailureException e) {
            log.error("Очередь email недоступна (Redis): {}", e.getMessage());
            return;
        }
        if (task == null) {
            return;
        }
        if (!(task instanceof EmailTaskRequestDto dto)) {
            log.error("В очереди email обнаружена задача неизвестного типа: {}", task.getClass());
            return;
        }

        try {
            process(dto);
            log.info("Email-задача {} для {} обработана успешно", dto.getTaskType(), dto.getTo());
        } catch (Exception e) {
            retryOrDrop(dto, e);
        }
    }

    private void process(EmailTaskRequestDto dto) {
        switch (dto.getTaskType()) {
            case EMAIL_VERIFICATION -> emailService.createAndSendVerificationToken(dto.getTo());
            case OWNER_CREDENTIALS -> {
                emailService.sendOwnerCredentialsEmail(dto.getTo(), dto.getOwnerName(), dto.getOwnerPassword());
                emailService.createAndSendVerificationToken(dto.getTo());
            }
            case RESEND_OWNER_CREDENTIALS -> emailService.resendOwnerCredentialsEmail(dto.getTo());
            case FORGOT_PASSWORD -> emailService.createAndSendPasswordResetToken(dto.getTo());
            case BOOKING_INFO_TO_GUEST -> emailService.sendBookingInfoToGuest(
                    dto.getTo(),
                    dto.getGuestName(),
                    dto.getBookingCode(),
                    dto.getCheckIn(),
                    dto.getCheckOut(),
                    dto.getPrice()
            );
            case BOOKING_INFO_TO_OWNER -> emailService.sendBookingInfoToOwner(
                    dto.getTo(),
                    dto.getOwnerName(),
                    dto.getGuestName(),
                    dto.getBookingCode(),
                    dto.getCheckIn(),
                    dto.getCheckOut(),
                    dto.getPrice()
            );
        }
    }

    private void retryOrDrop(EmailTaskRequestDto dto, Exception e) {
        int attempts = dto.getAttempts() + 1;
        dto.setAttempts(attempts);
        if (attempts < maxAttempts) {
            log.warn("Ошибка отправки email {} для {} (попытка {}/{}): {}. Задача возвращена в очередь.",
                    dto.getTaskType(), dto.getTo(), attempts, maxAttempts, e.getMessage());
            redisTemplate.opsForList().rightPush(EMAIL_QUEUE_KEY, dto);
        } else {
            log.error("Email {} для {} не отправлен после {} попыток. Задача удалена из очереди.",
                    dto.getTaskType(), dto.getTo(), attempts, e);
        }
    }
}
