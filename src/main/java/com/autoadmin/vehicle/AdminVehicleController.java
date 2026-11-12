package com.autoadmin.vehicle;

import com.autoadmin.vehicle.photo.VehiclePhotoMetadata;
import com.autoadmin.vehicle.photo.VehiclePhotoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/vehicles")
public class AdminVehicleController {
    private final AdminVehicleService vehicles;
    private final VehiclePhotoService photos;

    public AdminVehicleController(AdminVehicleService vehicles, VehiclePhotoService photos) {
        this.vehicles = vehicles;
        this.photos = photos;
    }

    @GetMapping
    public List<AdminVehicleListing> list() {
        return vehicles.listAll();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AdminVehicleListing create(@Valid @ModelAttribute VehicleListingForm form,
                                      @RequestParam(name = "photos", required = false) List<MultipartFile> uploads) {
        return AdminVehicleListing.from(vehicles.create(form, uploads));
    }

    @PostMapping(path = "/{vehicleId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AdminVehicleListing update(@PathVariable Long vehicleId, @Valid @ModelAttribute VehicleListingForm form,
                                      @RequestParam(name = "photos", required = false) List<MultipartFile> uploads) {
        return AdminVehicleListing.from(vehicles.update(vehicleId, form, uploads));
    }

    @DeleteMapping("/{vehicleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long vehicleId) {
        vehicles.delete(vehicleId);
    }

    @GetMapping("/{vehicleId}/photos")
    public List<VehiclePhotoMetadata> listPhotos(@PathVariable Long vehicleId) {
        return photos.listPhotos(vehicleId);
    }

    @PostMapping(path = "/{vehicleId}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public List<VehiclePhotoMetadata> addPhotos(@PathVariable Long vehicleId,
                                                 @RequestParam(name = "photos") List<MultipartFile> uploads) {
        vehicles.addPhotos(vehicleId, uploads);
        return photos.listPhotos(vehicleId);
    }

    @DeleteMapping("/{vehicleId}/photos/{photoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePhoto(@PathVariable Long vehicleId, @PathVariable Long photoId) {
        vehicles.deletePhoto(vehicleId, photoId);
    }
}
