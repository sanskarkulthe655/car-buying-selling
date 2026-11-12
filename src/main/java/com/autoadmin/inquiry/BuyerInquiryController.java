package com.autoadmin.inquiry;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inquiries")
public class BuyerInquiryController {
    private final BuyerInquiryRepository inquiries;

    public BuyerInquiryController(BuyerInquiryRepository inquiries) {
        this.inquiries = inquiries;
    }

    @GetMapping
    public List<BuyerInquiry> listInquiries() {
        return inquiries.findAllByOrderByCreatedAtDesc();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BuyerInquiry createInquiry(@Valid @RequestBody InquiryRequest request) {
        return inquiries.save(new BuyerInquiry(
            request.buyerName().trim(), request.buyerEmail().trim(), request.vehicleId(), request.message().trim()
        ));
    }

    public record InquiryRequest(
        @NotBlank @Size(max = 160) String buyerName,
        @NotBlank @Email @Size(max = 180) String buyerEmail,
        Long vehicleId,
        @NotBlank @Size(max = 1500) String message
    ) { }
}
