package com.java2024.ecoscape.services;

import com.java2024.ecoscape.dto.BookingRequest;
import com.java2024.ecoscape.dto.BookingResponse;
import com.java2024.ecoscape.exceptions.UnauthorizedException;
import com.java2024.ecoscape.models.*;
import com.java2024.ecoscape.repositories.BookingRepository;
import com.java2024.ecoscape.repositories.ListingRepository;
import com.java2024.ecoscape.repositories.UserRepository;
import com.java2024.ecoscape.validation.BookingValidationPipeline;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static com.java2024.ecoscape.models.Status.*;

@Service
public class BookingService {

    private final EmailService emailService;
    @Value("${service.fee:0.1}")
    private Double serviceFee;

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final ListingAvailableDatesService listingAvailableDatesService;
    private final AuthenticationService authenticationService;
    private final BookingValidationPipeline bookingValidationPipeline;

    public BookingService(EmailService emailService, BookingRepository bookingRepository,
                          UserRepository userRepository, ListingRepository listingRepository,
                          ListingAvailableDatesService listingAvailableDatesService,
                          AuthenticationService authenticationService, BookingValidationPipeline bookingValidationPipeline  ) {
        this.emailService = emailService;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
        this.listingAvailableDatesService = listingAvailableDatesService;
        this.authenticationService = authenticationService;
        this.bookingValidationPipeline = bookingValidationPipeline;
    }
    // från DB (entity ) till DTO och api
    public BookingResponse convertBookingEntityToBookingResponse(Booking booking ) {
        BookingResponse bookingResponse = new BookingResponse();
        bookingResponse.setBookingId(booking.getId());
        bookingResponse.setUserId(booking.getUser().getId());
        bookingResponse.setListingId(booking.getListing().getId());
        bookingResponse.setListingname(booking.getListing().getName());
        bookingResponse.setFirstName(booking.getFirstName());
        bookingResponse.setLastName(booking.getLastName());
        bookingResponse.setUsersContactPhoneNumber(booking.getUsersContactPhoneNumber());
        bookingResponse.setUsersContactEmail(booking.getUsersContactEmail());
        bookingResponse.setStartDate(booking.getStartDate());
        bookingResponse.setEndDate(booking.getEndDate());
        bookingResponse.setStatus(booking.getStatus());
        bookingResponse.setGuests(booking.getGuests());
        bookingResponse.setPricePerNight(booking.getListing().getPricePerNight());
        bookingResponse.setCleaningFee(booking.getListing().getCleaningFee());
        long nights = ChronoUnit.DAYS.between(booking.getStartDate(), booking.getEndDate());
        BigDecimal serviceFeeInKr = booking.getListing().getPricePerNight()
                .multiply(BigDecimal.valueOf(nights))  // عدد الليالي
                .multiply(BigDecimal.valueOf(serviceFee)) // الخدمة
                .setScale(2, RoundingMode.HALF_UP);
        bookingResponse.setWebsiteFee(serviceFeeInKr);

        bookingResponse.setTotalPrice(booking.getTotalPrice());


        return bookingResponse;
    }
    //ta emot data fron frontend api och spara i DB
    public Booking convertBookingRequestToBookingEntity(BookingRequest bookingRequest, Listing listing) {
        Booking booking = new Booking();
        booking.setFirstName(bookingRequest.getFirstName());
        booking.setLastName(bookingRequest.getLastName());
        booking.setUsersContactEmail(bookingRequest.getUsersContactEmail());
        booking.setUsersContactPhoneNumber(bookingRequest.getUsersContactPhoneNumber());
        booking.setStartDate(bookingRequest.getStartDate());
        booking.setEndDate(bookingRequest.getEndDate());
        booking.setGuests(bookingRequest.getGuests());

        // ensure the listing do not empty
        if (listing == null) {
            throw new IllegalArgumentException("Listing cannot be null when creating a booking.");
        }

        // تعيين القائمة للحجز
        booking.setListing(listing);

        // calculate Total price
        booking.setTotalPrice(calculateTotalPrice(booking, listing));



        return booking;
    }
    private BigDecimal calculateTotalPrice(Booking booking, Listing listing) {
        long nights = ChronoUnit.DAYS.between(booking.getStartDate(), booking.getEndDate());
        BigDecimal nightsBD = BigDecimal.valueOf(nights);

        BigDecimal pricePerNight = listing.getPricePerNight() != null ? listing.getPricePerNight() : BigDecimal.ZERO;
        BigDecimal cleaningFee = listing.getCleaningFee() != null ? listing.getCleaningFee() : BigDecimal.ZERO;

        BigDecimal serviceFeeBD = pricePerNight.multiply(nightsBD).multiply(BigDecimal.valueOf(serviceFee));

        return pricePerNight.multiply(nightsBD)
                .add(cleaningFee)
                .add(serviceFeeBD)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional
    public BookingResponse createBooking(BookingRequest bookingRequest, Long listingId) {
        // Find user and listing
        User authenticateUser = authenticationService.authenticateMethods();

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new NoSuchElementException("Listing not found"));

        if(authenticateUser.getId().equals(listing.getUser().getId())) {
            throw new IllegalArgumentException("You can't book your own listing!");

        }

        List<String> errors = bookingValidationPipeline.validateAll(bookingRequest, listing);
        if(!errors.isEmpty()) {
            throw new com.java2024.ecoscape.validation.BusinessValidationException(errors);
        }



        Booking booking = convertBookingRequestToBookingEntity(bookingRequest, listing);
        booking.setUser(authenticateUser);
        booking.setListing(listing);
        booking.setStatus(CONFIRMED);
        booking.setFirstName(bookingRequest.getFirstName());
        booking.setLastName(bookingRequest.getLastName());

        // blokera available dates i samband med booking
        listingAvailableDatesService.blockAvailableDatesAfterBooking(listingId, booking);
        // save booking to db
        Booking savedBooking = bookingRepository.save(booking);

        // تحويل الكيان إلى استجابة
        BookingResponse bookingResponse = convertBookingEntityToBookingResponse(booking);

        // إضافة الرسالة إلى الاستجابة
        bookingResponse.setMessage("The booking number " + booking.getId() + "\n has been confirmed. A confirmation email has been sent.");
        // Send confirmation email
       // sendBookingConfirmationByEmail(bookingResponse);
        return bookingResponse;

    }
    public void sendBookingConfirmationByEmail(BookingResponse bookingResponse) {
        String to = bookingResponse.getUsersContactEmail();
        String subject = "Booking Confirmation - EcoScape";
        String text = "Hello " + bookingResponse.getFirstName() + "!\n\n" +
                "We are pleased to inform you that your booking with EcoScape has been successfully confirmed. Below are the details of your booking:\n" +
                // "Booking ID: " + bookingResponse.getBookingId() + "\n" +
                //"Listing ID: " + bookingResponse.getListingId() + "\n" +
                //"Thank you for choosing EcoScape. We are excited to have you stay with us and look forward to making your experience memorable.\n\n" +
                bookingResponse.toString() + "\n" +
                "If you have any questions, feel free to contact us.\n"+
                "Best regards,\nThe EcoScape Team";

        emailService.sendEmail(to, subject, text);
    }



