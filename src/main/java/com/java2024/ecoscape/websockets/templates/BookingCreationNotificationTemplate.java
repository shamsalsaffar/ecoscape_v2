package com.java2024.ecoscape.websockets.templates;

import com.java2024.ecoscape.models.Booking;

public class BookingCreationNotificationTemplate extends NotificationTemplate {

    @Override
    protected String buildHeader() {
        return "Booking Confirmed!";
    }

    @Override
    protected String buildBody(Booking booking) {
            return "The booking with ID " + booking.getId() + " has been confirmed.";
        }


    @Override
    protected String buildFooter() {
        return "Thank you for using Ecoscape!";
    }
}
