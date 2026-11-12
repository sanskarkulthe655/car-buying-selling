package com.autoadmin.vehicle;

import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VehicleService {
    private final VehicleRepository vehicles;

    public VehicleService(VehicleRepository vehicles) {
        this.vehicles = vehicles;
    }

    @Transactional(readOnly = true)
    public List<Vehicle> listAvailableVehicles(String condition) {
        if (condition == null || condition.isBlank()) {
            return vehicles.findByStatusOrderByCreatedAtDesc("AVAILABLE");
        }
        String normalized = condition.trim().toUpperCase(Locale.ROOT);
        if (!normalized.equals("NEW") && !normalized.equals("USED")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Condition must be NEW or USED.");
        }
        return vehicles.findByStatusAndConditionTypeOrderByCreatedAtDesc("AVAILABLE", normalized);
    }
}