    // method to get all booking
    public List<BookingRequest> getAllbookings() {
        User authenticateUser = authenticationService.authenticateMethods();

        List<Booking> bookings = bookingRepository.findAll(); // Fetch all bookings from the repository
        return bookings.stream()
                .map(this::convertBookingEntityToBookingRequest) // Convert each Booking entity to BookingRequest DTO
                .collect(Collectors.toList());
    }

    public BookingResponse getBookingById(Long id) {
        User authenticateUser = authenticationService.authenticateMethods();

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        // تحويل Booking إلى BookingResponse

        return convertBookingEntityToBookingResponse(booking);
    }

    public List<BookingResponse> getBookingByUser() {
        User authenticateUser = authenticationService.authenticateMethods();

        List<Booking> bookings = bookingRepository.findByUser(authenticateUser);
        return bookings.stream()
                .map(this::convertBookingEntityToBookingResponse)
                .collect(Collectors.toList());
    }



    @Transactional
    public BookingResponse cancelBookingByUser(Long bookingId){

        User authenticateUser = authenticationService.authenticateMethods();

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if(!authenticateUser.getId().equals(booking.getUser().getId())) {
            throw new IllegalArgumentException("You can only cancel your own bookings!");
        }

        // التحقق مما إذا كان الحجز قد تم إلغاؤه مسبقًا
        if (booking.getStatus() == Status.CANCELLED_BY_USER || booking.getStatus() == Status.CANCELLED_BY_HOST) {
            throw new RuntimeException("This booking has already been cancelled.");
        }

        booking.setStatus(CANCELLED_BY_USER);
        listingAvailableDatesService.restoreAvailableDateRange(booking.getListing().getId(), booking.getStartDate(), booking.getEndDate());
        listingAvailableDatesService.mergeListingAvailableDates(booking.getListing().getId());
        bookingRepository.save(booking);
        // إرسال تأكيد الإلغاء بالبريد الإلكتروني
        sendCancellationEmail(booking);
        // تحويل الكيان إلى استجابة
        BookingResponse bookingResponse = convertBookingEntityToBookingResponse(booking);

        // إضافة الرسالة إلى الاستجابة
        bookingResponse.setMessage("The booking number " + booking.getId() + " has been cancelled. A confirmation email has been sent.");

        return bookingResponse;
    }

