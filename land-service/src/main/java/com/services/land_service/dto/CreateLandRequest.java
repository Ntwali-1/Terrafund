package com.services.land_service.dto;

import com.services.land_service.entity.Land.AvailabilityType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new land listing
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLandRequest {

    @NotBlank(message = "Province is required")
    @Size(min = 2, max = 50, message = "Province must be between 2 and 50 characters")
    private String province;

    @NotBlank(message = "District is required")
    @Size(min = 2, max = 50, message = "District must be between 2 and 50 characters")
    private String district;

    @NotBlank(message = "Sector is required")
    @Size(min = 2, max = 50, message = "Sector must be between 2 and 50 characters")
    private String sector;

    @NotNull(message = "Area is required")
    @DecimalMin(value = "100.0", message = "Area must be at least 100 square meters")
    @DecimalMax(value = "10000000.0", message = "Area cannot exceed 10,000,000 square meters")
    private Double areaSqMeters;

    @NotNull(message = "Availability type is required")
    private AvailabilityType availabilityType;

    private java.util.List<String> imageUrls;
    private java.util.List<String> documentUrls;
}