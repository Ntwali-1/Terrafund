package com.services.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitKYCRequest {

    @NotBlank(message = "ID number is required")
    private String idNumber;

    @NotBlank(message = "ID type is required")
    @Pattern(regexp = "NATIONAL_ID|PASSPORT|DRIVER_LICENSE", 
             message = "ID type must be NATIONAL_ID, PASSPORT, or DRIVER_LICENSE")
    private String idType;

    @NotBlank(message = "ID document URL is required")
    private String idDocumentUrl;
}
