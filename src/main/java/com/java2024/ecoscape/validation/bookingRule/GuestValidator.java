package com.java2024.ecoscape.validation.bookingRule;

import com.java2024.ecoscape.dto.BookingRequest;
import com.java2024.ecoscape.models.Listing;
import com.java2024.ecoscape.validation.BookingValidator;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Order(2)
public class GuestValidator implements BookingValidator {

    @Override
    public List<String> validate (BookingRequest bookingRequest, Listing listing) {
        List<String> errors = new ArrayList<>();

        // Skapa local variable för bättre läsbarhet
        Integer guests = bookingRequest.getGuests();

        if (guests == null) {
            errors.add("Guests is required.");
            return errors; // här retun direkt för att undvika NullPointerException
        }

        if (guests < 1 ){
            errors.add("Guests must be at least 1.");
        }

        if (guests > 10 ){
            errors.add("Guests cannot be more than 10.");
        }
        if (listing.getCapacity() != null && guests > listing.getCapacity()){
            errors.add("The number of guests exceeds the capacity for this listing.");
        }

        return errors;
        }



}
