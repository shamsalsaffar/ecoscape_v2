package com.java2024.ecoscape.websockets.templates;

import com.java2024.ecoscape.models.Booking;

public class BookingDetailsUpdateNotificationTemplate extends NotificationTemplate {

    @Override
    protected String buildHeader() {
        setHeader("Booking Details Updated!");
        return getHeader();
    }

    @Override
    protected String buildBody(Booking booking) {
        setBody("Booking details for the booking with ID" + booking.getId() + "has been updated.");
        return getBody();
    }

    @Override
    protected String buildFooter() {
        setFooter("Please review the changes.");
        return getFooter();
    }
}
