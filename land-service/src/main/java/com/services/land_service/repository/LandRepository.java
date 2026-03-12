package com.services.land_service.repository;

import com.services.land_service.entity.Land;
import com.services.land_service.entity.Land.LandStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LandRepository extends JpaRepository<Land, Long> {

    Page<Land> findByProvinceContainingIgnoreCase(String province, Pageable pageable);
    Page<Land> findByDistrictContainingIgnoreCase(String district, Pageable pageable);
    Page<Land> findBySectorContainingIgnoreCase(String sector, Pageable pageable);

    Page<Land> findByStatus(LandStatus status, Pageable pageable);

    Page<Land> findByAreaSqMetersBetween(Double minArea, Double maxArea, Pageable pageable);
    Page<Land> findByOwnerId(Long ownerId, Pageable pageable);
}