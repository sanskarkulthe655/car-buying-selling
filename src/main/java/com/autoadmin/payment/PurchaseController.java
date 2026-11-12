package com.autoadmin.payment;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PurchaseController {
    private final PurchaseService purchases;

    public PurchaseController(PurchaseService purchases) {
        this.purchases = purchases;
    }

    @PostMapping("/purchases/{vehicleId}")
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseResult simulate(@PathVariable Long vehicleId, @Valid @RequestBody DemoPaymentRequest request) {
        return purchases.simulate(vehicleId, request);
    }

    @PostMapping("/admin/sales/offline/{vehicleId}")
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseResult offlineSale(@PathVariable Long vehicleId, @Valid @RequestBody OfflineSaleRequest request) {
        return purchases.recordOfflineSale(vehicleId, request);
    }
}
