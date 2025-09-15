package com.java2024.ecoscape.validation.bookingRule;

import com.java2024.ecoscape.dto.BookingRequest;
import com.java2024.ecoscape.models.Listing;
import com.java2024.ecoscape.services.ListingAvailableDatesService;
import com.java2024.ecoscape.validation.BookingValidator;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
@Component
@Order(1)
public class DateValidator implements BookingValidator {
    private  final ListingAvailableDatesService listingAvailableDatesService;

    public DateValidator(ListingAvailableDatesService listingAvailableDatesService) {
        this.listingAvailableDatesService = listingAvailableDatesService;

    }

    @Override
    public List<String> validate (BookingRequest bookingRequest, Listing listing) {
        List<String> errors = new ArrayList<>();

        // 1) check null
        if (bookingRequest.getStartDate() == null || bookingRequest.getEndDate() == null) {
            errors.add("Start date and end date are required");
            return errors;
        }

        // Skapa local varible för att undvika unropa returnerar en instans av localDate
        LocalDate startDate = bookingRequest.getStartDate();
        LocalDate endDate = bookingRequest.getEndDate();


        // 2) Datum relation
       if (!endDate.isAfter(startDate)) {
           errors.add("Check-out date must be after check-in date");
       }

       if (startDate.isBefore(LocalDate.now())) {
           errors.add("Check-in date cannot be in the past");
       }

       if (!errors.isEmpty())
           return errors;

       // 3) check listing available date
       boolean available = listingAvailableDatesService.checkAvailability(
               listing.getId(), startDate, endDate
       );
       if (!available) {
           errors.add("The listing is unavailable for the requested dates.");

       }
       return errors;

    }
}
