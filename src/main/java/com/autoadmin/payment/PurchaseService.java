package com.autoadmin.payment;

import com.autoadmin.customer.Customer;
import com.autoadmin.customer.CustomerRepository;
import com.autoadmin.sales.Sale;
import com.autoadmin.sales.SaleRepository;
import com.autoadmin.vehicle.Vehicle;
import com.autoadmin.vehicle.VehicleRepository;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PurchaseService {
    private final VehicleRepository vehicles;
    private final CustomerRepository customers;
    private final SaleRepository sales;
    private final DemoPaymentAttemptRepository attempts;

    public PurchaseService(VehicleRepository vehicles, CustomerRepository customers,
                           SaleRepository sales, DemoPaymentAttemptRepository attempts) {
        this.vehicles = vehicles;
        this.customers = customers;
        this.sales = sales;
        this.attempts = attempts;
    }

    @Transactional
    public PurchaseResult simulate(Long vehicleId, DemoPaymentRequest request) {
        Vehicle vehicle = lockAvailableVehicle(vehicleId);
        Customer customer = saveCustomer(request.buyerName(), request.buyerEmail(), request.buyerPhone());
        boolean success = "SUCCESS".equals(request.simulatedOutcome());
        Sale sale = null;
        if (success) {
            if (sales.existsByVehicle_Id(vehicleId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "This car already has a completed sale record.");
            }
            sale = sales.save(new Sale(vehicle, customer, vehicle.getPrice(), "DEMO_PAYMENT"));
            vehicle.setStatus("SOLD");
        }
        String reference = "DEMO-" + UUID.randomUUID().toString().toUpperCase(Locale.ROOT);
        DemoPaymentAttempt attempt = attempts.save(new DemoPaymentAttempt(
            vehicle, customer, sale, vehicle.getPrice(), request.paymentMethod(),
            success ? "SUCCESS" : "FAILED", reference));
        String message = success
            ? "Demo payment succeeded. No real payment was processed; the car is recorded as sold."
            : "Demo payment was declined. No real payment was processed; the car remains available.";
        return new PurchaseResult(success, message, attempt.getId(), sale == null ? null : sale.getId(), reference);
    }

    @Transactional
    public PurchaseResult recordOfflineSale(Long vehicleId, OfflineSaleRequest request) {
        Vehicle vehicle = lockAvailableVehicle(vehicleId);
        Customer customer = saveCustomer(request.buyerName(), request.buyerEmail(), request.buyerPhone());
        BigDecimal salePrice = request.salePrice() == null ? vehicle.getPrice() : request.salePrice();
        if (salePrice.signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sale price cannot be negative.");
        }
        if (sales.existsByVehicle_Id(vehicleId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This car already has a completed sale record.");
        }
        Sale sale = sales.save(new Sale(vehicle, customer, salePrice, "OFFLINE"));
        vehicle.setStatus("SOLD");
        return new PurchaseResult(true, "Offline sale recorded in MySQL.", null, sale.getId(), null);
    }

    private Vehicle lockAvailableVehicle(Long vehicleId) {
        Vehicle vehicle = vehicles.findByIdForUpdate(vehicleId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found."));
        if (!"AVAILABLE".equalsIgnoreCase(vehicle.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This car is no longer available.");
        }
        return vehicle;
    }

    private Customer saveCustomer(String name, String email, String phone) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        Customer customer = customers.findByEmailIgnoreCase(normalizedEmail)
            .orElseGet(() -> new Customer(name.trim(), normalizedEmail, blankToNull(phone)));
        customer.setFullName(name.trim());
        customer.setPhone(blankToNull(phone));
        return customers.save(customer);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
