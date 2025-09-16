package com.java2024.ecoscape.services;

import com.java2024.ecoscape.dto.ListingAvailableDatesRequest;
import com.java2024.ecoscape.dto.ListingAvailableDatesResponse;
import com.java2024.ecoscape.models.Booking;
import com.java2024.ecoscape.models.Listing;
import com.java2024.ecoscape.models.ListingAvailableDates;
import com.java2024.ecoscape.models.User;
import com.java2024.ecoscape.repositories.ListingAvailableDatesRepository;
import com.java2024.ecoscape.repositories.ListingRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ListingAvailableDatesService {
    private final ListingAvailableDatesRepository listingAvailableDatesRepository;
    private final ListingRepository listingRepository;
    private final AuthenticationService authenticationService;

    public ListingAvailableDatesService(ListingAvailableDatesRepository listingAvailableDatesRepository, ListingRepository listingRepository, AuthenticationService authenticationService) {
        this.listingAvailableDatesRepository = listingAvailableDatesRepository;
        this.listingRepository = listingRepository;
        this.authenticationService = authenticationService;
    }

    @Transactional
    public ListingAvailableDatesResponse setAvailableDates(Long listingId, ListingAvailableDatesRequest listingAvailableDatesRequest){
        User authenticateUser = authenticationService.authenticateMethods();

        Listing listing = listingRepository.findById(listingId).orElseThrow(() -> new NoSuchElementException("Listing not found"));

       if(!authenticateUser.getId().equals(listing.getUser().getId())) {
            throw new IllegalArgumentException("You can only create available dates for your own listings!");
        }

        LocalDate oneYearFromNow = LocalDate.now().plusYears(1);
        ListingAvailableDates listingAvailableDates = convertListingAvailableDatesRequestToAvailableDatesEntity(listingAvailableDatesRequest);
        LocalDate endDate = listingAvailableDates.getEndDate();
    //felhantering av eventuella overlap
        Optional<ListingAvailableDates> existingDateRange = listingAvailableDatesRepository
                .findByListingIdAndStartDateAndEndDate(listingId, listingAvailableDates.getStartDate(), listingAvailableDates.getEndDate());


        if (existingDateRange.isPresent()) {
            throw new DataIntegrityViolationException("The available date range already exists.");
    }
        boolean isOverlapping = listingAvailableDatesRepository.existsByListingIdAndOverlappingDates(
                listingId, listingAvailableDates.getStartDate(), listingAvailableDates.getEndDate());

        if(isOverlapping){
            throw new DataIntegrityViolationException("The available date range overlaps with an existing range.");
        }
//felhantering av eventuella försök av hosten att lägga availability for mer än 1 år framåt
        if (endDate.isAfter(oneYearFromNow)) {
            throw new IllegalArgumentException("The availability cannot be more than one year ahead .");
        }

        listingAvailableDates.setListing(listing);


        ListingAvailableDates savedListingAvailableDates = listingAvailableDatesRepository.save(listingAvailableDates);
        return convertListingAvailableDatesEntityToListingAvailableDatesResponse(savedListingAvailableDates);
    }

    public ListingAvailableDatesResponse getSingleAvailableDatesByHost(Long listingAvailableDatesId){
        ListingAvailableDates listingAvailableDates = listingAvailableDatesRepository.findById(listingAvailableDatesId).orElseThrow(() -> new NoSuchElementException("No such available dates "));
        return convertListingAvailableDatesEntityToListingAvailableDatesResponse(listingAvailableDates);
    }

    public List<ListingAvailableDatesResponse> getAvailableDatesOfAListing(Long listingId){
        listingRepository.findById(listingId).orElseThrow(() -> new NoSuchElementException("Listing not found"));
        List <ListingAvailableDates> listingAvailableDatesList = listingAvailableDatesRepository.findAllByListingId(listingId);
        return convertListingAvailableDatesEntityToListingAvailableDatesResponse(listingAvailableDatesList);
    }

    public ListingAvailableDatesResponse updateSingleAvailableDates(Long listingAvailableDatesId, LocalDate newStartDate, LocalDate newEndDate){
        User authenticateUser = authenticationService.authenticateMethods();

        ListingAvailableDates listingAvailableDates = listingAvailableDatesRepository
                .findById(listingAvailableDatesId).orElseThrow(() -> new NoSuchElementException("No such available dates "));
        listingAvailableDates.setStartDate(newStartDate);
        listingAvailableDates.setEndDate(newEndDate);
        //felhantering av eventuella overlap, kallar på native JPA metoden findListing för att hitta listingId efter singleAvailableDateId
        Long listingId = listingAvailableDatesRepository.findListingIdByAvailableDatesId(listingAvailableDatesId);
        Optional<ListingAvailableDates> existingDateRange = listingAvailableDatesRepository
                .findByListingIdAndStartDateAndEndDate(listingId, listingAvailableDates.getStartDate(), listingAvailableDates.getEndDate());

        //kollar att inte available dates existerar, dvs behöver inte updateras om det är samma
        if (existingDateRange.isPresent()) {
            throw new DataIntegrityViolationException("The available date range already exists.");
        }
        boolean isOverlapping = listingAvailableDatesRepository.existsByListingIdAndOverlappingDates(
                listingId, listingAvailableDates.getStartDate(), listingAvailableDates.getEndDate());

//kolla att det inte finns överlapp och att inte det är samma available dates som vi vill ändra på
        ListingAvailableDates overlappingDates = listingAvailableDatesRepository.findOverlappingDateRange(listingId, newStartDate, newEndDate);
        if (isOverlapping && !overlappingDates.getId().equals(listingAvailableDatesId)) {
            throw new DataIntegrityViolationException("The available date range overlaps with an existing range.");
        }

//felhantering av eventuella försök av hosten att lägga availability for mer än 1 år framåt
        LocalDate oneYearFromNow = LocalDate.now().plusYears(1);
        if (newEndDate.isAfter(oneYearFromNow)) {
            throw new IllegalArgumentException("The availability cannot be more than one year ahead .");
        }
        Listing listing = listingRepository.findById(listingId).orElseThrow(() -> new NoSuchElementException("Listing not found"));
        listingAvailableDates.setListing(listing);
        ListingAvailableDates updateListingAvailableDates = listingAvailableDatesRepository.save(listingAvailableDates);
        return convertListingAvailableDatesEntityToListingAvailableDatesResponse(updateListingAvailableDates);
    }

    public void deleteSingleAvailableDatesByHost(Long availableDatesId){
        User authenticateUser = authenticationService.authenticateMethods();

        ListingAvailableDates listingAvailableDates = listingAvailableDatesRepository.findById(availableDatesId).orElseThrow(() -> new NoSuchElementException("No such available dates "));
        listingAvailableDatesRepository.delete(listingAvailableDates);
    }

    //behöver Transactional annotation för metoden ska funka, funkade inte utan den
    @Transactional
    public String deleteAllAvailableDatesByHostOfAListing(Long listingId) {
        User authenticateUser = authenticationService.authenticateMethods();

        listingRepository.findById(listingId)
                .orElseThrow(() -> new NoSuchElementException("Listing not found"));

        if (listingAvailableDatesRepository.existsByListingId(listingId)) {
            listingAvailableDatesRepository.deleteAllByListingId(listingId);
            return "All available dates for listing with ID " + listingId + " have been successfully removed.";
        } else {
            return "No available dates found for listing ID " + listingId;
        }
    }

    public boolean checkAvailability(Long listingId, LocalDate startDate, LocalDate endDate) {
        User authenticateUser = authenticationService.authenticateMethods();

        listingRepository.findById(listingId).orElseThrow(() -> new NoSuchElementException("Listing not found"));
        boolean isAvailable = listingAvailableDatesRepository.existsByListingIdAndOverlappingDates(
                listingId,startDate, endDate);
        return isAvailable;
    }


    @Transactional
    public void blockAvailableDatesAfterBooking(Long listingId, Booking newBooking) {
        User authenticateUser = authenticationService.authenticateMethods();

        Listing listing = listingRepository.findById(listingId).orElseThrow(() -> new NoSuchElementException("Listing not found"));
        List<ListingAvailableDates> availableDatesRangesOfListing = listingAvailableDatesRepository.findAllByListingId(listingId);

        if (availableDatesRangesOfListing.isEmpty()) {
            throw new NoSuchElementException("No available dates found for the given listing.");
        }

        LocalDate bookingStartDate = newBooking.getStartDate();
        LocalDate bookingEndDate = newBooking.getEndDate();

        for (ListingAvailableDates availableDates : availableDatesRangesOfListing) {
            if (bookingStartDate.isBefore(availableDates.getEndDate()) && bookingEndDate.isAfter(availableDates.getStartDate())) {

                //om bokningen har samma startdatum som availableDateRange
                if (bookingStartDate.isEqual(availableDates.getStartDate()) && bookingEndDate.isEqual(availableDates.getEndDate())) {
                    listingAvailableDatesRepository.delete(availableDates);
                }

                //om bokningen är i mitten på range, och separerar range, två resterande range sparas, det gamla range tas bort
                else if (bookingStartDate.isAfter(availableDates.getStartDate()) && bookingEndDate.isBefore(availableDates.getEndDate())) {
                    ListingAvailableDates rangeBeforeNewBooking = new ListingAvailableDates();
                    rangeBeforeNewBooking.setListing(listing);
                    rangeBeforeNewBooking.setStartDate(availableDates.getStartDate());
                    rangeBeforeNewBooking.setEndDate(bookingStartDate);


                    ListingAvailableDates rangeAfterBooking = new ListingAvailableDates();
                    rangeAfterBooking.setListing(listing);
                    rangeAfterBooking.setStartDate(bookingEndDate);
                    rangeAfterBooking.setEndDate(availableDates.getEndDate());

                    List<ListingAvailableDates> rangesToSave = new ArrayList<>();
                    rangesToSave.add(rangeBeforeNewBooking);
                    rangesToSave.add(rangeAfterBooking);

                    listingAvailableDatesRepository.delete(availableDates);
                    listingAvailableDatesRepository.saveAll(rangesToSave);


                }
                /////om bokningen är i början på range
                else if (bookingStartDate.isEqual(availableDates.getStartDate()) && bookingEndDate.isBefore(availableDates.getEndDate())) {
                    ListingAvailableDates rangeRemainingAfter = new ListingAvailableDates();
                    rangeRemainingAfter.setListing(listing);
                    rangeRemainingAfter.setStartDate(bookingEndDate);
                    rangeRemainingAfter.setEndDate(availableDates.getEndDate());
                    listingAvailableDatesRepository.save(rangeRemainingAfter);
                    listingAvailableDatesRepository.delete(availableDates);
                }
                //om bokningen är på slutet på range
                else if (bookingStartDate.isAfter(availableDates.getStartDate()) && bookingEndDate.isEqual(availableDates.getEndDate())) {
                    ListingAvailableDates rangeRemainingBefore = new ListingAvailableDates();
                    rangeRemainingBefore.setListing(listing);
                    rangeRemainingBefore.setStartDate(availableDates.getStartDate());
                    rangeRemainingBefore.setEndDate(bookingStartDate);
                    listingAvailableDatesRepository.save(rangeRemainingBefore);
                    listingAvailableDatesRepository.delete(availableDates);

                }
            }
        }
    }
    @Transactional
    public void restoreAvailableDateRange(Long listingId, LocalDate bookingStartDate, LocalDate bookingEndDate) {
        //hitta listing
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new NoSuchElementException("Listing ej hittad"));

        //kontrollera om range redan är tillgängligt
        List<ListingAvailableDates> availableDatesRanges = listingAvailableDatesRepository.findAllByListingId(listingId);
        boolean isRangeAlreadyAvailable = availableDatesRanges.stream().anyMatch(range ->
                (bookingStartDate.isBefore(range.getEndDate()) && bookingEndDate.isAfter(range.getStartDate())));

        //om range redan är tillgängligt gör inga ändringar
        if (isRangeAlreadyAvailable) {
            return;
        }

        //skapa det återställda range om ingen överlappning
        ListingAvailableDates restoredRange = new ListingAvailableDates();
        restoredRange.setListing(listing);
        restoredRange.setStartDate(bookingStartDate);
        restoredRange.setEndDate(bookingEndDate);

        //spara det återställda tillgängliga datumintervallet
        listingAvailableDatesRepository.save(restoredRange);
    }



    @Transactional
    public void mergeListingAvailableDates(Long listingId) {
        //hämta alla tillgängliga available date ranges för det listingen
        List<ListingAvailableDates> allAvailableDatesRangesOfAListing = listingAvailableDatesRepository.findAllByListingId(listingId);

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new NoSuchElementException("Listing not found"));

        //om inga datumintervall hittas gör inget
        if (allAvailableDatesRangesOfAListing == null || allAvailableDatesRangesOfAListing.isEmpty()) {
            return;
        }

        //skaffar en tomm lista för att lagra de mergeade intervallen
        List<ListingAvailableDates> mergedListingAvailableDatesRanges = new ArrayList<>();

        //sorterar datumintervallen efter startdatum, VIKTIGT annars merge kommer ske inkorekt
        allAvailableDatesRangesOfAListing.sort((range1, range2) -> range1.getStartDate().compareTo(range2.getStartDate()));

        // loopa genom alla ranges och slå ihop de
        for (ListingAvailableDates currentRange : allAvailableDatesRangesOfAListing) {
            // Om mergedListingAvailableDatesRanges är tom lägg till det första intervallet direkt
            if (mergedListingAvailableDatesRanges.isEmpty()) {
                mergedListingAvailableDatesRanges.add(currentRange);
            } else {
                ListingAvailableDates lastRange = mergedListingAvailableDatesRanges.get(mergedListingAvailableDatesRanges.size() - 1);

                // Kontrollera om det nuvarande range överlappar eller är anslutet till det senaste range
                if (!currentRange.getStartDate().isAfter(lastRange.getEndDate()) || currentRange.getStartDate().equals(lastRange.getEndDate())) {
                    // Om de överlappar eller är anslutna (exakt slutdatum = startdatum)
                    if (currentRange.getEndDate().isAfter(lastRange.getEndDate())) {
                        lastRange.setEndDate(currentRange.getEndDate());
                    }
                } else {
                    // Om de inte överlappar lägg till det aktuella range som ett nytt intervall
                    mergedListingAvailableDatesRanges.add(currentRange);
                }
            }
        }

        //tar bort gamla range från databasen för listingId
        listingAvailableDatesRepository.deleteAllByListingId(listingId);

        //skapar nya objekt för sammanslagna range och spara dem i databasen
        for (ListingAvailableDates mergedRange : mergedListingAvailableDatesRanges) {
            ListingAvailableDates newListingAvailableDates = new ListingAvailableDates();
            newListingAvailableDates.setListing(listing);
            newListingAvailableDates.setStartDate(mergedRange.getStartDate());
            newListingAvailableDates.setEndDate(mergedRange.getEndDate());

            listingAvailableDatesRepository.save(newListingAvailableDates);
        }
    }



    //metod overloading
    public List<ListingAvailableDatesResponse> convertListingAvailableDatesEntityToListingAvailableDatesResponse(List<ListingAvailableDates> listingAvailableDatesList){

        List<ListingAvailableDatesResponse> listingAvailableDatesResponseList = new ArrayList<>();
        for(ListingAvailableDates listingAvailableDates : listingAvailableDatesList) {
            listingAvailableDatesResponseList.add(convertListingAvailableDatesEntityToListingAvailableDatesResponse(listingAvailableDates));
        }
        return listingAvailableDatesResponseList;
    }

    public ListingAvailableDatesResponse convertListingAvailableDatesEntityToListingAvailableDatesResponse(ListingAvailableDates listingAvailableDates) {
        ListingAvailableDatesResponse listingAvailableDatesResponse = new ListingAvailableDatesResponse();
        listingAvailableDatesResponse.setListingId(listingAvailableDates.getListing().getId());
        listingAvailableDatesResponse.setStartDate(listingAvailableDates.getStartDate());
        listingAvailableDatesResponse.setEndDate(listingAvailableDates.getEndDate());
        return listingAvailableDatesResponse;
    }

    public ListingAvailableDates convertListingAvailableDatesRequestToAvailableDatesEntity(ListingAvailableDatesRequest listingAvailableDatesRequest){
        ListingAvailableDates listingAvailableDates = new ListingAvailableDates();
        listingAvailableDates.setStartDate(listingAvailableDatesRequest.getStartDate());
        listingAvailableDates.setEndDate(listingAvailableDatesRequest.getEndDate());
        return listingAvailableDates;
    }

}
