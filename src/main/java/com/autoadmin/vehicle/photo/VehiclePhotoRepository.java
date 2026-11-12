package com.autoadmin.vehicle.photo;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehiclePhotoRepository extends JpaRepository<VehiclePhoto, Long> {
    long countByVehicleId(Long vehicleId);

    @Query("select new com.autoadmin.vehicle.photo.VehiclePhotoMetadata(p.id, p.fileName, p.contentType, p.sortOrder) "
        + "from VehiclePhoto p where p.vehicleId = :vehicleId order by p.sortOrder asc, p.id asc")
    List<VehiclePhotoMetadata> findMetadataByVehicleId(@Param("vehicleId") Long vehicleId);

    Optional<VehiclePhoto> findByIdAndVehicleId(Long id, Long vehicleId);
}
