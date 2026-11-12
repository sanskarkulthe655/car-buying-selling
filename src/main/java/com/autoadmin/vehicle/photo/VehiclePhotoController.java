package com.autoadmin.vehicle.photo;

import java.util.List;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehicles/{vehicleId}/photos")
public class VehiclePhotoController {
    private final VehiclePhotoService photos;

    public VehiclePhotoController(VehiclePhotoService photos) {
        this.photos = photos;
    }

    @GetMapping
    public List<VehiclePhotoMetadata> list(@PathVariable Long vehicleId) {
        return photos.listPhotos(vehicleId);
    }

    @GetMapping("/{photoId}")
    public ResponseEntity<byte[]> image(@PathVariable Long vehicleId, @PathVariable Long photoId) {
        VehiclePhoto photo = photos.getPhoto(vehicleId, photoId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(photo.getContentType()));
        headers.setContentLength(photo.getPhotoData().length);
        headers.setCacheControl(CacheControl.noCache());
        headers.set("X-Content-Type-Options", "nosniff");
        return ResponseEntity.ok().headers(headers).body(photo.getPhotoData());
    }
}
