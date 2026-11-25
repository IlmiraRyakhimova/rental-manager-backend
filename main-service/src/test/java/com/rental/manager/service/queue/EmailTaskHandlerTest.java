package com.rental.manager.service.queue;

import com.rental.manager.dto.requestdto.EmailTaskRequestDto;
import com.rental.manager.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class EmailTaskHandlerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailTaskHandler emailTaskHandler;

    @Test
    void sendsVerificationEmail() {
        EmailTaskRequestDto dto = EmailTaskRequestDto.builder()
                .taskType(EmailTaskType.EMAIL_VERIFICATION)
                .to("user@mail.com")
                .build();

        emailTaskHandler.handle(dto);

        verify(emailService).createAndSendVerificationToken("user@mail.com");
        verifyNoMoreInteractions(emailService);
    }

    @Test
    void sendsOwnerCredentialsAndVerification() {
        EmailTaskRequestDto dto = EmailTaskRequestDto.builder()
                .taskType(EmailTaskType.OWNER_CREDENTIALS)
                .to("user@mail.com")
                .ownerName("Anna")
                .ownerPassword("pass123")
                .build();

        emailTaskHandler.handle(dto);

        verify(emailService).sendOwnerCredentialsEmail("user@mail.com", "Anna", "pass123");
        verify(emailService).createAndSendVerificationToken("user@mail.com");
        verifyNoMoreInteractions(emailService);
    }

    @Test
    void resendsOwnerCredentials() {
        EmailTaskRequestDto dto = EmailTaskRequestDto.builder()
                .taskType(EmailTaskType.RESEND_OWNER_CREDENTIALS)
                .to("user@mail.com")
                .build();

        emailTaskHandler.handle(dto);

        verify(emailService).resendOwnerCredentialsEmail("user@mail.com");
        verifyNoMoreInteractions(emailService);
    }

    @Test
    void sendsPasswordReset() {
        EmailTaskRequestDto dto = EmailTaskRequestDto.builder()
                .taskType(EmailTaskType.FORGOT_PASSWORD)
                .to("user@mail.com")
                .build();

        emailTaskHandler.handle(dto);

        verify(emailService).createAndSendPasswordResetToken("user@mail.com");
        verifyNoMoreInteractions(emailService);
    }

    @Test
    void sendsBookingInfoToGuest() {
        EmailTaskRequestDto dto = EmailTaskRequestDto.builder()
                .taskType(EmailTaskType.BOOKING_INFO_TO_GUEST)
                .to("user@mail.com")
                .guestName("Ivan")
                .bookingCode("BOOKING-1")
                .checkIn(LocalDate.of(2026, 12, 1))
                .checkOut(LocalDate.of(2026, 12, 10))
                .price(new BigDecimal("15000"))
                .build();

        emailTaskHandler.handle(dto);

        verify(emailService).sendBookingInfoToGuest("user@mail.com", "Ivan", "BOOKING-1", LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 10), new BigDecimal("15000"));
        verifyNoMoreInteractions(emailService);
    }

    @Test
    void sendsBookingInfoToOwner() {
        EmailTaskRequestDto dto = EmailTaskRequestDto.builder()
                .taskType(EmailTaskType.BOOKING_INFO_TO_OWNER)
                .to("user@mail.com")
                .ownerName("Anna")
                .guestName("Ivan")
                .bookingCode("BOOKING-1")
                .checkIn(LocalDate.of(2026, 12, 1))
                .checkOut(LocalDate.of(2026, 12, 10))
                .price(new BigDecimal("15000"))
                .build();

        emailTaskHandler.handle(dto);

        verify(emailService).sendBookingInfoToOwner("user@mail.com", "Anna", "Ivan", "BOOKING-1", LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 10), new BigDecimal("15000"));
        verifyNoMoreInteractions(emailService);
    }



}
