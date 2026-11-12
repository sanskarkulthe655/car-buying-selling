ALTER TABLE sales
    ADD COLUMN sale_channel VARCHAR(24) NOT NULL DEFAULT 'OFFLINE';

CREATE TABLE demo_payment_attempts (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    sale_id BIGINT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    payment_method VARCHAR(16) NOT NULL,
    outcome VARCHAR(16) NOT NULL,
    demo_reference VARCHAR(64) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_demo_payment_attempts_created (created_at, id),
    INDEX idx_demo_payment_attempts_vehicle (vehicle_id, id),
    CONSTRAINT fk_demo_payment_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id),
    CONSTRAINT fk_demo_payment_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_demo_payment_sale FOREIGN KEY (sale_id) REFERENCES sales(id),
    CONSTRAINT chk_demo_payment_method CHECK (payment_method IN ('CARD', 'UPI')),
    CONSTRAINT chk_demo_payment_outcome CHECK (outcome IN ('SUCCESS', 'FAILED'))
);
