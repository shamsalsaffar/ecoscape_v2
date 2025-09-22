package com.java2024.ecoscape.emails;

import com.java2024.ecoscape.dto.BookingResponse;
import com.java2024.ecoscape.models.Booking;
import com.java2024.ecoscape.services.EmailService;
import org.springframework.stereotype.Service;

@Service
public class BookingEmailServiceImpl implements  BookingEmailService {

    private final EmailService emailService;
    public BookingEmailServiceImpl(EmailService emailService) {
        this.emailService = emailService;

    }

    @Override
    public void sendBookingConfirmationByEmail(BookingResponse bookingResponse) {
        String to = bookingResponse.getUsersContactEmail();
        String subject = "Booking Confirmation - EcoScape";
        String text = "Hello " + bookingResponse.getFirstName() + "!\n\n" +
                "We are pleased to inform you that your booking with EcoScape has been successfully confirmed. Below are the details of your booking:\n" +
                bookingResponse.toString() + "\n" +
                "If you have any questions, feel free to contact us.\n"+
                "Best regards,\nThe EcoScape Team";

        emailService.sendEmail(to, subject, text);
    }

    @Override
    public void sendUpdateEmail(Booking booking) {
        String to = booking.getUsersContactEmail();
        String subject = "Your booking details have been updated";

        StringBuilder text = new StringBuilder();
        text.append("Hello ").append(booking.getFirstName()).append(",\n\n");
        text.append("We would like to inform you that the booking number ")
                .append(booking.getId())
                .append(" for the listing ID ")
                .append(booking.getListing().getId())
                .append(" has been successfully updated.\n\n");

        text.append("Here are your updated contact details:\n");
        text.append("Full Name: ").append(booking.getFirstName()).append(" ").append(booking.getLastName()).append("\n");
        text.append("Email: ").append(booking.getUsersContactEmail()).append("\n");
        text.append("Phone: ").append(booking.getUsersContactPhoneNumber()).append("\n\n");

        text.append("If you have any questions or need further assistance, please don't hesitate to contact us.\n\n");
        text.append("Best regards,\nThe Ecoscape Team.");
        // send mail by java mail
        emailService.sendEmail(to, subject, text.toString());
    }

    @Override
    public void sendCancellationEmail(Booking booking  ) {
        String to = booking.getUsersContactEmail();
        String subject = "Confirm cancellation of booking";
        String text = "Hello" + booking.getFirstName() + "!\n\n" +
                "We would like to inform you that the booking number "+ booking.getId() + " in "+ booking.getListing().getId()
                + " you made with us has been cancelled.\n"+ "We apologize for any inconvenience this may cause.\n\n"
                + "If you need any assistance, please don't hesitate to contact us.\n\n"+ "Best regards,\nThe Ecoscape Team.";

        // Sending the email using the EmailService
        emailService.sendEmail(to, subject, text);
    }
}
