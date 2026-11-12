package com.autoadmin.sales;

import com.autoadmin.customer.Customer;
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
@Table(name = "sales")
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "sale_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal salePrice;

    @Column(name = "sold_at", insertable = false, updatable = false)
    private Instant soldAt;

    @Column(name = "sale_channel", nullable = false, length = 24)
    private String saleChannel = "OFFLINE";

    protected Sale() { }

    public Sale(Vehicle vehicle, Customer customer, BigDecimal salePrice, String saleChannel) {
        this.vehicle = vehicle;
        this.customer = customer;
        this.salePrice = salePrice;
        this.saleChannel = saleChannel;
    }

    public Long getId() { return id; }
    public Vehicle getVehicle() { return vehicle; }
    public Customer getCustomer() { return customer; }
    public BigDecimal getSalePrice() { return salePrice; }
    public Instant getSoldAt() { return soldAt; }
    public String getSaleChannel() { return saleChannel; }
}
