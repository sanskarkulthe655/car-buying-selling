package com.autoadmin.vehicle;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/listings")
public class VehicleListingController {
    private final VehicleListingService listings;

    public VehicleListingController(VehicleListingService listings) {
        this.listings = listings;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public VehiclePublicListing createListing(
        @Valid @ModelAttribute VehicleListingForm form,
        @RequestParam(name = "photos", required = false) List<MultipartFile> photos
    ) {
        return VehiclePublicListing.from(listings.createListing(form.toVehicle(), photos));
    }
}
