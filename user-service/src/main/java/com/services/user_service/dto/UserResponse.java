package com.services.user_service.dto;

import com.services.user_service.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private Boolean isVerified;
    private String jwtToken;
    private Boolean isActive;
    private Set<Role> roles;
    
    // Profile enhancements
    private String profilePictureUrl;
    private String bio;
    private LocalDateTime dateOfBirth;
    
    // Address
    private String country;
    private String province;
    private String district;
    private String sector;
    private String cell;
    private String village;
    private String streetAddress;
    
    // KYC
    private String idNumber;
    private String idType;
    private String verificationStatus;
    private LocalDateTime verifiedAt;
    
    // Profile completion
    private Integer profileCompletionPercentage;
    
    // Ratings
    private Double averageRating;
    private Integer totalRatings;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}