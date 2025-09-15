package com.java2024.ecoscape.validation;

import com.java2024.ecoscape.dto.BookingRequest;
import com.java2024.ecoscape.models.Listing;

import java.util.List;

public interface BookingValidator {
    List<String> validate(BookingRequest bookingRequest, Listing listing);
}
