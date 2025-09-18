package com.java2024.ecoscape.websockets.templates;

import com.java2024.ecoscape.models.Booking;

public class BookingCancellationNotificationTemplate extends NotificationTemplate {

    @Override
    protected String buildHeader() {
        return "Booking Cancelled";
    }

    @Override
    protected String buildBody(Booking booking) {
        return "The booking with ID" + booking.getId() + "has been cancelled.";
    }

    @Override
    protected String buildFooter() {
        return "Please contact support if you have any questions.";
    }
}
