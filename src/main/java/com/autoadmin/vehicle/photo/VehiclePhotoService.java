package com.autoadmin.vehicle.photo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VehiclePhotoService {
    public static final int MAX_PHOTOS = 5;
    public static final long MAX_FILE_BYTES = 2L * 1024 * 1024;

    private final VehiclePhotoRepository photos;

    public VehiclePhotoService(VehiclePhotoRepository photos) {
        this.photos = photos;
    }

    @Transactional
    public List<VehiclePhotoMetadata> savePhotos(Long vehicleId, List<? extends MultipartFile> uploads) {
        if (uploads == null || uploads.isEmpty()) return List.of();
        if (photos.countByVehicleId(vehicleId) + uploads.size() > MAX_PHOTOS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose no more than five car photos.");
        }

        List<VehiclePhoto> records = new ArrayList<>();
        for (int index = 0; index < uploads.size(); index++) {
            MultipartFile upload = uploads.get(index);
            if (upload == null || upload.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each selected photo must contain image data.");
            }
            if (upload.getSize() > MAX_FILE_BYTES) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each car photo must be 2 MB or smaller.");
            }
            byte[] bytes;
            try {
                bytes = upload.getBytes();
            } catch (IOException exception) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The selected photo could not be read.");
            }
            String contentType = detectImageType(bytes);
            if (contentType == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use JPEG, PNG, or WebP image files only.");
            }
            records.add(new VehiclePhoto(vehicleId, safeFileName(upload.getOriginalFilename()), contentType, index, bytes));
        }

        List<VehiclePhoto> saved = photos.saveAll(records);
        return saved.stream()
            .map(photo -> new VehiclePhotoMetadata(photo.getId(), photo.getFileName(), photo.getContentType(), photo.getSortOrder()))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<VehiclePhotoMetadata> listPhotos(Long vehicleId) {
        return photos.findMetadataByVehicleId(vehicleId);
    }

    @Transactional(readOnly = true)
    public VehiclePhoto getPhoto(Long vehicleId, Long photoId) {
        return photos.findByIdAndVehicleId(photoId, vehicleId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car photo not found."));
    }

    @Transactional
    public void deletePhoto(Long vehicleId, Long photoId) {
        VehiclePhoto photo = photos.findByIdAndVehicleId(photoId, vehicleId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car photo not found."));
        photos.delete(photo);
    }

    private static String detectImageType(byte[] bytes) {
        if (bytes.length >= 3 && (bytes[0] & 0xff) == 0xff && (bytes[1] & 0xff) == 0xd8 && (bytes[2] & 0xff) == 0xff) {
            return "image/jpeg";
        }
        if (bytes.length >= 8 && (bytes[0] & 0xff) == 0x89
            && bytes[1] == 0x50 && bytes[2] == 0x4e && bytes[3] == 0x47
            && bytes[4] == 0x0d && bytes[5] == 0x0a && bytes[6] == 0x1a && bytes[7] == 0x0a) {
            return "image/png";
        }
        if (bytes.length >= 12
            && new String(bytes, 0, 4, StandardCharsets.US_ASCII).equals("RIFF")
            && new String(bytes, 8, 4, StandardCharsets.US_ASCII).equals("WEBP")) {
            return "image/webp";
        }
        return null;
    }

    private static String safeFileName(String originalName) {
        if (originalName == null || originalName.isBlank()) return "car-photo";
        String normalized = originalName.replace('\\', '/');
        String name = normalized.substring(normalized.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}]", "").trim();
        if (name.isBlank()) return "car-photo";
        return name.substring(0, Math.min(name.length(), 255));
    }
}
