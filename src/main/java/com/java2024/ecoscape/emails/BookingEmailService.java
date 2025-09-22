package com.java2024.ecoscape.emails;

import com.java2024.ecoscape.dto.BookingResponse;
import com.java2024.ecoscape.models.Booking;

public interface BookingEmailService {
    void sendBookingConfirmationByEmail(BookingResponse bookingResponse);
    void sendCancellationEmail(Booking booking);
    void sendUpdateEmail(Booking booking);
}
