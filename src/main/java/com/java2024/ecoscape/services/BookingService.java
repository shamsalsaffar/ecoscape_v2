package com.java2024.ecoscape.services;

import com.java2024.ecoscape.dto.BookingRequest;
import com.java2024.ecoscape.dto.BookingResponse;
import com.java2024.ecoscape.emails.BookingEmailService;
import com.java2024.ecoscape.exceptions.BusinessValidationException;
import com.java2024.ecoscape.exceptions.UnauthorizedException;
import com.java2024.ecoscape.models.*;
import com.java2024.ecoscape.repositories.BookingRepository;
import com.java2024.ecoscape.repositories.ListingRepository;
import com.java2024.ecoscape.repositories.UserRepository;
import com.java2024.ecoscape.validation.BookingValidationPipeline;
import com.java2024.ecoscape.validation.CalendarOrchestrator;
import com.java2024.ecoscape.validation.EffectiveBookingRequestFactory;
import com.java2024.ecoscape.websockets.service.PushNotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.java2024.ecoscape.mappers.BookingMapper;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static com.java2024.ecoscape.models.Status.*;

@Service
public class BookingService {

    private final EmailService emailService;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final ListingAvailableDatesService listingAvailableDatesService;
    private final AuthenticationService authenticationService;
    private final BookingValidationPipeline bookingValidationPipeline;
    private final EffectiveBookingRequestFactory effectiveBookingRequestFactory;
    private final CalendarOrchestrator calendarOrchestrator;
    private final PriceService priceService;
    private final PushNotificationService pushNotificationService;
    private  final BookingMapper bookingMapper;
    private final BookingEmailService bookingEmailService;

    public BookingService(EmailService emailService, BookingRepository bookingRepository,
                          UserRepository userRepository, ListingRepository listingRepository,
                          ListingAvailableDatesService listingAvailableDatesService,
                          AuthenticationService authenticationService, BookingValidationPipeline bookingValidationPipeline,
                          EffectiveBookingRequestFactory effectiveBookingRequestFactory,
                          CalendarOrchestrator calendarOrchestrator,
                          PriceService priceService, PushNotificationService pushNotificationService,
                          BookingMapper bookingMapper,
                          BookingEmailService bookingEmailService) {
        this.emailService = emailService;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
        this.listingAvailableDatesService = listingAvailableDatesService;
        this.authenticationService = authenticationService;
        this.bookingValidationPipeline = bookingValidationPipeline;
        this.effectiveBookingRequestFactory = effectiveBookingRequestFactory;
        this.calendarOrchestrator = calendarOrchestrator;
        this.priceService = priceService;
        this.pushNotificationService = pushNotificationService;
        this.bookingMapper = bookingMapper;
        this.bookingEmailService = bookingEmailService;


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
            throw new BusinessValidationException(errors);
        }

        Booking booking = bookingMapper.toEntity(bookingRequest, listing, authenticateUser, priceService);
        booking.setUser(authenticateUser);
        booking.setListing(listing);
        booking.setStatus(CONFIRMED);
        booking.setFirstName(bookingRequest.getFirstName());
        booking.setLastName(bookingRequest.getLastName());

