package com.java2024.ecoscape.websockets.templates;

import com.java2024.ecoscape.models.Booking;

public class BookingCancellationNotificationTemplate extends NotificationTemplate {

    @Override
    protected String buildHeader(Booking booking) {
        setHeader("Booking " + booking.getId() + " is cancelled!");
        return getHeader();
    }

    @Override
    protected String buildBody() {
        setBody("The booking has been cancelled. The booking status is now updated to reflect the cancellation.");
        return getBody();
    }

    @Override
    protected String buildFooter() {
       setFooter("Please contact support if you have any questions.");
       return getFooter();
    }
}
