package com.services.user_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "roles")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Column(length = 20)
    private String phoneNumber;

    // Profile Enhancements
    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "date_of_birth")
    private LocalDateTime dateOfBirth;

    // Address Information (for land owners)
    @Column(length = 100)
    private String country;

    @Column(length = 100)
    private String province;

    @Column(length = 100)
    private String district;

    @Column(length = 100)
    private String sector;

    @Column(length = 100)
    private String cell;

    @Column(length = 100)
    private String village;

    @Column(columnDefinition = "TEXT")
    private String streetAddress;

    // ID Verification / KYC
    @Column(name = "id_number", unique = true)
    private String idNumber;

    @Column(name = "id_type")
    private String idType; // NATIONAL_ID, PASSPORT, DRIVER_LICENSE

    @Column(name = "id_document_url")
    private String idDocumentUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status")
    private VerificationStatus verificationStatus = VerificationStatus.UNVERIFIED;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "verified_by")
    private Long verifiedBy; // Admin user ID who verified

    @Column(name = "verification_notes", columnDefinition = "TEXT")
    private String verificationNotes;

    // Profile Completion
    @Column(name = "profile_completion_percentage")
    private Integer profileCompletionPercentage = 0;

    // User Ratings
    @Column(name = "average_rating")
    private Double averageRating = 0.0;

    @Column(name = "total_ratings")
    private Integer totalRatings = 0;

    // Account Status
    @Column(nullable = false)
    private Boolean isVerified = false;

    @Column
    private String jwt = null;

    @Column(nullable = false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<UserRole> roles = new HashSet<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void addRole(UserRole role) {
        roles.add(role);
        role.setUser(this);
    }

    // Enums
    public enum VerificationStatus {
        UNVERIFIED,
        PENDING,
        VERIFIED,
        REJECTED
    }

    // Helper method to calculate profile completion
    public void calculateProfileCompletion() {
        int totalFields = 15; // Total important fields
        int completedFields = 0;

        if (fullName != null && !fullName.isBlank()) completedFields++;
        if (email != null && !email.isBlank()) completedFields++;
        if (phoneNumber != null && !phoneNumber.isBlank()) completedFields++;
        if (profilePictureUrl != null && !profilePictureUrl.isBlank()) completedFields++;
        if (bio != null && !bio.isBlank()) completedFields++;
        if (dateOfBirth != null) completedFields++;
        if (country != null && !country.isBlank()) completedFields++;
        if (province != null && !province.isBlank()) completedFields++;
        if (district != null && !district.isBlank()) completedFields++;
        if (sector != null && !sector.isBlank()) completedFields++;
        if (streetAddress != null && !streetAddress.isBlank()) completedFields++;
        if (idNumber != null && !idNumber.isBlank()) completedFields++;
        if (idType != null && !idType.isBlank()) completedFields++;
        if (idDocumentUrl != null && !idDocumentUrl.isBlank()) completedFields++;
        if (isVerified) completedFields++;

        this.profileCompletionPercentage = (completedFields * 100) / totalFields;
    }

}