package com.autoadmin.vehicle;

import java.math.BigDecimal;
import java.time.Instant;

public record VehiclePublicListing(
    Long id,
    String make,
    String model,
    Integer year,
    String bodyType,
    BigDecimal price,
    Integer mileage,
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
    public static VehiclePublicListing from(Vehicle vehicle) {
        return new VehiclePublicListing(
            vehicle.getId(), vehicle.getMake(), vehicle.getModel(), vehicle.getYear(),
            vehicle.getBodyType(), vehicle.getPrice(), vehicle.getMileage(), vehicle.getColor(),
            vehicle.getSellerName(), vehicle.getConditionType(), vehicle.getVariant(),
            vehicle.getFuelType(), vehicle.getTransmission(), vehicle.getEngineSpec(),
            vehicle.getPowerOutput(), vehicle.getTorqueOutput(), vehicle.getEfficiencyRange(),
            vehicle.getDrivetrain(), vehicle.getSeatCount(), vehicle.getWarranty(),
            vehicle.getCreatedAt()
        );
    }
}
