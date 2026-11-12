package com.autoadmin.vehicle;

import com.autoadmin.payment.DemoPaymentAttemptRepository;
import com.autoadmin.sales.SaleRepository;
import com.autoadmin.service.ServiceTaskRepository;
import com.autoadmin.vehicle.photo.VehiclePhotoService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminVehicleService {
    private final VehicleRepository vehicles;
    private final VehicleListingService listingService;
    private final VehiclePhotoService photos;
    private final SaleRepository sales;
    private final DemoPaymentAttemptRepository attempts;
    private final ServiceTaskRepository serviceTasks;

    public AdminVehicleService(VehicleRepository vehicles, VehicleListingService listingService,
                               VehiclePhotoService photos, SaleRepository sales,
                               DemoPaymentAttemptRepository attempts, ServiceTaskRepository serviceTasks) {
        this.vehicles = vehicles;
        this.listingService = listingService;
        this.photos = photos;
        this.sales = sales;
        this.attempts = attempts;
        this.serviceTasks = serviceTasks;
    }

    @Transactional(readOnly = true)
    public List<AdminVehicleListing> listAll() {
        return vehicles.findAllByOrderByCreatedAtDesc().stream().map(AdminVehicleListing::from).toList();
    }

    @Transactional
    public Vehicle create(VehicleListingForm form, List<MultipartFile> uploads) {
        return listingService.createListing(form.toVehicle(), uploads);
    }

    @Transactional
    public Vehicle update(Long id, VehicleListingForm form, List<MultipartFile> uploads) {
        Vehicle current = vehicles.findByIdForUpdate(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found."));
        if (!"AVAILABLE".equalsIgnoreCase(current.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Sold cars cannot be edited.");
        }
        Vehicle incoming = form.toVehicle();
        current.setMake(incoming.getMake());
        current.setModel(incoming.getModel());
        current.setYear(incoming.getYear());
        current.setVin(incoming.getVin());
        current.setBodyType(incoming.getBodyType());
        current.setPrice(incoming.getPrice());
        current.setMileage(incoming.getMileage());
        current.setColor(incoming.getColor());
        current.setSellerName(incoming.getSellerName());
        current.setConditionType(incoming.getConditionType());
        current.setVariant(incoming.getVariant());
        current.setFuelType(incoming.getFuelType());
        current.setTransmission(incoming.getTransmission());
        current.setEngineSpec(incoming.getEngineSpec());
        current.setPowerOutput(incoming.getPowerOutput());
        current.setTorqueOutput(incoming.getTorqueOutput());
        current.setEfficiencyRange(incoming.getEfficiencyRange());
        current.setDrivetrain(incoming.getDrivetrain());
        current.setSeatCount(incoming.getSeatCount());
        current.setWarranty(incoming.getWarranty());
        photos.savePhotos(id, uploads);
        return current;
    }

    @Transactional
    public void delete(Long id) {
        Vehicle current = vehicles.findByIdForUpdate(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found."));
        if (!"AVAILABLE".equalsIgnoreCase(current.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Sold cars cannot be deleted.");
        }
        if (sales.existsByVehicle_Id(id) || attempts.existsByVehicle_Id(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This car has sale or payment history and cannot be deleted.");
        }
        if (serviceTasks.existsByVehicle_Id(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This car has service history and cannot be deleted.");
        }
        vehicles.delete(current);
    }

    @Transactional
    public void deletePhoto(Long vehicleId, Long photoId) {
        Vehicle vehicle = vehicles.findById(vehicleId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found."));
        if (!"AVAILABLE".equalsIgnoreCase(vehicle.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Photos on sold cars cannot be changed.");
        }
        photos.deletePhoto(vehicleId, photoId);
    }

    @Transactional
    public void addPhotos(Long vehicleId, List<MultipartFile> uploads) {
        Vehicle vehicle = vehicles.findById(vehicleId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found."));
        if (!"AVAILABLE".equalsIgnoreCase(vehicle.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Photos on sold cars cannot be changed.");
        }
        photos.savePhotos(vehicleId, uploads);
    }
}
