package com.autoadmin.vehicle;

import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class VehicleController {
    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping("/vehicles")
    public List<VehiclePublicListing> listVehicles(@RequestParam(name = "condition", required = false) String condition) {
        return vehicleService.listAvailableVehicles(condition).stream()
            .map(VehiclePublicListing::from)
            .toList();
    }
}
