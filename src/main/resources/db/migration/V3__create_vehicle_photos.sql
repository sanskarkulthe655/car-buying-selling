CREATE TABLE vehicle_photos (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(32) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    photo_data MEDIUMBLOB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_vehicle_photos_vehicle_order (vehicle_id, sort_order, id),
    CONSTRAINT fk_vehicle_photos_vehicle
        FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE
);
