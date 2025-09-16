package com.java2024.ecoscape.validation;

import com.java2024.ecoscape.models.Booking;
import com.java2024.ecoscape.models.Listing;

import java.time.LocalDate;

public interface CalendarOrchestrator {

    // Reshemalägg bokingen, uppdatera kalendern
    void reschedule (Listing listing, Booking booking, LocalDate newStart, LocalDate newEnd );

    // Släpp boknngs datum tillbaka till listingens kalender
    void release (Listing listing, Booking booking);

    //Verifiera att nya datum är lediga, om nej kasta fel
    void tryRescheduleOrThrow (Listing listing, Booking booking, LocalDate newStart, LocalDate newEnd);
}