        // blokera available dates i samband med booking
        listingAvailableDatesService.blockAvailableDatesAfterBooking(listingId, booking);
        // save booking to db
        Booking savedBooking = bookingRepository.save(booking);
        BookingResponse bookingResponse = bookingMapper.toResponse(savedBooking, priceService);
        bookingResponse.setMessage("The booking number " + booking.getId() + "\n has been confirmed. A confirmation email has been sent.");
        return bookingResponse;

    }


    // method to get all booking
    public List<BookingRequest> getAllbookings() {
        User authenticateUser = authenticationService.authenticateMethods();
        return bookingRepository.findAll()
                .stream()
                .map(bookingMapper ::toRequest)
                .collect(Collectors.toList());
    }


    public BookingResponse getBookingById(Long id) {
        User authenticateUser = authenticationService.authenticateMethods();
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        return bookingMapper.toResponse(booking, priceService);
    }


    public List<BookingResponse> getBookingByUser() {
        User authenticateUser = authenticationService.authenticateMethods();
        List<Booking> bookings = bookingRepository.findByUser(authenticateUser);
        return bookings.stream()
                .map(b -> bookingMapper.toResponse(b, priceService))
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

        if (booking.getStatus() == Status.CANCELLED_BY_USER || booking.getStatus() == Status.CANCELLED_BY_HOST) {
            throw new RuntimeException("This booking has already been cancelled.");
        }

        booking.setStatus(CANCELLED_BY_USER);
        listingAvailableDatesService.restoreAvailableDateRange(booking.getListing().getId(), booking.getStartDate(), booking.getEndDate());
        listingAvailableDatesService.mergeListingAvailableDates(booking.getListing().getId());
        bookingRepository.save(booking);

        pushNotificationService.notify(NotificationType.BOOKING_CANCELLATION, booking);

        // send email
        bookingEmailService.sendCancellationEmail(booking);
        BookingResponse bookingResponse = bookingMapper.toResponse(booking, priceService);

        // send Message with response
        bookingResponse.setMessage("The booking number " + booking.getId() + " has been cancelled. A confirmation email has been sent.");

        return bookingResponse;
    }

    @Transactional
    public BookingResponse cancelBookingByHost(Long bookingId) {
        User authenticateUser = authenticationService.authenticateMethods();

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        // check if this booking has not been cancelled previously
        if (booking.getStatus() == Status.CANCELLED_BY_USER || booking.getStatus() == Status.CANCELLED_BY_HOST) {
            throw new RuntimeException("This booking has already been cancelled.");
        }
        booking.setStatus(CANCELLED_BY_HOST);
        listingAvailableDatesService.restoreAvailableDateRange(booking.getListing().getId(), booking.getStartDate(), booking.getEndDate());
        listingAvailableDatesService.mergeListingAvailableDates(booking.getListing().getId());
        bookingRepository.save(booking);

        pushNotificationService.notify(NotificationType.BOOKING_CANCELLATION, booking);

        // send email
        bookingEmailService.sendCancellationEmail(booking);

        // تحويل الكيان إلى استجابة
        BookingResponse bookingResponse = bookingMapper.toResponse(booking, priceService);

        // send Message with response
        bookingResponse.setMessage("The booking number " + booking.getId() + " has been cancelled. A confirmation email has been sent.");

        return bookingResponse;
    }


    @Transactional
    public BookingResponse updateBooking(BookingRequest bookingRequest, Long bookingId, Listing listing, User user) {
        User authenticateUser = authenticationService.authenticateMethods();

        Booking existing = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found"));

        // 1) skapa ett effektive request av inkommande värden ( frontend) + med nuvarande bokning
        BookingRequest eff = effectiveBookingRequestFactory.forUpdateAll(existing, bookingRequest);

        // 2) validera
        List<String> errors = bookingValidationPipeline.validateAll(eff, listing);
        if (!errors.isEmpty()) throw new BusinessValidationException(errors);

        // 3) Upptäck om datum faktiskt har ändrat
        boolean datesChanged =
                !existing.getStartDate().equals(eff.getStartDate())
                        || !existing.getEndDate().equals(eff.getEndDate());

        // 4) om datum ändrats: kontrollera availabledatum och rechemlägg ( block/ merge)
        if (datesChanged) {

            calendarOrchestrator.tryRescheduleOrThrow(listing, existing, eff.getStartDate(), eff.getEndDate());

            // Räkna price igen efter ändrning
            PriceService.PriceBreakdown pb = priceService.calculate(
                    listing,
                    existing.getStartDate(),
                    existing.getEndDate()
            );
            existing.setTotalPrice(pb.getTotal());
        }

        // 5) Kontrollera kontaktuppgifterna
        bookingMapper.updateEntityFromRequest(eff, existing);

        // 6) uppdatera status om tillåts
        if (eff.getStatus() != null && eff.getStatus() != existing.getStatus()) {
            Status newStatus = eff.getStatus();
            switch (newStatus) {
                case CANCELLED_BY_HOST, CANCELLED_BY_USER -> {
                    calendarOrchestrator.release(listing, existing);
                }
                default -> { /* övriga statusfall vid behöv */ }
            }
            existing.setStatus(newStatus);
        }

        Booking saved = bookingRepository.save(existing);
        bookingEmailService.sendUpdateEmail(saved);

        BookingResponse bookingResponse = bookingMapper.toResponse(saved, priceService);
        bookingResponse.setMessage("The booking number " + saved.getId() + " has been updated. A confirmation email has been sent.");
        return bookingResponse;
    }


    public BookingResponse updateBookingContactInfo(Long bookingId, BookingRequest bookingRequest) {
        User authenticateUser = authenticationService.authenticateMethods();
        // Find the booking
        Booking existing = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found"));

        if (!authenticateUser.getId().equals(existing.getUser().getId())) {
            throw new UnauthorizedException("You don't have permission to update this booking.");
        }
        BookingRequest eff = effectiveBookingRequestFactory.forUpdateContact(existing, bookingRequest);


        List<String> errors = bookingValidationPipeline.validateContactOnly(eff, existing.getListing());
        if (!errors.isEmpty()) {
            throw new BusinessValidationException(errors);
        }
        bookingMapper.updateEntityFromRequest(eff, existing);

        Booking booking = bookingRepository.save(existing);

        pushNotificationService.notify(NotificationType.BOOKING_DETAILS_UPDATE, booking);

        // Send email to confirm the update
        bookingEmailService.sendUpdateEmail(booking);
        // convert to response
        BookingResponse bookingResponse = bookingMapper.toResponse(booking, priceService);
        // Addera SMS till response
        bookingResponse.setMessage("The booking number " + booking.getId() + " has been update. A update email has been sent.");
        return bookingResponse;
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