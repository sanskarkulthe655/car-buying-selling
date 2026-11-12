package com.autoadmin.vehicle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VehicleListingFormTest {
    private static Validator validator;
    private static AutoCloseable validatorFactory;
    private VehicleListingForm form;

    @BeforeAll
    static void createValidator() {
        var factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        validatorFactory = factory::close;
    }

    @AfterAll
    static void closeValidator() throws Exception {
        validatorFactory.close();
    }

    @BeforeEach
    void setUp() {
        form = new VehicleListingForm();
        form.setMake("Example Motors");
        form.setModel("Roadster");
        form.setYear(2026);
        form.setBodyType("SUV");
        form.setPrice(new BigDecimal("1500000"));
        form.setMileage(0);
        form.setColor("Graphite");
        form.setSellerName("Showroom");
    }

    @Test
    void preOwnedListingsRequireAVin() {
        form.setConditionType("USED");
        form.setVin(" ");
        assertTrue(messages().contains("Enter a VIN or chassis number of at least 6 characters for a Pre-Owned car."));
    }

    @Test
    void newListingsRequireCoreModelSpecifications() {
        form.setConditionType("NEW");
        assertTrue(messages().contains("New cars require a variant, fuel type, transmission, and engine or motor details."));
    }

    @Test
    void validNewListingNeedsNoVinAndMapsSpecifications() {
        form.setConditionType("NEW");
        form.setVariant("  Premium  ");
        form.setFuelType("Electric");
        form.setTransmission("Single-speed");
        form.setEngineSpec("45 kWh electric motor");
        form.setPowerOutput("110 kW");
        form.setEfficiencyRange("350 km range");
        form.setDrivetrain("FWD");
        form.setSeatCount(5);
        form.setWarranty("3 years");

        assertTrue(validator.validate(form).isEmpty());
        Vehicle vehicle = form.toVehicle();
        assertEquals("NEW", vehicle.getConditionType());
        assertNull(vehicle.getVin());
        assertEquals("Premium", vehicle.getVariant());
        assertEquals("Electric", vehicle.getFuelType());
        assertEquals("Single-speed", vehicle.getTransmission());
        assertEquals("45 kWh electric motor", vehicle.getEngineSpec());
        assertEquals("110 kW", vehicle.getPowerOutput());
        assertEquals("350 km range", vehicle.getEfficiencyRange());
        assertEquals(5, vehicle.getSeatCount());
        assertEquals("3 years", vehicle.getWarranty());
    }

    @Test
    void validPreOwnedListingStillMapsVinAndCondition() {
        form.setConditionType("USED");
        form.setVin(" ab123456 ");
        assertTrue(validator.validate(form).isEmpty());
        Vehicle vehicle = form.toVehicle();
        assertEquals("USED", vehicle.getConditionType());
        assertEquals("AB123456", vehicle.getVin());
    }

    private Set<String> messages() {
        return validator.validate(form).stream().map(v -> v.getMessage()).collect(Collectors.toSet());
    }
}
