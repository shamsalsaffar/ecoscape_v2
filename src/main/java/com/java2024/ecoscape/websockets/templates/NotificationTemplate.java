package com.java2024.ecoscape.websockets.templates;

import com.java2024.ecoscape.dto.NotificationDTO;
import com.java2024.ecoscape.models.Booking;

public abstract class NotificationTemplate {

    private String header;

    private String body;

    private String footer;

    //access modifier protected är en bättre praxis i template pattern, det gör att bara child klasserna kan override abstrakta metoderna
    protected abstract String buildHeader(Booking booking);

    protected abstract String buildBody();

    protected abstract String buildFooter();

    public NotificationDTO buildNotification(Booking booking) {
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setBookingId(booking.getId());
        notificationDTO.setTitle(buildHeader(booking));
        notificationDTO.setMessage(buildBody() + "\n" + buildFooter());
        return notificationDTO;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getFooter() {
        return footer;
    }

    public void setFooter(String footer) {
        this.footer = footer;
    }
}
