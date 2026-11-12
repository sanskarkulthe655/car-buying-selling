package com.autoadmin.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record OfflineSaleRequest(
    @NotBlank @Size(max = 160) String buyerName,
    @NotBlank @Email @Size(max = 180) String buyerEmail,
    @Size(max = 40) String buyerPhone,
    @DecimalMin(value = "0.00") BigDecimal salePrice
) { }
