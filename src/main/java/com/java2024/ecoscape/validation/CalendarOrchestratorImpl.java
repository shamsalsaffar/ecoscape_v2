package com.java2024.ecoscape.validation;

import com.java2024.ecoscape.models.Booking;
import com.java2024.ecoscape.models.Listing;
import com.java2024.ecoscape.services.ListingAvailableDatesService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.java2024.ecoscape.exceptions.BusinessValidationException;

import java.time.LocalDate;

@Component
public class CalendarOrchestratorImpl implements CalendarOrchestrator {
    private final ListingAvailableDatesService listingAvailableDatesService;
    public CalendarOrchestratorImpl(ListingAvailableDatesService listingAvailableDatesService) {
        this.listingAvailableDatesService = listingAvailableDatesService;
    }

    // Ta bort gamla datum, sätt nya datum, blockera dem oc slå ihop klandar
    @Override
    @Transactional
    public void reschedule (Listing listing, Booking booking, LocalDate newStart, LocalDate newEnd ) {
        listingAvailableDatesService.restoreAvailableDateRange(listing.getId(), booking.getStartDate(), booking.getEndDate());
        listingAvailableDatesService.mergeListingAvailableDates(listing.getId());

        booking.setStartDate(newStart);
        booking.setEndDate(newEnd);

        listingAvailableDatesService.blockAvailableDatesAfterBooking(listing.getId(), booking);
        listingAvailableDatesService.mergeListingAvailableDates(listing.getId());

    }


    // Släpp bokningens datum tillbaka till kalendarn
    @Override
    @Transactional
    public void release (Listing listing, Booking booking) {
        listingAvailableDatesService.restoreAvailableDateRange(listing.getId(), booking.getStartDate(), booking.getEndDate());

        listingAvailableDatesService.mergeListingAvailableDates(listing.getId());
    }


    @Override
    @Transactional
    public void tryRescheduleOrThrow (Listing listing, Booking booking, LocalDate newStart, LocalDate newEnd ) {
        boolean available = listingAvailableDatesService.checkAvailability(listing.getId(), newStart, newEnd);
        if (!available) {
            throw new BusinessValidationException(
                    java.util.List.of("The listing is unavaliable for the requested dates.")
            );
        }
        reschedule(listing, booking, newStart, newEnd);
    }
}
