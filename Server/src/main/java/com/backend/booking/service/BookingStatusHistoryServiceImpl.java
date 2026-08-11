package com.backend.booking.service;

import com.backend.booking.dto.response.BookingStatusHistoryResponseDto;
import com.backend.booking.entity.Booking;
import com.backend.booking.entity.BookingStatusHistory;
import com.backend.booking.repository.BookingRepository;
import com.backend.booking.repository.BookingStatusHistoryRepository;
import com.backend.common.exception.ResourceNotFoundException;
import com.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingStatusHistoryServiceImpl implements BookingStatusHistoryService {

    private final BookingStatusHistoryRepository bookingStatusHistoryRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BookingStatusHistoryResponseDto> getHistoryForBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " , bookingId));

        return bookingStatusHistoryRepository.findByBookingOrderByChangedAtAsc(booking)
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public void recordStatusChange(Booking booking, Booking.BookingStatus fromStatus,
                                    Booking.BookingStatus toStatus, User changedByUser, String note) {
        BookingStatusHistory history = BookingStatusHistory.builder()
                .booking(booking)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .changedByUser(changedByUser)
                .note(note)
                .build();
        bookingStatusHistoryRepository.save(history);
    }

    // Runs inside the @Transactional read method so the LAZY changedByUser association
    // is still fetchable (open-in-view is disabled) - same pattern as the other services.
    private BookingStatusHistoryResponseDto mapToResponseDto(BookingStatusHistory history) {
        return BookingStatusHistoryResponseDto.builder()
                .id(history.getId())
                .bookingId(history.getBooking().getId())
                .fromStatus(history.getFromStatus())
                .toStatus(history.getToStatus())
                .changedByUserId(history.getChangedByUser() != null ? history.getChangedByUser().getId() : null)
                .changedByUserName(history.getChangedByUser() != null ? history.getChangedByUser().getFullName() : null)
                .note(history.getNote())
                .changedAt(history.getChangedAt())
                .build();
    }
}
