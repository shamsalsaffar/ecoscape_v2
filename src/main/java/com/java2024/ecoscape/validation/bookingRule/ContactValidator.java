package com.java2024.ecoscape.validation.bookingRule;

import com.java2024.ecoscape.dto.BookingRequest;
import com.java2024.ecoscape.models.Listing;
import com.java2024.ecoscape.validation.BookingValidator;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

// importerar Spring's StringUtils för textvalidering
import org.springframework.util.StringUtils;

@Component
@Order(3)
public class ContactValidator implements BookingValidator {

    private static final String NAME_REGEX = "^[a-zA-ZåäöÅÄÖéÉèÈüÜß\\-\\s']{1,50}$";
    private static final String PHONE_REGEX =   "^\\+?[1-9]\\d{7,14}$";
    private static final String EMAIL_REGEX =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@[a-zA-Z0-9-]+(?:\\.[a-zA-Z]{2,})+$";

    @Override
    public List<String> validate (BookingRequest bookingRequest, Listing listing) {
        List<String> errors = new ArrayList<>();

        // First Name
        String firstName = bookingRequest.getFirstName();
        if (!StringUtils.hasText(firstName)) {
            errors.add("First name is required.");
        } else if (!firstName.matches(NAME_REGEX)) {
            errors.add("First name contains invalid characters or is too long.");
        }

        // Last Name
        String lastname = bookingRequest.getLastName();
        if (!StringUtils.hasText(lastname)) {
            errors.add("Last name is required.");
        } else if (!bookingRequest.getLastName().matches(NAME_REGEX)) {
            errors.add("Last name contains invalid characters or too long.");

        }


        // Phone
        String phone = bookingRequest.getUsersContactPhoneNumber();
        if (!StringUtils.hasText(phone)) {
            errors.add("Phone number is required.");
        } else if (!phone.matches(PHONE_REGEX)) {
            errors.add("That's not a valid phone number.");
        }

        // Email
        String email = bookingRequest.getUsersContactEmail();
        if (!StringUtils.hasText(email)) {
            errors.add("Email is required.");
        } else if (!email.matches(EMAIL_REGEX)) {
            errors.add("That's not a valid e-mail address.");
        } else if (email.length() > 30) {
            errors.add("Email cannot be longer than 30 characters.");
        }
        return errors;

    }

}