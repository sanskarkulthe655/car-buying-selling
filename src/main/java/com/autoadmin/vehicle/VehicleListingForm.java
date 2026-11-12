package com.autoadmin.vehicle;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Locale;

public class VehicleListingForm {
    @NotBlank @Size(max = 80)
    private String make;

    @NotBlank @Size(max = 100)
    private String model;

    @NotNull @Min(1900) @Max(2035)
    private Integer year;

    @Size(max = 32)
    private String vin;

    @NotBlank @Size(max = 40)
    private String bodyType;

    @NotNull @DecimalMin("0.0")
    private BigDecimal price;

    @NotNull @Min(0)
    private Integer mileage = 0;

    @NotBlank @Size(max = 40)
    private String color;

    @NotBlank @Size(max = 120)
    private String sellerName;

    @NotBlank @Pattern(regexp = "NEW|USED", message = "Choose New or Pre-Owned.")
    private String conditionType = "USED";

    @Size(max = 120)
    private String variant;

    @Size(max = 32)
    private String fuelType;

    @Size(max = 40)
    private String transmission;

    @Size(max = 120)
    private String engineSpec;

    @Size(max = 60)
    private String powerOutput;

    @Size(max = 60)
    private String torqueOutput;

    @Size(max = 80)
    private String efficiencyRange;

    @Size(max = 40)
    private String drivetrain;

    @Min(1) @Max(20)
    private Integer seatCount;

    @Size(max = 120)
    private String warranty;

    @AssertTrue(message = "Enter a VIN or chassis number of at least 6 characters for a Pre-Owned car.")
    public boolean isVinValidForCondition() {
        if ("NEW".equalsIgnoreCase(conditionType)) return true;
        return "USED".equalsIgnoreCase(conditionType) && vin != null && vin.trim().length() >= 6;
    }

    @AssertTrue(message = "New cars require a variant, fuel type, transmission, and engine or motor details.")
    public boolean isNewSpecificationsComplete() {
        if (!"NEW".equalsIgnoreCase(conditionType)) return true;
        return hasText(variant) && hasText(fuelType) && hasText(transmission) && hasText(engineSpec);
    }

    public Vehicle toVehicle() {
        Vehicle vehicle = new Vehicle();
        vehicle.setMake(make.trim());
        vehicle.setModel(model.trim());
        vehicle.setYear(year);
        vehicle.setVin(blankToNull(vin) == null ? null : vin.trim().toUpperCase(Locale.ROOT));
        vehicle.setBodyType(bodyType.trim());
        vehicle.setPrice(price);
        vehicle.setMileage(mileage);
        vehicle.setColor(color.trim());
        vehicle.setSellerName(sellerName.trim());
        vehicle.setStatus("AVAILABLE");
        vehicle.setConditionType(conditionType.trim().toUpperCase(Locale.ROOT));
        vehicle.setVariant(blankToNull(variant));
        vehicle.setFuelType(blankToNull(fuelType));
        vehicle.setTransmission(blankToNull(transmission));
        vehicle.setEngineSpec(blankToNull(engineSpec));
        vehicle.setPowerOutput(blankToNull(powerOutput));
        vehicle.setTorqueOutput(blankToNull(torqueOutput));
        vehicle.setEfficiencyRange(blankToNull(efficiencyRange));
        vehicle.setDrivetrain(blankToNull(drivetrain));
        vehicle.setSeatCount(seatCount);
        vehicle.setWarranty(blankToNull(warranty));
        return vehicle;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String blankToNull(String value) {
        return hasText(value) ? value.trim() : null;
    }

    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }
    public String getBodyType() { return bodyType; }
    public void setBodyType(String bodyType) { this.bodyType = bodyType; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getMileage() { return mileage; }
    public void setMileage(Integer mileage) { this.mileage = mileage; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }
    public String getConditionType() { return conditionType; }
    public void setConditionType(String conditionType) { this.conditionType = conditionType; }
    public String getVariant() { return variant; }
    public void setVariant(String variant) { this.variant = variant; }
    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }
    public String getTransmission() { return transmission; }
    public void setTransmission(String transmission) { this.transmission = transmission; }
    public String getEngineSpec() { return engineSpec; }
    public void setEngineSpec(String engineSpec) { this.engineSpec = engineSpec; }
    public String getPowerOutput() { return powerOutput; }
    public void setPowerOutput(String powerOutput) { this.powerOutput = powerOutput; }
    public String getTorqueOutput() { return torqueOutput; }
    public void setTorqueOutput(String torqueOutput) { this.torqueOutput = torqueOutput; }
    public String getEfficiencyRange() { return efficiencyRange; }
    public void setEfficiencyRange(String efficiencyRange) { this.efficiencyRange = efficiencyRange; }
    public String getDrivetrain() { return drivetrain; }
    public void setDrivetrain(String drivetrain) { this.drivetrain = drivetrain; }
    public Integer getSeatCount() { return seatCount; }
    public void setSeatCount(Integer seatCount) { this.seatCount = seatCount; }
    public String getWarranty() { return warranty; }
    public void setWarranty(String warranty) { this.warranty = warranty; }
}
