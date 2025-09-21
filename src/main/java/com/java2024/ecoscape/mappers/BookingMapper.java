package com.java2024.ecoscape.mappers;

import com.java2024.ecoscape.dto.BookingRequest;
import com.java2024.ecoscape.dto.BookingResponse;
import com.java2024.ecoscape.models.Booking;
import com.java2024.ecoscape.models.Listing;
import com.java2024.ecoscape.models.User;
import com.java2024.ecoscape.services.PriceService;
import org.mapstruct.*;
import org.mapstruct.BeanMapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring")
public interface BookingMapper {

    /* ====== Entity -> Response ====== */
    // == specificerar fältmappningar från Booking (entity) till BookingResponse (DTO) ==//
    @Mappings({
            @Mapping(target = "bookingId", source = "id"), // Sätter bookingId i svaret från entityns id
            @Mapping(target = "userId", source = "user.id"),
            @Mapping(target = "listingId", source = "listing.id"),
            @Mapping(target = "listingname", source = "listing.name"),
            @Mapping(target = "pricePerNight", source = "listing.pricePerNight"),
            @Mapping(target = "cleaningFee", source = "listing.cleaningFee"),
            @Mapping(target = "websiteFee", ignore = true),
            @Mapping(target = "message", ignore = true),
            @Mapping(target = "totalPrice", source = "totalPrice")
    })

    // Metod som mappar Booking -> BookingResponse, med PriceService som @Context (tillgänglig i AfterMapping)
    BookingResponse toResponse(Booking booking, @Context PriceService priceService);

    // Aftermapping: börja räkna websiteFee genom priceService för att få samma resultat
    @AfterMapping
    default void fillWebsiteFee(Booking booking,
                                @Context PriceService priceService,
                                @MappingTarget BookingResponse resp) {
        var pb = priceService.calculate(
                booking.getListing(),
                booking.getStartDate(),
                booking.getEndDate()
        );
        resp.setWebsiteFee(pb.getServiceFeeAmount());
    }

    /* ====== Request -> Entity ====== */
    /* == // specificerar hur ett inkommande BookingRequest ska mappas till en ny Booking-entity ==*/
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "user", expression = "java(user)"),
            @Mapping(target = "listing", expression = "java(listing)"),
            @Mapping(target = "status", ignore = true),
            @Mapping(target = "totalPrice", ignore = true) // få detta eftermapping
    })

    // Metod som mappar BookingRequest -> Booking, med Listing, User och PriceService som @Context
    Booking toEntity(BookingRequest req,
                     @Context Listing listing,
                     @Context User user,
                     @Context PriceService priceService);

    /* Här räkna totalPrice Aftermapping genom priceService */
    @AfterMapping
    default void fillTotalPrice(BookingRequest req,
                                @Context Listing listing,
                                @Context User user,
                                // use @context som hjälpparameter för beräkningar/uppslag – inte en del av DTO/Entity.
                                @Context PriceService priceService,
                                @MappingTarget Booking entity) {
        var pb = priceService.calculate(
                listing,
                req.getStartDate(),
                req.getEndDate()
        );
        entity.setTotalPrice(pb.getTotal());
    }


    /* ====== Entity -> Request(get all ) ====== */
    @Mappings({
            @Mapping(target = "userId", source = "user.id"),
            @Mapping(target = "listingId", source = "listing.id"),

    })

    BookingRequest toRequest ( Booking booking);



    /*===== Update existing entity from request ( contact + dates + guests) =====*/
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "user", ignore = true),
            @Mapping(target = "listing", ignore = true),
            @Mapping(target = "status", ignore = true),
            @Mapping(target = "totalPrice", ignore = true)
    })
    void updateEntityFromRequest(BookingRequest req , @MappingTarget Booking entity);




}
