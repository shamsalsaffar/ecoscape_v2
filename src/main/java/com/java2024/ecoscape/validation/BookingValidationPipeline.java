package com.java2024.ecoscape.validation;

import com.java2024.ecoscape.dto.BookingRequest;
import com.java2024.ecoscape.models.Listing;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BookingValidationPipeline {
    private final List<BookingValidator>validators;

    public BookingValidationPipeline(List<BookingValidator> validators){
        this.validators = validators;
    }
    public List<String> validateAll(BookingRequest bookingRequest, Listing listing) {
        List<String> allErrors = new ArrayList<>();
        for (BookingValidator validator : validators){
            List<String>errors = validator.validate(bookingRequest, listing);
            if (errors != null && !errors.isEmpty()){
                allErrors.addAll(errors);
            }

        }
        return allErrors;
    }
}
