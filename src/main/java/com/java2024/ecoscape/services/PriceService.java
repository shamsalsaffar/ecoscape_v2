package com.java2024.ecoscape.services;

import com.java2024.ecoscape.models.Listing;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class PriceService {

    @Value("${service.fee:0.1}")
    private BigDecimal serviceFeeRate;

    @Data
    @Builder
    public static class PriceBreakdown {
        private long nights;
        private BigDecimal pricePerNight;
        private BigDecimal nightlySubtotal;  // pricePerNight * nights
        private BigDecimal cleaningFee;      // Från listing
        private BigDecimal serviceFeeAmount; // nightlySubtotal * 0.10
        private BigDecimal total;            // nightlySubtotal + cleaning + service

    }

    public PriceBreakdown calculate(Listing listing, LocalDate startDate, LocalDate endDate) {
        if (listing == null) throw new IllegalArgumentException("Listing cannot be null");
        if (startDate == null || endDate == null)
            throw new IllegalArgumentException("startDate and endDate cannot be null");

        long nights = ChronoUnit.DAYS.between(startDate, endDate);
        if (nights <= 0) throw new IllegalArgumentException("nights must be greater than 0");

        BigDecimal pricePerNight = money(listing.getPricePerNight());
        BigDecimal cleaning = money(listing.getCleaningFee());

        BigDecimal nightlySubtotal = pricePerNight
                .multiply(BigDecimal.valueOf(nights))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal rate = (serviceFeeRate != null) ? serviceFeeRate : BigDecimal.ZERO;

        BigDecimal service = nightlySubtotal
                .multiply(rate)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal total = nightlySubtotal
                .add(cleaning)
                .add(service)
                .setScale(2, RoundingMode.HALF_UP);

        return PriceBreakdown.builder()
                .nights(nights)
                .pricePerNight(pricePerNight)
                .nightlySubtotal(nightlySubtotal)
                .cleaningFee(cleaning)
                .serviceFeeAmount(service)
                .total(total)
                .build();
    }

    private BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }

}




