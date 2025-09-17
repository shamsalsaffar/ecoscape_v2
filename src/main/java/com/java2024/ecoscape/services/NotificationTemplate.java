package com.java2024.ecoscape.services;

public abstract class NotificationTemplate {

    //access modifier protected är en bättre praxis i template pattern, det gör att bara child klasserna kan override abstrakta metoderna
    protected abstract void buildHeader();

    protected abstract void buildBody(Long bookingId);

    protected abstract void buildFooter();



}
