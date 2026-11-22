package com.rental.manager.service.impl;

import com.rental.manager.dto.requestdto.*;
import com.rental.manager.dto.responsedto.BookingResponseDto;
import com.rental.manager.entities.Booking;
import com.rental.manager.entities.Apartment;
import com.rental.manager.entities.Guest;
import com.rental.manager.entities.User;
import com.rental.manager.entities.enums.BookingStatus;
import com.rental.manager.entities.enums.PaymentStatus;
import com.rental.manager.mappers.BookingMapper;
import com.rental.manager.mappers.GuestMapper;
import com.rental.manager.repository.ApartmentRepository;
import com.rental.manager.repository.BookingRepository;
import com.rental.manager.repository.GuestRepository;
import com.rental.manager.service.BookingService;
import com.rental.manager.service.queue.EmailQueueProducer;
import com.rental.manager.service.queue.EmailTaskType;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final String BOOKING_NOT_FOUND_MSG = "Бронирование не найдено с id: ";
    private static final String APARTMENT_NOT_FOUND_MSG = "Апартаменты не найдены с id: ";

    private final BookingRepository bookingRepository;
    private final ApartmentRepository apartmentRepository;
    private final GuestRepository guestRepository;
    private final BookingMapper mapper;
    private final GuestMapper guestMapper;
    private final EmailQueueProducer emailQueueProducer;

    @Override
    @Transactional
    public BookingResponseDto createBooking(BookingRequestDto request) {
        Apartment apartment = apartmentRepository.findById(request.getApartmentId())
                .orElseThrow(() -> new EntityNotFoundException(APARTMENT_NOT_FOUND_MSG + request.getApartmentId()));

        validateDates(request.getApartmentId(), request.getCheckInDate(), request.getCheckOutDate(), null);

        Booking booking = mapper.toEntity(request);
        booking.setApartment(apartment);
        resolveGuest(booking);
        Booking savedBooking = bookingRepository.save(booking);
        return mapper.toDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponseDto updateBooking(UUID id, BookingRequestDto request) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(BOOKING_NOT_FOUND_MSG + id));

        Apartment apartment = apartmentRepository.findById(request.getApartmentId())
                .orElseThrow(() -> new EntityNotFoundException(APARTMENT_NOT_FOUND_MSG + request.getApartmentId()));

        validateDates(request.getApartmentId(), request.getCheckInDate(), request.getCheckOutDate(), id);

        booking.setApartment(apartment);
        if (request.getMainGuest() != null) {
            if (booking.getMainGuest() != null) {
                guestMapper.updateEntity(booking.getMainGuest(), request.getMainGuest());
            } else {
                booking.setMainGuest(guestMapper.toEntity(request.getMainGuest()));
                resolveGuest(booking);
            }
        }
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setNumberOfAdults(request.getNumberOfAdults() != null ? request.getNumberOfAdults() : 0);
        booking.setNumberOfChildren(request.getNumberOfChildren() != null ? request.getNumberOfChildren() : 0);
        booking.setSecurityDeposit(request.getSecurityDeposit());
        booking.setSecurityDepositCurrency(request.getSecurityDepositCurrency());
        booking.setDepositDueDate(request.getDepositDueDate());
        booking.setCleaningFee(request.getCleaningFee());
        booking.setElectricityRate(request.getElectricityRate());
        booking.setWaterRate(request.getWaterRate());
        booking.setNotes(request.getNotes());
        return mapper.toDto(bookingRepository.save(booking));
    }

    private void validateDates(UUID apartmentId, LocalDate checkIn, LocalDate checkOut, UUID excludeBookingId) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Дата выезда должна быть позже даты заезда");
        }
        List<Booking> overlapping = bookingRepository
                .findOverlappingBookings(apartmentId, checkIn, checkOut, excludeBookingId);
        if (!overlapping.isEmpty()) {
            throw new IllegalArgumentException(
                    "Выбранные даты пересекаются с бронированием " + overlapping.get(0).getBookingCode());
        }
    }

    private void resolveGuest(Booking booking) {
        Guest guest = booking.getMainGuest();
        if (guest == null) {
            return;
        }

        Guest existingGuest = findExistingGuest(guest);
        if (existingGuest != null) {
            mergeGuestData(existingGuest, guest);
            booking.setMainGuest(existingGuest);
        } else {
            booking.setMainGuest(guestRepository.save(guest));
        }
    }

    /** Переносит заполненные данные (паспорт, контакты) из формы на уже существующего гостя. */
    private void mergeGuestData(Guest existing, Guest incoming) {
        if (incoming.getName() != null && !incoming.getName().isBlank()) {
            existing.setName(incoming.getName());
        }
        if (incoming.getEmail() != null && !incoming.getEmail().isBlank()) {
            existing.setEmail(incoming.getEmail());
        }
        if (incoming.getPhoneNumber() != null && !incoming.getPhoneNumber().isBlank()) {
            existing.setPhoneNumber(incoming.getPhoneNumber());
        }
        if (incoming.getDateOfBirth() != null) {
            existing.setDateOfBirth(incoming.getDateOfBirth());
        }
        if (incoming.getNationality() != null && !incoming.getNationality().isBlank()) {
            existing.setNationality(incoming.getNationality());
        }
        if (incoming.getPassportNumber() != null && !incoming.getPassportNumber().isBlank()) {
            existing.setPassportNumber(incoming.getPassportNumber());
        }
        if (incoming.getPassportIssueDate() != null) {
            existing.setPassportIssueDate(incoming.getPassportIssueDate());
        }
        if (incoming.getNotes() != null && !incoming.getNotes().isBlank()) {
            existing.setNotes(incoming.getNotes());
        }
    }

    private Guest findExistingGuest(Guest guest) {
        if (guest.getPhoneNumber() != null && !guest.getPhoneNumber().isBlank()) {
            return guestRepository.findByPhoneNumber(guest.getPhoneNumber());
        }

        if (guest.getEmail() != null && !guest.getEmail().isBlank()) {
            return guestRepository.findByEmail(guest.getEmail());
        }
            return null;
    }

    @Override
    @Transactional
    public BookingResponseDto patchBooking(UUID id, BookingPatchRequestDto request) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(BOOKING_NOT_FOUND_MSG + id));

        mapper.updateEntity(booking, request);
        if (request.getCheckInDate() != null || request.getCheckOutDate() != null) {
            validateDates(booking.getApartment().getId(), booking.getCheckInDate(), booking.getCheckOutDate(), id);
        }
        return mapper.toDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingResponseDto patchBookingStatus(UUID id, BookingStatusPatchRequestDto request) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(BOOKING_NOT_FOUND_MSG + id));
        booking.setBookingStatus(request.getBookingStatus());
        Booking savedBooking = bookingRepository.save(booking);
        if (request.getBookingStatus() == BookingStatus.CONFIRMED) {
            String guestEmail = savedBooking.getMainGuest() != null ? savedBooking.getMainGuest().getEmail() : null;
            String guestName = savedBooking.getMainGuest() != null ? savedBooking.getMainGuest().getName() : null;
            String bookingCode = savedBooking.getBookingCode();
            BigDecimal price = savedBooking.getTotalPrice();
            if (guestEmail != null && !guestEmail.isBlank()) {
                EmailTaskRequestDto guestDto = EmailTaskRequestDto.builder()
                        .to(guestEmail)
                        .guestName(guestName)
                        .bookingCode(bookingCode)
                        .checkIn(savedBooking.getCheckInDate())
                        .checkOut(savedBooking.getCheckOutDate())
                        .price(price)
                        .taskType(EmailTaskType.BOOKING_INFO_TO_GUEST)
                        .build();
                emailQueueProducer.enqueueEmailTask(guestDto);
            }
            User owner = savedBooking.getApartment().getOwner();
            if (owner != null && owner.getEmail() != null && !owner.getEmail().isBlank()) {
                EmailTaskRequestDto ownerDto = EmailTaskRequestDto.builder()
                        .to(owner.getEmail())
                        .ownerName(owner.getName())
                        .guestName(guestName)
                        .bookingCode(bookingCode)
                        .checkIn(savedBooking.getCheckInDate())
                        .checkOut(savedBooking.getCheckOutDate())
                        .price(price)
                        .taskType(EmailTaskType.BOOKING_INFO_TO_OWNER)
                        .build();
                emailQueueProducer.enqueueEmailTask(ownerDto);
            }
        }
        return mapper.toDto(savedBooking);
    }

    @Override
    public BookingResponseDto patchPaymentStatus(UUID id, BookingPaymentStatusPatchRequestDto request) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(BOOKING_NOT_FOUND_MSG + id));
        booking.setPaymentStatus(request.getPaymentStatus());
        return mapper.toDto(bookingRepository.save(booking));
    }

    @Override
    public void deleteBooking(UUID id) {
        bookingRepository.deleteById(id);
    }

    @Override
    public List<BookingResponseDto> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return bookings.stream().map(mapper::toDto).toList();
    }

    @Override
    public BookingResponseDto getBookingById(UUID id) {
        return mapper.toDto(bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(BOOKING_NOT_FOUND_MSG + id)));
    }

    @Override
    public BookingResponseDto getBookingByBookingCode(String bookingCode) {
        return mapper.toDto(bookingRepository.findByBookingCode(bookingCode));

    }

    @Override
    public List<BookingResponseDto>  getBookingsByGuestName(String guestName) {
        List<Booking> bookings = bookingRepository.findByMainGuest_NameContainingIgnoreCase(guestName);
        return bookings.stream().map(mapper::toDto).toList();
    }

    @Override
    public List<BookingResponseDto> getBookingsByGuestEmail(String guestEmail) {
        List<Booking> bookings = bookingRepository.findByMainGuest_EmailContainingIgnoreCase(guestEmail);
        return bookings.stream().map(mapper::toDto).toList();
    }

    @Override
    public List<BookingResponseDto> getBookingsByGuestPhoneNumber(String guestPhoneNumber) {
        List<Booking> bookings = bookingRepository.findByMainGuest_PhoneNumberContainingIgnoreCase(guestPhoneNumber);
        return bookings.stream().map(mapper::toDto).toList();
    }

    @Override
    public List<BookingResponseDto> getBookingsByBookingStatus(BookingStatus bookingStatus) {
        List<Booking> bookings = bookingRepository.findByBookingStatus(bookingStatus);
        return bookings.stream().map(mapper::toDto).toList();
    }

    @Override
    public List<BookingResponseDto> getBookingsByPaymentStatus(PaymentStatus paymentStatus) {
        List<Booking> bookings = bookingRepository.findByPaymentStatus(paymentStatus);
        return bookings.stream().map(mapper::toDto).toList();
    }

    @Override
    public List<BookingResponseDto> getBookingsByApartmentId(UUID apartmentId) {
        List<Booking> bookings = bookingRepository.findByApartmentId(apartmentId);
        return bookings.stream().map(mapper::toDto).toList();
    }

    @Override
    public List<BookingResponseDto> getBookingsByApartmentTitle(String apartmentTitle) {
        List<Booking> bookings = bookingRepository.findByApartmentTitleContainingIgnoreCase(apartmentTitle);
        return bookings.stream().map(mapper::toDto).toList();
    }

    @Override
    public List<BookingResponseDto> getBookingsByApartmentOwnerId(UUID ownerId) {
        List<Booking> bookings = bookingRepository.findByApartmentOwnerId(ownerId);
        return bookings.stream().map(mapper::toDto).toList();
    }

    @Override
    public List<BookingResponseDto> getBookingsByApartmentOwnerName(String ownerName) {
        List<Booking> bookings = bookingRepository.findByApartmentOwnerNameContainingIgnoreCase(ownerName);
        return bookings.stream().map(mapper::toDto).toList();
    }

    @Override
    public List<BookingResponseDto> getBookingsByApartmentOwnerEmail(String ownerEmail) {
        List<Booking> bookings = bookingRepository.findByApartmentOwnerEmailContainingIgnoreCase(ownerEmail);
        return bookings.stream().map(mapper::toDto).toList();
    }

    @Override
    public List<BookingResponseDto> getBookingsByApartmentOwnerPhoneNumber(String ownerPhoneNumber) {
        List<Booking> bookings = bookingRepository.findByApartmentOwnerPhoneNumberContainingIgnoreCase(ownerPhoneNumber);
        return bookings.stream().map(mapper::toDto).toList();
    }
}
