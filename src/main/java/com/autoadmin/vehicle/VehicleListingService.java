package com.autoadmin.vehicle;

import com.autoadmin.vehicle.photo.VehiclePhotoService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VehicleListingService {
    private final VehicleRepository vehicles;
    private final VehiclePhotoService photos;

    public VehicleListingService(VehicleRepository vehicles, VehiclePhotoService photos) {
        this.vehicles = vehicles;
        this.photos = photos;
    }

    @Transactional
    public Vehicle createListing(Vehicle vehicle, List<MultipartFile> photoFiles) {
        vehicle.setStatus("AVAILABLE");
        Vehicle saved = vehicles.save(vehicle);
        photos.savePhotos(saved.getId(), photoFiles);
        return saved;
    }
}
