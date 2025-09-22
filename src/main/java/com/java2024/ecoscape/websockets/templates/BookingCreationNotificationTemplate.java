package com.java2024.ecoscape.websockets.templates;

import com.java2024.ecoscape.models.Booking;

public class BookingCreationNotificationTemplate extends NotificationTemplate {

    @Override
    protected String buildHeader(Booking booking) {
        setHeader("Booking " + booking.getId() + " is confirmed!");
        return getHeader();
    }

    @Override
    protected String buildBody() {
            setBody("We’re excited to let you know that the booking has been confirmed.");
            return getBody();
        }


    @Override
    protected String buildFooter() {
        setFooter("Thank you for using Ecoscape!");
        return getFooter();
    }
}
