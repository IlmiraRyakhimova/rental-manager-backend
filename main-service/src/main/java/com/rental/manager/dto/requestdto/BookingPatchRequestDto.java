package com.rental.manager.dto.requestdto;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class BookingPatchRequestDto {
    private UUID apartmentId;
    private GuestPatchRequestDto mainGuest;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer numberOfAdults;
    private Integer numberOfChildren;

    private BigDecimal securityDeposit;
    private String securityDepositCurrency;
    private LocalDate depositDueDate;
    private BigDecimal cleaningFee;
    private BigDecimal electricityRate;
    private BigDecimal waterRate;

    private String notes;
}
