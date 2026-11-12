package com.autoadmin.payment;

import com.autoadmin.customer.Customer;
import com.autoadmin.sales.Sale;
import com.autoadmin.vehicle.Vehicle;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "demo_payment_attempts")
public class DemoPaymentAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id")
    private Sale sale;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_method", nullable = false, length = 16)
    private String paymentMethod;

    @Column(nullable = false, length = 16)
    private String outcome;

    @Column(name = "demo_reference", nullable = false, unique = true, length = 64)
    private String demoReference;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    protected DemoPaymentAttempt() { }

    public DemoPaymentAttempt(Vehicle vehicle, Customer customer, Sale sale, BigDecimal amount,
                              String paymentMethod, String outcome, String demoReference) {
        this.vehicle = vehicle;
        this.customer = customer;
        this.sale = sale;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.outcome = outcome;
        this.demoReference = demoReference;
    }

    public Long getId() { return id; }
    public Vehicle getVehicle() { return vehicle; }
    public Customer getCustomer() { return customer; }
    public Sale getSale() { return sale; }
    public BigDecimal getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getOutcome() { return outcome; }
    public String getDemoReference() { return demoReference; }
    public Instant getCreatedAt() { return createdAt; }
}
