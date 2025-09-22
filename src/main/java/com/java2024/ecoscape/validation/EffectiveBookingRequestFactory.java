package com.java2024.ecoscape.validation;


import com.java2024.ecoscape.dto.BookingRequest;
import com.java2024.ecoscape.models.Booking;
import org.springframework.stereotype.Component;




@Component
public class EffectiveBookingRequestFactory {

    public BookingRequest forUpdateAll (Booking existing, BookingRequest incoming ) {

        BookingRequest effective = new BookingRequest();

        // Kontakt
        effective.setFirstName(pick(incoming.getFirstName(), existing.getFirstName()));
        effective.setLastName(pick(incoming.getLastName(), existing.getLastName()));
        effective.setUsersContactEmail(pick(incoming.getUsersContactEmail(), existing.getUsersContactEmail()));
        effective.setUsersContactPhoneNumber(pick(incoming.getUsersContactPhoneNumber(), existing.getUsersContactPhoneNumber()));

        // Datum & gäster
        effective.setStartDate(pick(incoming.getStartDate(), existing.getStartDate()));
        effective.setEndDate(pick(incoming.getEndDate(), existing.getEndDate()));
        effective.setGuests(pick(incoming.getGuests(), existing.getGuests()));
        effective.setStatus(pick(incoming.getStatus(), existing.getStatus()));
        return effective;

    }

    public BookingRequest forUpdateContact(Booking existing, BookingRequest incoming) {
        BookingRequest effective = new BookingRequest();

        // Kontakt
        effective.setFirstName(pick(incoming.getFirstName(), existing.getFirstName()));
        effective.setLastName(pick(incoming.getLastName(), existing.getLastName()));
        effective.setUsersContactEmail(pick(incoming.getUsersContactEmail(), existing.getUsersContactEmail()));
        effective.setUsersContactPhoneNumber(pick(incoming.getUsersContactPhoneNumber(),existing.getUsersContactPhoneNumber()));

        effective.setStartDate(existing.getStartDate());
        effective.setEndDate(existing.getEndDate());
        effective.setGuests(existing.getGuests());
        effective.setStatus(existing.getStatus());



        return effective;
    }


    // Support method som väljer värdet från incoming (frontend) om det inte är null, annars användas fallback
    private static <T> T pick(T incoming, T fallback){
        return incoming != null ? incoming : fallback;
    }

}
