package com.autoadmin.vehicle.photo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "vehicle_photos")
public class VehiclePhoto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "content_type", nullable = false, length = 32)
    private String contentType;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Lob
    @Column(name = "photo_data", nullable = false, columnDefinition = "MEDIUMBLOB")
    private byte[] photoData;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    protected VehiclePhoto() { }

    public VehiclePhoto(Long vehicleId, String fileName, String contentType, Integer sortOrder, byte[] photoData) {
        this.vehicleId = vehicleId;
        this.fileName = fileName;
        this.contentType = contentType;
        this.sortOrder = sortOrder;
        this.photoData = photoData;
    }

    public Long getId() { return id; }
    public Long getVehicleId() { return vehicleId; }
    public String getFileName() { return fileName; }
    public String getContentType() { return contentType; }
    public Integer getSortOrder() { return sortOrder; }
    public byte[] getPhotoData() { return photoData; }
    public Instant getCreatedAt() { return createdAt; }
}
