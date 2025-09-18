package com.java2024.ecoscape.services;

import com.java2024.ecoscape.models.Booking;

public abstract class NotificationTemplate {

    //access modifier protected är en bättre praxis i template pattern, det gör att bara child klasserna kan override abstrakta metoderna
    protected abstract String buildHeader();

    protected abstract String buildBody(Booking booking);

    protected abstract String buildFooter();

    public String buildMessage(Booking booking) {
        return buildHeader() + "\n"
                + buildBody(booking) + "\n"
                + buildFooter();
    }

}
