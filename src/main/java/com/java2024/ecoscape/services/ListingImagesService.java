package com.java2024.ecoscape.services;

import com.java2024.ecoscape.dto.ListingImagesGetResponse;
import com.java2024.ecoscape.dto.ListingImagesRequest;
import com.java2024.ecoscape.models.Listing;
import com.java2024.ecoscape.models.ListingImages;
import com.java2024.ecoscape.models.User;
import com.java2024.ecoscape.repositories.ListingImagesRepository;
import com.java2024.ecoscape.repositories.ListingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class ListingImagesService {
    private final ListingRepository listingRepository;
    private final ListingImagesRepository listingImagesRepository;
    private final AuthenticationService authenticationService;

    public ListingImagesService(ListingRepository listingRepository, ListingImagesRepository listingImagesRepository, AuthenticationService authenticationService) {
        this.listingRepository = listingRepository;
        this.listingImagesRepository = listingImagesRepository;
        this.authenticationService = authenticationService;
    }


    public boolean existsByImageUrl(String imageUrl) {
        return listingImagesRepository.findByImageUrl(imageUrl).isPresent();
    }

    public boolean existsById(Long id) {
        return listingImagesRepository.findById(id).isPresent();
    }

    public ListingImages createListingImages(ListingImagesRequest listingImagesRequest) {
        User authenticateUser = authenticationService.authenticateMethods();

        Listing listing = listingRepository.findById(listingImagesRequest.getListing().getId())
                .orElseThrow(() -> new NoSuchElementException("Listing was not found."));

        if(!authenticateUser.getId().equals(listing.getUser().getId())) {
            throw new IllegalArgumentException("You can only add images to your own listings.");
        }

        ListingImages listingImages = new ListingImages();
        listingImages.setListing(listing);
        listingImages.setImageUrl(listingImagesRequest.getImageUrl());

        return listingImagesRepository.save(listingImages);
    }

    public ListingImages findImageById(Long id) {
        return listingImagesRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Listing image not found"));
    }

    public List<ListingImagesGetResponse> findAllListingImages() {
        User authenticateUser = authenticationService.authenticateMethods();

        List<ListingImages> listingImages = listingImagesRepository.findAll();

        return listingImages.stream()
                .map(images -> new ListingImagesGetResponse(
                        images.getListing().getId(),
                        images.getImageUrl()
                ))
                .collect(Collectors.toList());
    }

    public List<ListingImagesGetResponse> getAllListingImagesByListingId(Long listingId) {
        listingImagesRepository.findById(listingId)
                .orElseThrow(() -> new NoSuchElementException("Listing not found"));

        List<ListingImages> listingImages = listingImagesRepository.findByListingId(listingId);

        return listingImages.stream()
                .map(images -> new ListingImagesGetResponse(
                        images.getListing().getId(),
                        images.getImageUrl()
                ))
                .collect(Collectors.toList());
    }

    public ListingImages updateListingImage(Long id, ListingImagesRequest listingImagesRequest) {
        User authenticateUser = authenticationService.authenticateMethods();

        ListingImages existingListingImage = listingImagesRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Listing image not found"));

        Listing listing = existingListingImage.getListing();

        if (!authenticateUser.getId().equals(listing.getUser().getId())) {
            throw new IllegalArgumentException("You can only update images for your own listings.");
        }

        existingListingImage.setImageUrl(listingImagesRequest.getImageUrl());

        return listingImagesRepository.save(existingListingImage);
    }

    public void deleteListingImage(Long id) {
        User authenticateUser = authenticationService.authenticateMethods();

        ListingImages existingListingImage = listingImagesRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Listing image not found"));

        Listing listing = existingListingImage.getListing();

        if (!authenticateUser.getId().equals(listing.getUser().getId())) {
            throw new IllegalArgumentException("You can only update images for your own listings.");
        }

        listingImagesRepository.deleteById(id);
    }

    public void deleteAllListingImages(Long listingId) {
        User authenticateUser = authenticationService.authenticateMethods();

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new NoSuchElementException("Listing not found"));

        if (!authenticateUser.getId().equals(listing.getUser().getId())) {
            throw new IllegalArgumentException("You can only delete images from your own listings.");
        }

        List<ListingImages> existingImages = listingImagesRepository.findByListingId(listingId);

        listingImagesRepository.deleteAll(existingImages);
    }

    public List <String> findImageUrlsByBookingId(Long bookingId) {
        List <String> listingImages = listingImagesRepository.findImageUrlsByBookingId(bookingId);
        return listingImages;
    }


}
