package com.java2024.ecoscape.websockets.templates;

import com.java2024.ecoscape.models.Booking;

public class BookingCancellationNotificationTemplate extends NotificationTemplate {

    @Override
    protected String buildHeader() {
        setHeader("Booking Cancelled");
        return getHeader();
    }

    @Override
    protected String buildBody(Booking booking) {
        setBody("The booking with ID" + booking.getId() + "has been cancelled.");
        return getBody();
    }

    @Override
    protected String buildFooter() {
       setFooter("Please contact support if you have any questions.");
       return getFooter();
    }
}
