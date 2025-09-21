package com.java2024.ecoscape.websockets.templates;

import com.java2024.ecoscape.models.Booking;

public class BookingDetailsUpdateNotificationTemplate extends NotificationTemplate {

    @Override
    protected String buildHeader(Booking booking) {
        setHeader("The details are updated for booking " + booking.getId() +"!");
        return getHeader();
    }

    @Override
    protected String buildBody() {
        setBody("Booking details for these booking has been updated. The booking status and details are now current and reflect the latest updates.");
        return getBody();
    }

    @Override
    protected String buildFooter() {
        setFooter("Please review the changes.");
        return getFooter();
    }
}
