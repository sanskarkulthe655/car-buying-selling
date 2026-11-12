package com.autoadmin.vehicle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "vehicles")
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 80)
    private String make;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String model;

    @NotNull
    @Min(1900)
    @Column(name = "model_year", nullable = false)
    private Integer year;

    @Column(unique = true, length = 32)
    private String vin;

    @NotBlank
    @Column(name = "body_type", nullable = false, length = 40)
    private String bodyType;

    @NotNull
    @DecimalMin("0.0")
    @Column(name = "asking_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer mileage = 0;

    @NotBlank
    @Column(nullable = false, length = 24)
    private String status = "AVAILABLE";

    @Column(nullable = false, length = 40)
    private String color = "—";

    @NotBlank
    @Column(name = "seller_name", nullable = false, length = 120)
    private String sellerName = "Northline Motors";

    @NotBlank
    @Column(name = "condition_type", nullable = false, length = 12)
    private String conditionType = "USED";

    @Column(name = "variant_name", length = 120)
    private String variant;

    @Column(name = "fuel_type", length = 32)
    private String fuelType;

    @Column(length = 40)
    private String transmission;

    @Column(name = "engine_spec", length = 120)
    private String engineSpec;

    @Column(name = "power_output", length = 60)
    private String powerOutput;

    @Column(name = "torque_output", length = 60)
    private String torqueOutput;

    @Column(name = "efficiency_range", length = 80)
    private String efficiencyRange;

    @Column(length = 40)
    private String drivetrain;

    @Min(1)
    @Column(name = "seat_count")
    private Integer seatCount;

    @Column(length = 120)
    private String warranty;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    protected Vehicle() { }

    public Long getId() { return id; }
    public String getMake() { return make; }
    public String getModel() { return model; }
    public Integer getYear() { return year; }
    public String getVin() { return vin; }
    public String getBodyType() { return bodyType; }
    public BigDecimal getPrice() { return price; }
    public Integer getMileage() { return mileage; }
    public String getStatus() { return status; }
    public String getColor() { return color; }
    public String getSellerName() { return sellerName; }
    public String getConditionType() { return conditionType; }
    public String getVariant() { return variant; }
    public String getFuelType() { return fuelType; }
    public String getTransmission() { return transmission; }
    public String getEngineSpec() { return engineSpec; }
    public String getPowerOutput() { return powerOutput; }
    public String getTorqueOutput() { return torqueOutput; }
    public String getEfficiencyRange() { return efficiencyRange; }
    public String getDrivetrain() { return drivetrain; }
    public Integer getSeatCount() { return seatCount; }
    public String getWarranty() { return warranty; }
    public Instant getCreatedAt() { return createdAt; }

    public void setMake(String make) { this.make = make; }
    public void setModel(String model) { this.model = model; }
    public void setYear(Integer year) { this.year = year; }
    public void setVin(String vin) { this.vin = vin; }
    public void setBodyType(String bodyType) { this.bodyType = bodyType; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setMileage(Integer mileage) { this.mileage = mileage; }
    public void setStatus(String status) { this.status = status; }
    public void setColor(String color) { this.color = color; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }
    public void setConditionType(String conditionType) { this.conditionType = conditionType; }
    public void setVariant(String variant) { this.variant = variant; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }
    public void setTransmission(String transmission) { this.transmission = transmission; }
    public void setEngineSpec(String engineSpec) { this.engineSpec = engineSpec; }
    public void setPowerOutput(String powerOutput) { this.powerOutput = powerOutput; }
    public void setTorqueOutput(String torqueOutput) { this.torqueOutput = torqueOutput; }
    public void setEfficiencyRange(String efficiencyRange) { this.efficiencyRange = efficiencyRange; }
    public void setDrivetrain(String drivetrain) { this.drivetrain = drivetrain; }
    public void setSeatCount(Integer seatCount) { this.seatCount = seatCount; }
    public void setWarranty(String warranty) { this.warranty = warranty; }
}
