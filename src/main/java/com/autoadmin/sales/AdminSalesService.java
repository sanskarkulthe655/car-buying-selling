package com.autoadmin.sales;

import com.autoadmin.payment.DemoPaymentAttempt;
import com.autoadmin.payment.DemoPaymentAttemptRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminSalesService {
    private final SaleRepository sales;
    private final DemoPaymentAttemptRepository attempts;

    public AdminSalesService(SaleRepository sales, DemoPaymentAttemptRepository attempts) {
        this.sales = sales;
        this.attempts = attempts;
    }

    @Transactional(readOnly = true)
    public List<SaleHistoryItem> sales() {
        return sales.findAllWithVehicleAndCustomer().stream().map(SaleHistoryItem::from).toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentHistoryItem> paymentAttempts() {
        return attempts.findAllWithDetails().stream().map(PaymentHistoryItem::from).toList();
    }

    public record SaleHistoryItem(Long id, Long vehicleId, String make, String model, Integer year,
                                  String buyerName, String buyerEmail, BigDecimal salePrice,
                                  String channel, Instant soldAt) {
        static SaleHistoryItem from(Sale sale) {
            return new SaleHistoryItem(sale.getId(), sale.getVehicle().getId(), sale.getVehicle().getMake(),
                sale.getVehicle().getModel(), sale.getVehicle().getYear(), sale.getCustomer().getFullName(),
                sale.getCustomer().getEmail(), sale.getSalePrice(), sale.getSaleChannel(), sale.getSoldAt());
        }
    }

    public record PaymentHistoryItem(Long id, String reference, Long vehicleId, String vehicleName,
                                     String buyerName, String buyerEmail, BigDecimal amount,
                                     String method, String outcome, Long saleId, Instant attemptedAt) {
        static PaymentHistoryItem from(DemoPaymentAttempt payment) {
            var vehicle = payment.getVehicle();
            return new PaymentHistoryItem(payment.getId(), payment.getDemoReference(), vehicle.getId(),
                vehicle.getYear() + " " + vehicle.getMake() + " " + vehicle.getModel(),
                payment.getCustomer().getFullName(), payment.getCustomer().getEmail(), payment.getAmount(),
                payment.getPaymentMethod(), payment.getOutcome(),
                payment.getSale() == null ? null : payment.getSale().getId(), payment.getCreatedAt());
        }
    }
}
