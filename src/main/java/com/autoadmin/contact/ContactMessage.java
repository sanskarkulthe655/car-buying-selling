package com.autoadmin.contact;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "contact_messages")
public class ContactMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_name", nullable = false, length = 160)
    private String senderName;

    @Column(name = "sender_email", nullable = false, length = 180)
    private String senderEmail;

    @Column(length = 40)
    private String phone;

    @Column(nullable = false, length = 160)
    private String subject;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(nullable = false, length = 24)
    private String status = "NEW";

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    protected ContactMessage() { }

    public ContactMessage(String senderName, String senderEmail, String phone, String subject, String message) {
        this.senderName = senderName;
        this.senderEmail = senderEmail;
        this.phone = phone;
        this.subject = subject;
        this.message = message;
    }

    public Long getId() { return id; }
    public String getSenderName() { return senderName; }
    public String getSenderEmail() { return senderEmail; }
    public String getPhone() { return phone; }
    public String getSubject() { return subject; }
    public String getMessage() { return message; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
