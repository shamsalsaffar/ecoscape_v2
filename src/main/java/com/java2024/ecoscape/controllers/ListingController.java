package com.java2024.ecoscape.controllers;

import com.java2024.ecoscape.dto.ListingRequest;
import com.java2024.ecoscape.dto.ListingResponse;
import com.java2024.ecoscape.models.Category;
import com.java2024.ecoscape.repositories.ListingImagesRepository;
import com.java2024.ecoscape.repositories.ListingRepository;
import com.java2024.ecoscape.services.ListingService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    private final ListingService listingService;
    private final ListingRepository listingRepository;
    private final ListingImagesRepository listingImagesRepository;


    public ListingController(ListingService listingService, ListingRepository listingRepository, ListingImagesRepository listingImagesRepository) {
        this.listingService = listingService;
        this.listingRepository = listingRepository;
        this.listingImagesRepository = listingImagesRepository;
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('HOST', 'ADMIN')")
    public ResponseEntity<ListingResponse> createListing(@Valid @RequestBody ListingRequest listingRequest) {
        ListingResponse listingResponse = listingService.createListing(listingRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(listingResponse);
    }

    @GetMapping
    public ResponseEntity <List<ListingResponse>> getAllListing(){
        List<ListingResponse> listingResponseList = listingService.getAllExistingListings();
        return ResponseEntity.ok(listingResponseList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ListingResponse> getListingById(@PathVariable Long id) {
        ListingResponse listingResponse = listingService.getListingById(id);
        return ResponseEntity.ok(listingResponse);
    }

    @PatchMapping("/{listingId}")
    public ResponseEntity<ListingResponse> partialUpdateListing(@PathVariable Long listingId, @RequestBody ListingRequest listingRequest) {
        ListingResponse listingResponse = listingService.partialUpdateListingById(listingId, listingRequest);
        return ResponseEntity.ok(listingResponse);
    }

    @DeleteMapping("/{listingId}")
    public ResponseEntity<String> deleteListingById(@PathVariable Long listingId) {
        listingService.deleteListingById(listingId);
        return ResponseEntity.ok("Listing successfully deleted!");
    }

    @GetMapping("/search")
    public ResponseEntity <List<ListingResponse>> searchListings (@RequestParam(name = "checkInDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkInDate,
                                                                  @RequestParam(name = "checkOutDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkOutDate,
                                                                  @RequestParam(name = "name", required = false) String name,
                                                                  @RequestParam(name = "location", required = false) String location,
                                                                  @RequestParam(name = "capacity", required = false) Integer capacity,
                                                                  @RequestParam(name = "category", required = false) Category category) {
        return ResponseEntity.ok(listingService.searchAvailableListings(checkInDate, checkOutDate, name, location, capacity, category));

    }


}
