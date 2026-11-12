package com.autoadmin.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.autoadmin.customer.Customer;
import com.autoadmin.customer.CustomerRepository;
import com.autoadmin.sales.Sale;
import com.autoadmin.sales.SaleRepository;
import com.autoadmin.vehicle.Vehicle;
import com.autoadmin.vehicle.VehicleListingForm;
import com.autoadmin.vehicle.VehicleRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {
    @Mock private VehicleRepository vehicles;
    @Mock private CustomerRepository customers;
    @Mock private SaleRepository sales;
    @Mock private DemoPaymentAttemptRepository attempts;
    @InjectMocks private PurchaseService purchaseService;
    private Vehicle vehicle;
    private Customer customer;

    @BeforeEach
    void setUp() {
        VehicleListingForm form = new VehicleListingForm();
        form.setMake("AutoAdmin");
        form.setModel("Demo");
        form.setYear(2026);
        form.setBodyType("Hatchback");
        form.setPrice(new BigDecimal("850000.00"));
        form.setMileage(0);
        form.setColor("Graphite");
        form.setSellerName("AutoAdmin Motors");
        form.setConditionType("NEW");
        vehicle = form.toVehicle();
        vehicle.setStatus("AVAILABLE");
        customer = new Customer("Demo Buyer", "buyer@example.com", null);
    }

    @Test
    void successfulDemoPaymentRecordsSaleAndMarksCarSold() {
        when(vehicles.findByIdForUpdate(7L)).thenReturn(Optional.of(vehicle));
        when(customers.findByEmailIgnoreCase("buyer@example.com")).thenReturn(Optional.of(customer));
        when(customers.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(sales.existsByVehicle_Id(7L)).thenReturn(false);
        when(sales.save(any(Sale.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(attempts.save(any(DemoPaymentAttempt.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseResult result = purchaseService.simulate(7L,
            new DemoPaymentRequest("Demo Buyer", "buyer@example.com", "", "CARD", "SUCCESS"));

        assertTrue(result.success());
        assertEquals("SOLD", vehicle.getStatus());
        ArgumentCaptor<Sale> saleCaptor = ArgumentCaptor.forClass(Sale.class);
        verify(sales).save(saleCaptor.capture());
        assertEquals("DEMO_PAYMENT", saleCaptor.getValue().getSaleChannel());
        ArgumentCaptor<DemoPaymentAttempt> attemptCaptor = ArgumentCaptor.forClass(DemoPaymentAttempt.class);
        verify(attempts).save(attemptCaptor.capture());
        assertEquals("SUCCESS", attemptCaptor.getValue().getOutcome());
        assertEquals("CARD", attemptCaptor.getValue().getPaymentMethod());
        assertTrue(attemptCaptor.getValue().getSale() != null);
    }

    @Test
    void failedDemoPaymentLogsAttemptWithoutSaleOrSellingCar() {
        when(vehicles.findByIdForUpdate(7L)).thenReturn(Optional.of(vehicle));
        when(customers.findByEmailIgnoreCase("buyer@example.com")).thenReturn(Optional.of(customer));
        when(customers.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(attempts.save(any(DemoPaymentAttempt.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseResult result = purchaseService.simulate(7L,
            new DemoPaymentRequest("Demo Buyer", "buyer@example.com", "", "UPI", "FAILED"));

        assertFalse(result.success());
        assertEquals("AVAILABLE", vehicle.getStatus());
        verify(sales, never()).save(any(Sale.class));
        verify(sales, never()).existsByVehicle_Id(7L);
        ArgumentCaptor<DemoPaymentAttempt> attemptCaptor = ArgumentCaptor.forClass(DemoPaymentAttempt.class);
        verify(attempts).save(attemptCaptor.capture());
        assertEquals("FAILED", attemptCaptor.getValue().getOutcome());
        assertEquals("UPI", attemptCaptor.getValue().getPaymentMethod());
        assertNull(attemptCaptor.getValue().getSale());
    }
}
