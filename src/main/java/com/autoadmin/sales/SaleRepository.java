package com.autoadmin.sales;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    boolean existsByVehicle_Id(Long vehicleId);

    @Query("select s from Sale s join fetch s.vehicle join fetch s.customer order by s.soldAt desc")
    List<Sale> findAllWithVehicleAndCustomer();
}