    @Transactional
    public BookingResponse cancelBookingByHost(Long bookingId) {
        User authenticateUser = authenticationService.authenticateMethods();

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        // التحقق مما إذا كان الحجز قد تم إلغاؤه مسبقًا
        if (booking.getStatus() == Status.CANCELLED_BY_USER || booking.getStatus() == Status.CANCELLED_BY_HOST) {
            throw new RuntimeException("This booking has already been cancelled.");
        }
        booking.setStatus(CANCELLED_BY_HOST);
        listingAvailableDatesService.restoreAvailableDateRange(booking.getListing().getId(), booking.getStartDate(), booking.getEndDate());
        listingAvailableDatesService.mergeListingAvailableDates(booking.getListing().getId());
        bookingRepository.save(booking);
        // إرسال تأكيد الإلغاء بالبريد الإلكتروني
        sendCancellationEmail(booking);

        // تحويل الكيان إلى استجابة
        BookingResponse bookingResponse = convertBookingEntityToBookingResponse(booking);

        // إضافة الرسالة إلى الاستجابة
        bookingResponse.setMessage("The booking number " + booking.getId() + " has been cancelled. A confirmation email has been sent.");

        return bookingResponse;
    }

    private void sendCancellationEmail(Booking booking  ) {
        String to = booking.getUsersContactEmail();
        String subject = "Confirm cancellation of booking";
        String text = "Hello" + booking.getFirstName() + "!\n\n" +
                "We would like to inform you that the booking number "+ booking.getId() + " in "+ booking.getListing().getId()
                + " you made with us has been cancelled.\n"+ "We apologize for any inconvenience this may cause.\n\n"
                + "If you need any assistance, please don't hesitate to contact us.\n\n"+ "Best regards,\nThe Ecoscape Team.";

        // Sending the email using the EmailService
        emailService.sendEmail(to, subject, text);
    }

    // DB (entity )till request DTO
    private BookingRequest convertBookingEntityToBookingRequest(Booking booking) {
        BookingRequest bookingRequest = new BookingRequest();

        bookingRequest.setFirstName(booking.getFirstName());
        bookingRequest.setLastName(booking.getLastName());

        bookingRequest.setUsersContactEmail(booking.getUsersContactEmail());
        bookingRequest.setUsersContactPhoneNumber(booking.getUsersContactPhoneNumber());
        bookingRequest.setStartDate(booking.getStartDate());
        bookingRequest.setEndDate(booking.getEndDate());
        bookingRequest.setGuests(booking.getGuests());
        return bookingRequest;
    }


