ALTER TABLE vehicles
    MODIFY COLUMN vin VARCHAR(32) NULL,
    ADD COLUMN condition_type VARCHAR(12) NOT NULL DEFAULT 'USED',
    ADD COLUMN variant_name VARCHAR(120) NULL,
    ADD COLUMN fuel_type VARCHAR(32) NULL,
    ADD COLUMN transmission VARCHAR(40) NULL,
    ADD COLUMN engine_spec VARCHAR(120) NULL,
    ADD COLUMN power_output VARCHAR(60) NULL,
    ADD COLUMN torque_output VARCHAR(60) NULL,
    ADD COLUMN efficiency_range VARCHAR(80) NULL,
    ADD COLUMN drivetrain VARCHAR(40) NULL,
    ADD COLUMN seat_count INT NULL,
    ADD COLUMN warranty VARCHAR(120) NULL;
