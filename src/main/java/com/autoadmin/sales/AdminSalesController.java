package com.autoadmin.sales;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminSalesController {
    private final AdminSalesService history;

    public AdminSalesController(AdminSalesService history) {
        this.history = history;
    }

    @GetMapping("/session")
    public AdminSessionView session(Authentication authentication) {
        return new AdminSessionView(authentication.getName(), "ADMIN");
    }

    @GetMapping("/sales")
    public List<AdminSalesService.SaleHistoryItem> sales() {
        return history.sales();
    }

    @GetMapping("/payment-attempts")
    public List<AdminSalesService.PaymentHistoryItem> paymentAttempts() {
        return history.paymentAttempts();
    }

    public record AdminSessionView(String username, String role) { }
}
