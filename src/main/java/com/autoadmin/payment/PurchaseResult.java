package com.autoadmin.payment;

public record PurchaseResult(boolean success, String message, Long attemptId, Long saleId, String reference) { }
