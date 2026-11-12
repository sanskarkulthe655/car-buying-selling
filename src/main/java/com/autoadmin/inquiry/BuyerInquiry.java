package com.autoadmin.inquiry;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "buyer_inquiries")
public class BuyerInquiry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "buyer_name", nullable = false, length = 160)
    private String buyerName;

    @Column(name = "buyer_email", nullable = false, length = 180)
    private String buyerEmail;

    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(nullable = false, length = 1500)
    private String message;

    @Column(nullable = false, length = 24)
    private String status = "NEW";

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    protected BuyerInquiry() { }

    public BuyerInquiry(String buyerName, String buyerEmail, Long vehicleId, String message) {
        this.buyerName = buyerName;
        this.buyerEmail = buyerEmail;
        this.vehicleId = vehicleId;
        this.message = message;
    }

    public Long getId() { return id; }
    public String getBuyerName() { return buyerName; }
    public String getBuyerEmail() { return buyerEmail; }
    public Long getVehicleId() { return vehicleId; }
    public String getMessage() { return message; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
