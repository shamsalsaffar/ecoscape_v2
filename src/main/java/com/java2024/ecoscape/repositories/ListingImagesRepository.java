package com.java2024.ecoscape.repositories;

import com.java2024.ecoscape.models.Listing;
import com.java2024.ecoscape.models.ListingImages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ListingImagesRepository extends JpaRepository<ListingImages, Long> {
    Optional<ListingImages> findByImageUrl(String imageUrl);

    List<ListingImages> findByListingId(Long listingId);

    long countByListing(Listing listing);

    @Query(value = "SELECT li.image_url " +
            "FROM listing_images li " +
            "JOIN listings l ON li.listing_id = l.id " +
            "JOIN bookings b ON b.listing_id = l.id " +
            "WHERE b.id = :bookingId " +
            "AND li.id = (SELECT MIN(id) FROM listing_images WHERE listing_id = l.id)",
            nativeQuery = true)
    String findFirstImageUrlsByBookingId(@Param("bookingId") Long bookingId);

}
