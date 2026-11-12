package com.autoadmin.inquiry;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuyerInquiryRepository extends JpaRepository<BuyerInquiry, Long> {
    List<BuyerInquiry> findAllByOrderByCreatedAtDesc();
}
