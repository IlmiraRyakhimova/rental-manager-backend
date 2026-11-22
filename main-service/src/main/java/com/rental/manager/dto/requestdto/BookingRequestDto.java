package com.rental.manager.dto.requestdto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class BookingRequestDto {

    @NotNull
    private UUID apartmentId;

    @Valid
    private GuestRequestDto mainGuest;
    private Integer numberOfAdults;
    private Integer numberOfChildren;

    @NotNull
    private LocalDate checkInDate;

    @NotNull
    private LocalDate checkOutDate;

    private BigDecimal securityDeposit;
    private String securityDepositCurrency;
    private LocalDate depositDueDate;
    private BigDecimal cleaningFee;
    private BigDecimal electricityRate;
    private BigDecimal waterRate;

    private String notes;
}
