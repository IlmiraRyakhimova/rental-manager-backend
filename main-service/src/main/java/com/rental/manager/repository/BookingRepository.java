package com.rental.manager.repository;

import com.rental.manager.entities.Booking;
import com.rental.manager.entities.enums.BookingStatus;
import com.rental.manager.entities.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    Booking findByBookingCode(String bookingCode);
    List<Booking> findByMainGuest_NameContainingIgnoreCase(String guestName);
    List<Booking> findByMainGuest_PhoneNumberContainingIgnoreCase(String guestPhoneNumber);
    List<Booking>  findByMainGuest_EmailContainingIgnoreCase(String guestEmail);
    List<Booking> findByBookingStatus(BookingStatus bookingStatus);
    List<Booking> findByPaymentStatus(PaymentStatus paymentStatus);

    List<Booking> findByApartmentId(UUID apartmentId);
    List<Booking> findByApartmentTitleContainingIgnoreCase(String apartmentTitle);

    List<Booking> findByApartmentOwnerId(UUID ownerId);
    List<Booking> findByApartmentOwnerNameContainingIgnoreCase(String ownerName);
    List<Booking> findByApartmentOwnerEmailContainingIgnoreCase(String ownerEmail);
    List<Booking> findByApartmentOwnerPhoneNumberContainingIgnoreCase(String ownerPhoneNumber);

    @Query("""
            select b from Booking b
            where b.apartment.id = :apartmentId
              and b.bookingStatus <> com.rental.manager.entities.enums.BookingStatus.CANCELLED
              and (:excludeId is null or b.id <> :excludeId)
              and b.checkInDate < :checkOut
              and b.checkOutDate > :checkIn
            """)
    List<Booking> findOverlappingBookings(@Param("apartmentId") UUID apartmentId,
                                          @Param("checkIn") LocalDate checkIn,
                                          @Param("checkOut") LocalDate checkOut,
                                          @Param("excludeId") UUID excludeId);
}
