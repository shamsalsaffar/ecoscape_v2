package com.java2024.ecoscape.controllers;

import com.java2024.ecoscape.dto.ListingImagesGetResponse;
import com.java2024.ecoscape.dto.ListingImagesRequest;
import com.java2024.ecoscape.dto.ListingImagesUploadResponse;
import com.java2024.ecoscape.models.ListingImages;
import com.java2024.ecoscape.repositories.ListingImagesRepository;
import com.java2024.ecoscape.services.ListingImagesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/images")
public class ListingImagesController {
    private final ListingImagesService listingImagesService;
    private final ListingImagesRepository listingImagesRepository;

    public ListingImagesController(ListingImagesService listingImagesService, ListingImagesRepository listingImagesRepository) {
        this.listingImagesService = listingImagesService;
        this.listingImagesRepository = listingImagesRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('HOST', 'ADMIN')")
    public ResponseEntity<?> createListingImage(@Valid @RequestBody ListingImagesRequest request) {
        if (listingImagesService.existsByImageUrl(request.getImageUrl())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("That image is already uploaded.");
        }

        long listingImagesCounter = listingImagesRepository.countByListing(request.getListing());

        if (listingImagesCounter >= 5) {
            throw new IllegalArgumentException("Listings can only have 5 images");
        }

        ListingImages listingImages = listingImagesService.createListingImages(request);

        ListingImagesUploadResponse listingImagesUploadResponse = new ListingImagesUploadResponse(
                listingImages.getImageUrl(),
                "Image got uploaded successfully!"
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(listingImagesUploadResponse);
    }

    @GetMapping
    public ResponseEntity<List<ListingImagesGetResponse>> getAllListingImages() {
        List<ListingImagesGetResponse> images = listingImagesService.findAllListingImages();
        return ResponseEntity.ok(images);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ListingImagesGetResponse> getImageById(@PathVariable Long id) {
        ListingImages listingImages = listingImagesService.findImageById(id);

        ListingImagesGetResponse listingImagesGetResponse = new ListingImagesGetResponse(
                listingImages.getListing().getId(),
                listingImages.getImageUrl()
        );

        return ResponseEntity.ok(listingImagesGetResponse);
    }

    @GetMapping("/all/{listingId}")
    public ResponseEntity<List<ListingImagesGetResponse>> getListingImageById(@PathVariable Long listingId) {
        List<ListingImagesGetResponse> listingImages = listingImagesService.getAllListingImagesByListingId(listingId);
        return ResponseEntity.ok(listingImages);
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('HOST', 'ADMIN')")
    public ResponseEntity<?> updateListingImage(@PathVariable Long id, @Valid @RequestBody ListingImagesRequest listingImagesRequest) {
        if (!listingImagesRepository.existsById(id)) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("That image doesn't exist.");
        }

        if (listingImagesService.existsByImageUrl(listingImagesRequest.getImageUrl())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("That image is already uploaded.");
        }

        ListingImages updatedListingImage = listingImagesService.updateListingImage(id, listingImagesRequest);

        ListingImagesUploadResponse listingImagesUploadResponse = new ListingImagesUploadResponse(
                updatedListingImage.getImageUrl(),
                "Image got updated successfully!"
        );

        return ResponseEntity.ok(listingImagesUploadResponse);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('HOST', 'ADMIN')")
    public ResponseEntity<String> deleteListingImage(@PathVariable Long id) {
        listingImagesService.deleteListingImage(id);

        return ResponseEntity.ok("Listing image successfully deleted!");
    }

    @DeleteMapping("/listing/{listingId}")
    @PreAuthorize("hasAnyRole('HOST', 'ADMIN')")
    public ResponseEntity<String> deleteAllListingImagesById(@PathVariable Long listingId) {
        listingImagesService.deleteAllListingImages(listingId);

        return ResponseEntity.ok("All listing images belonging to " + listingId + " have been successfully deleted!");
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<String> findImageUrlsByBookingId(@PathVariable Long bookingId) {
        String listingImageUrl = listingImagesService.findImageUrlsByBookingId(bookingId);
        return ResponseEntity.ok(listingImageUrl);
    }
}
