package com.rental.manager.dto.requestdto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class GuestRequestDto {
    @NotBlank
    private String name;

    private String email;

    private String phoneNumber;

    private LocalDate dateOfBirth;
    private String nationality;
    private String passportNumber;
    private LocalDate passportIssueDate;

    private String notes;
}