    @Transactional
    public BookingResponse updateBooking(BookingRequest bookingRequest, Long bookingId, Listing listing, User user) {
        User authenticateUser = authenticationService.authenticateMethods();

        // Find Booking
        Booking existingBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found"));

        // Check if the authenticated user is an ADMIN or if the user is the host of the current listing
        if (!(authenticateUser.getRoles().equals(Role.ADMIN) || authenticateUser.getId().equals(listing.getUser().getId()))) {
            throw new UnauthorizedException("You dont have premission to update this booking.");
        }


        // list to collect errors so they all appeared at one
        List<String>errors = new ArrayList<>();

        // control can not have guests more than capacity in listing
        if (bookingRequest.getGuests() > listing.getCapacity()) {
            errors.add("The number of guests exceeds the capacity for this listing.");
        }

        // control can not be end date before start date
        if (bookingRequest.getEndDate().isBefore(bookingRequest.getStartDate())) {
            errors.add("Check-out date cannot be before check-in date.");
        }

        // if have two errors together, sen exception with what is wrong
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errors));

        }



        try {
            // Försök att uppdatera första namnet
            existingBooking.setFirstName(bookingRequest.getFirstName());
            System.out.println("First name updated to: " + existingBooking.getFirstName());
            bookingRepository.save(existingBooking);
        } catch (Exception e) {
            System.out.println("Error updating first name: " + e.getMessage());
            throw e; // Rulla tillbaka om något går fel
        }


        if (bookingRequest.getLastName() != null) {
            existingBooking.setLastName(bookingRequest.getLastName());
        }

        if (bookingRequest.getUsersContactPhoneNumber() != null) {
            existingBooking.setUsersContactPhoneNumber(bookingRequest.getUsersContactPhoneNumber());
        }

        if (bookingRequest.getUsersContactEmail() != null) {
            existingBooking.setUsersContactEmail(bookingRequest.getUsersContactEmail());
        }

        if (bookingRequest.getStartDate() != null) {
            existingBooking.setStartDate(bookingRequest.getStartDate());
        }

        if (bookingRequest.getEndDate() != null) {
            existingBooking.setEndDate(bookingRequest.getEndDate());
        }

        if (bookingRequest.getStatus() != null) {
            existingBooking.setStatus(bookingRequest.getStatus());
        }

        if (bookingRequest.getGuests() != null) {
            existingBooking.setGuests(bookingRequest.getGuests());
        }

        // save update booking
        Booking updatedBooking = bookingRepository.save(existingBooking);

        // Send email to confirm the update
        sendUpdateEmail(updatedBooking);

        // Add a response massage
        BookingResponse bookingResponse = convertBookingEntityToBookingResponse(updatedBooking);
        bookingResponse.setMessage("The booking number " + updatedBooking.getId() + " has been updated. A confirmation email has been sent.");

        return bookingResponse;

    }

    public BookingResponse updateBookingContactInfo(Long bookingId, BookingRequest bookingRequest) {
        User authenticateUser = authenticationService.authenticateMethods();


        // الحصول على الحجز من قاعدة البيانات
        // Find the booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found"));

        if (!authenticateUser.getId().equals(booking.getUser().getId())) {
            throw new UnauthorizedException("You don't have permission to update this booking.");
        }

        // list to collect errors so they all appeared at one
        List<String>errors = new ArrayList<>();


        // تحديث الحقول فقط إذا كانت غير فارغة
        // update fields
        if (bookingRequest.getFirstName() != null) {
            booking.setFirstName(bookingRequest.getFirstName());
        }
        if (bookingRequest.getLastName() != null) {
            booking.setLastName(bookingRequest.getLastName());
        }
        if (bookingRequest.getUsersContactEmail() != null) {
            booking.setUsersContactEmail(bookingRequest.getUsersContactEmail());
        }
        if (bookingRequest.getUsersContactPhoneNumber() != null) {
            booking.setUsersContactPhoneNumber(bookingRequest.getUsersContactPhoneNumber());
        }
        // إرسال تأكيد الإلغاء بالبريد الإلكتروني
        // Send email to confirm the update
        sendUpdateEmail(booking);

        // تحويل الكيان إلى استجابة
        // convert to response
        BookingResponse bookingResponse = convertBookingEntityToBookingResponse(booking);


        // إضافة الرسالة إلى الاستجابة
        bookingResponse.setMessage("The booking number " + booking.getId() + " has been update. A update email has been sent.");

        return bookingResponse;
    }
    private void sendUpdateEmail(Booking booking) {
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

        // إرسال البريد الإلكتروني باستخدام الخدمة
        // send mail by java mail
        emailService.sendEmail(to, subject, text.toString());
    }







    public ResponseEntity<String> deleteBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .map(booking -> {
                    bookingRepository.delete(booking);
                    return ResponseEntity.ok("Booking deleted successfully"); // Return success message with HTTP 200 status
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking not found")); // Return error message with HTTP 404 status
    }
}