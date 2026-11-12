package com.autoadmin.payment;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DemoPaymentAttemptRepository extends JpaRepository<DemoPaymentAttempt, Long> {
    boolean existsByVehicle_Id(Long vehicleId);

    @Query("select p from DemoPaymentAttempt p join fetch p.vehicle join fetch p.customer left join fetch p.sale order by p.createdAt desc")
    List<DemoPaymentAttempt> findAllWithDetails();
}
