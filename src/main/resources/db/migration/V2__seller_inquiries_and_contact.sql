ALTER TABLE vehicles
    ADD COLUMN seller_name VARCHAR(120) NOT NULL DEFAULT 'Northline Motors' AFTER color;

CREATE TABLE buyer_inquiries (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    buyer_name VARCHAR(160) NOT NULL,
    buyer_email VARCHAR(180) NOT NULL,
    vehicle_id BIGINT NULL,
    message VARCHAR(1500) NOT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'NEW',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inquiries_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id)
);

CREATE TABLE contact_messages (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    sender_name VARCHAR(160) NOT NULL,
    sender_email VARCHAR(180) NOT NULL,
    phone VARCHAR(40),
    subject VARCHAR(160) NOT NULL,
    message VARCHAR(2000) NOT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'NEW',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
