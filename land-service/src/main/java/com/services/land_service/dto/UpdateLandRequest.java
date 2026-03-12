package com.services.land_service.dto;

import com.services.land_service.entity.Land.AvailabilityType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateLandRequest {

    @Size(min = 2, max = 50, message = "Province must be between 2 and 50 characters")
    private String province;

    @Size(min = 2, max = 50, message = "District must be between 2 and 50 characters")
    private String district;

    @Size(min = 2, max = 50, message = "Sector must be between 2 and 50 characters")
    private String sector;

    @DecimalMin(value = "100.0", message = "Area must be at least 100 square meters")
    @DecimalMax(value = "10000000.0", message = "Area cannot exceed 10,000,000 square meters")
    private Double areaSqMeters;

    private AvailabilityType availabilityType;

    private java.util.List<String> imageUrls;
    private java.util.List<String> documentUrls;
}