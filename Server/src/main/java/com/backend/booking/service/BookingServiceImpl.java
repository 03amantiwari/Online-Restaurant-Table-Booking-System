package com.backend.booking.service;

import com.backend.booking.dto.request.BookingCreateRequestDto;
import com.backend.booking.dto.request.BookingStatusUpdateDto;
import com.backend.booking.dto.response.BookingResponseDto;
import com.backend.booking.entity.Booking;
import com.backend.booking.repository.BookingRepository;
import com.backend.common.exception.BusinessRuleViolationException;
import com.backend.common.exception.ResourceNotFoundException;
import com.backend.common.exception.UnauthorizedActionException;
import com.backend.restaurant.entity.Restaurant;
import com.backend.restaurant.repository.RestaurantRepository;
import com.backend.restaurantTable.entity.RestaurantTable;
import com.backend.restaurantTable.repository.RestaurantTableRepository;
import com.backend.timeSlot.entity.TimeSlot;
import com.backend.timeSlot.repository.TimeSlotRepository;
import com.backend.user.entity.User;
import com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantTableRepository restaurantTableRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final BookingStatusHistoryService bookingStatusHistoryService;

    // Bookings in these statuses hold the table for a slot (used for the double-booking check)
    private static final List<Booking.BookingStatus> ACTIVE_STATUSES =
            List.of(Booking.BookingStatus.PENDING, Booking.BookingStatus.CONFIRMED);

    // Once a booking reaches one of these, it can't be changed further
    private static final Set<Booking.BookingStatus> TERMINAL_STATUSES =
            Set.of(Booking.BookingStatus.CANCELLED, Booking.BookingStatus.COMPLETED, Booking.BookingStatus.REJECTED);

    private static final Random REFERENCE_RANDOM = new Random();

    @Override
    @Transactional
    public BookingResponseDto createBooking(BookingCreateRequestDto dto) {
        // Rule: User, Restaurant, Table and TimeSlot must exist
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " , dto.getUserId()));

        Restaurant restaurant = restaurantRepository.findById(dto.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " , dto.getRestaurantId()));

        RestaurantTable table = restaurantTableRepository.findById(dto.getTableId())
                .orElseThrow(() -> new ResourceNotFoundException("Table not found with id: " , dto.getTableId()));

        TimeSlot timeSlot = timeSlotRepository.findById(dto.getTimeSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Time slot not found with id: " , dto.getTimeSlotId()));

        // Rule: Table must belong to Restaurant
        if (!table.getRestaurant().getId().equals(restaurant.getId())) {
            throw new BusinessRuleViolationException("Table " + table.getId() + " does not belong to restaurant " + restaurant.getId());
        }

        // Time slot should belong to the same restaurant too
        if (!timeSlot.getRestaurant().getId().equals(restaurant.getId())) {
            throw new BusinessRuleViolationException("Time slot " + timeSlot.getId() + " does not belong to restaurant " + restaurant.getId());
        }

        // Rule: Table must be available for selected date & slot
        // First check: the table isn't manually marked unavailable/occupied by the admin
        if (table.getTableStatus() != RestaurantTable.TableStatus.AVAILABLE) {
            throw new BusinessRuleViolationException("Table " + table.getTableNumber() + " is not available for booking");
        }

        // Rule: Guests <= Table Capacity
        if (dto.getPartySize() > table.getSeatingCapacity()) {
            throw new BusinessRuleViolationException(
                    "Party size (" + dto.getPartySize() + ") exceeds table capacity (" + table.getSeatingCapacity() + ")");
        }

        // Rule: Prevent double booking - same table, same date, same slot, still active
        boolean alreadyBooked = bookingRepository.existsByRestaurantTableAndBookingDateAndTimeSlotAndStatusIn(
                table, dto.getBookingDate(), timeSlot, ACTIVE_STATUSES);
        if (alreadyBooked) {
            throw new BusinessRuleViolationException(
                    "Table " + table.getTableNumber() + " is already booked for " + dto.getBookingDate() + " in this time slot");
        }

        Booking booking = Booking.builder()
                .bookingReference(generateUniqueBookingReference())
                .user(user)
                .restaurant(restaurant)
                .restaurantTable(table)
                .timeSlot(timeSlot)
                .bookingDate(dto.getBookingDate())
                .partySize(dto.getPartySize())
                .specialRequest(dto.getSpecialRequest())
                .build(); // status defaults to PENDING via @PrePersist

        Booking savedBooking = bookingRepository.save(booking);

        bookingStatusHistoryService.recordStatusChange(
                savedBooking, null, savedBooking.getStatus(), user, "Booking created");

        return mapToResponseDto(savedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDto getBookingById(Long bookingId) {
        return mapToResponseDto(findBookingOrThrow(bookingId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getMyBookings(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " , userId));

        return bookingRepository.findByUserOrderByBookingDateDescCreatedAtDesc(user)
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public BookingResponseDto cancelBooking(Long bookingId, Long userId) {
        Booking booking = findBookingOrThrow(bookingId);

        // Only the booking's own customer can cancel it
        if (!booking.getUser().getId().equals(userId)) {
            throw new UnauthorizedActionException("You are not authorized to cancel this booking");
        }

        // Rule: Customer can cancel only future bookings
        if (booking.getBookingDate().isBefore(LocalDate.now())) {
            throw new BusinessRuleViolationException("Cannot cancel a booking whose date has already passed");
        }

        if (TERMINAL_STATUSES.contains(booking.getStatus())) {
            throw new BusinessRuleViolationException(
                    "Booking is already " + booking.getStatus() + " and cannot be cancelled");
        }

        Booking.BookingStatus previousStatus = booking.getStatus();
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());

        Booking savedBooking = bookingRepository.save(booking);

        /*
         * WHY reset the table to AVAILABLE here?
         * When a booking is cancelled, the physical table is freed up.
         * Setting it back to AVAILABLE means the next customer can see
         * and book it immediately — without any manual admin intervention.
         * This runs inside @Transactional, so if the booking save fails,
         * the table status also rolls back — guaranteed consistency.
         */
        RestaurantTable table = booking.getRestaurantTable();
        table.setTableStatus(RestaurantTable.TableStatus.AVAILABLE);
        restaurantTableRepository.save(table);

        bookingStatusHistoryService.recordStatusChange(
                savedBooking, previousStatus, Booking.BookingStatus.CANCELLED,
                savedBooking.getUser(), "Cancelled by customer");

        return mapToResponseDto(savedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getRestaurantBookings(Long restaurantId) {
        Restaurant restaurant = findRestaurantOrThrow(restaurantId);
        return bookingRepository.findByRestaurant(restaurant)
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public BookingResponseDto updateBookingStatus(Long bookingId, BookingStatusUpdateDto dto, Long changedByUserId) {
        Booking booking = findBookingOrThrow(bookingId);

        if (TERMINAL_STATUSES.contains(booking.getStatus())) {
            throw new BusinessRuleViolationException(
                    "Booking is already " + booking.getStatus() + " and its status cannot be changed further");
        }

        Booking.BookingStatus previousStatus = booking.getStatus();
        booking.setStatus(dto.getStatus());
        if (TERMINAL_STATUSES.contains(dto.getStatus()) && dto.getStatus() == Booking.BookingStatus.CANCELLED) {
            booking.setCancelledAt(LocalDateTime.now());
        }

        Booking savedBooking = bookingRepository.save(booking);

        /*
         * WHY reset table to AVAILABLE on terminal statuses?
         * CANCELLED, COMPLETED, REJECTED — all mean the booking is done.
         * The physical table should be freed so future customers can book it.
         * NO_SHOW is also included — the customer didn't come, table is free.
         * This does NOT apply to PENDING → CONFIRMED (table stays held).
         */
        Set<Booking.BookingStatus> tableReleaseStatuses = Set.of(
            Booking.BookingStatus.CANCELLED,
            Booking.BookingStatus.COMPLETED,
            Booking.BookingStatus.REJECTED,
            Booking.BookingStatus.NO_SHOW
        );
        if (tableReleaseStatuses.contains(dto.getStatus())) {
            RestaurantTable table = savedBooking.getRestaurantTable();
            table.setTableStatus(RestaurantTable.TableStatus.AVAILABLE);
            restaurantTableRepository.save(table);
        }

        // changedByUserId is optional - null just means "actor not tracked" until auth exists
        User changedByUser = changedByUserId != null
                ? userRepository.findById(changedByUserId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " , changedByUserId))
                : null;

        bookingStatusHistoryService.recordStatusChange(
                savedBooking, previousStatus, dto.getStatus(), changedByUser, "Status updated by owner/admin");

        return mapToResponseDto(savedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getBookingsByDate(Long restaurantId, LocalDate date) {
        Restaurant restaurant = findRestaurantOrThrow(restaurantId);
        return bookingRepository.findByRestaurantAndBookingDate(restaurant, date)
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getBookingsByTimeSlot(Long restaurantId, Long timeSlotId) {
        Restaurant restaurant = findRestaurantOrThrow(restaurantId);

        TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new ResourceNotFoundException("Time slot not found with id: " , timeSlotId));

        if (!timeSlot.getRestaurant().getId().equals(restaurant.getId())) {
            throw new BusinessRuleViolationException("Time slot " + timeSlotId + " does not belong to restaurant " + restaurantId);
        }

        return bookingRepository.findByRestaurantAndTimeSlot(restaurant, timeSlot)
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    // ---- helpers ----

    private Booking findBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " , bookingId));
    }

    private Restaurant findRestaurantOrThrow(Long restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " , restaurantId));
    }

    private String generateUniqueBookingReference() {
        String reference;
        do {
            // Simple readable format for now, e.g. BK-483920. A production system
            // would likely use a DB sequence or UUID instead of a random retry loop.
            reference = "BK-" + String.format("%06d", REFERENCE_RANDOM.nextInt(1_000_000));
        } while (bookingRepository.existsByBookingReference(reference));
        return reference;
    }

    // Manual mapping (see the RestaurantTable service for why: STRICT ModelMapper
    // won't flatten nested associations like restaurant.name -> restaurantName).
    // Runs inside the @Transactional method so LAZY associations are still fetchable.
    private BookingResponseDto mapToResponseDto(Booking booking) {
        return BookingResponseDto.builder()
                .id(booking.getId())
                .bookingReference(booking.getBookingReference())
                .userId(booking.getUser().getId())
                .userFullName(booking.getUser().getFullName())
                .restaurantId(booking.getRestaurant().getId())
                .restaurantName(booking.getRestaurant().getName())
                .tableId(booking.getRestaurantTable().getId())
                .tableNumber(booking.getRestaurantTable().getTableNumber())
                .timeSlotId(booking.getTimeSlot().getId())
                .timeSlotLabel(booking.getTimeSlot().getLabel())
                .bookingDate(booking.getBookingDate())
                .partySize(booking.getPartySize())
                .status(booking.getStatus())
                .specialRequest(booking.getSpecialRequest())
                .cancelledAt(booking.getCancelledAt())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}