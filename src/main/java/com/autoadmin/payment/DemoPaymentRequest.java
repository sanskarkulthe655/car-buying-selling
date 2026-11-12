package com.autoadmin.payment;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DemoPaymentRequest(
    @NotBlank @Size(max = 160) String buyerName,
    @NotBlank @Email @Size(max = 180) String buyerEmail,
    @Size(max = 40) String buyerPhone,
    @NotBlank @Pattern(regexp = "CARD|UPI", message = "Choose Demo Card or Demo UPI.") String paymentMethod,
    @NotBlank @Pattern(regexp = "SUCCESS|FAILED", message = "Choose a demo success or failure outcome.") String simulatedOutcome
) { }
