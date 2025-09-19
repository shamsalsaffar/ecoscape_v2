package com.java2024.ecoscape.websockets.templates;

import com.java2024.ecoscape.models.Booking;

public class BookingCreationNotificationTemplate extends NotificationTemplate {

    @Override
    protected String buildHeader() {
        setHeader("Booking Confirmed!");
        return getHeader();
    }

    @Override
    protected String buildBody(Booking booking) {
            setBody("The booking with ID " + booking.getId() + " has been confirmed.");
            return getBody();
        }


    @Override
    protected String buildFooter() {
        setFooter("Thank you for using Ecoscape!");
        return getFooter();
    }
}
