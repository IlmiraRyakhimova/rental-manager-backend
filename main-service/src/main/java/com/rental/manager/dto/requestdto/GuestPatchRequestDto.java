package com.rental.manager.dto.requestdto;

import com.rental.manager.validation.ValidEmail;
import com.rental.manager.validation.ValidPhone;
import lombok.Data;

import java.time.LocalDate;

@Data
public class GuestPatchRequestDto {
    private String name;

    @ValidEmail
    private String email;

    @ValidPhone
    private String phoneNumber;

    private LocalDate dateOfBirth;
    private String nationality;
    private String passportNumber;
    private LocalDate passportIssueDate;

    private String notes;
}
