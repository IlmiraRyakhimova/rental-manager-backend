package com.rental.manager.service.queue;

import com.rental.manager.dto.requestdto.EmailTaskRequestDto;
import com.rental.manager.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailTaskHandler {
    private final EmailService emailService;

    public void handle(EmailTaskRequestDto dto) {
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
}
