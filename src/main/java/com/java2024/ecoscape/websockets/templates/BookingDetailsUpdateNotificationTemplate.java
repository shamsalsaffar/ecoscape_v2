package com.java2024.ecoscape.websockets.templates;

import com.java2024.ecoscape.models.Booking;

public class BookingDetailsUpdateNotificationTemplate extends NotificationTemplate {


    @Override
    protected String buildHeader() {
        return "Booking Details Updated!";
    }

    @Override
    protected String buildBody(Booking booking) {
            return "Booking details for the booking with ID" + booking.getId() + "has been updated.";
    }

    @Override
    protected String buildFooter() {
        return "Please review the changes.";
    }
}
