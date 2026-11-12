package com.autoadmin.vehicle;

import java.math.BigDecimal;
import java.time.Instant;

public record AdminVehicleListing(
    Long id,
    String make,
    String model,
    Integer year,
    String vin,
    String bodyType,
    BigDecimal price,
    Integer mileage,
    String status,
    String color,
    String sellerName,
    String conditionType,
    String variant,
    String fuelType,
    String transmission,
    String engineSpec,
    String powerOutput,
    String torqueOutput,
    String efficiencyRange,
    String drivetrain,
    Integer seatCount,
    String warranty,
    Instant createdAt
) {
    public static AdminVehicleListing from(Vehicle vehicle) {
        return new AdminVehicleListing(vehicle.getId(), vehicle.getMake(), vehicle.getModel(), vehicle.getYear(),
            vehicle.getVin(), vehicle.getBodyType(), vehicle.getPrice(), vehicle.getMileage(), vehicle.getStatus(),
            vehicle.getColor(), vehicle.getSellerName(), vehicle.getConditionType(), vehicle.getVariant(),
            vehicle.getFuelType(), vehicle.getTransmission(), vehicle.getEngineSpec(), vehicle.getPowerOutput(),
            vehicle.getTorqueOutput(), vehicle.getEfficiencyRange(), vehicle.getDrivetrain(), vehicle.getSeatCount(),
            vehicle.getWarranty(), vehicle.getCreatedAt());
    }
}
