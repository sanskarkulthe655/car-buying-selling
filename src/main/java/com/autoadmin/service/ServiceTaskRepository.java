package com.autoadmin.service;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceTaskRepository extends JpaRepository<ServiceTask, Long> {
    boolean existsByVehicle_Id(Long vehicleId);
}
