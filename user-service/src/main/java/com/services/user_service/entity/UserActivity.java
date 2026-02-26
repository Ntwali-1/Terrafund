package com.services.user_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_activities", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_activity_type", columnList = "activity_type"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 50)
    private ActivityType activityType;

    @Column(nullable = false)
    private String description;

    @Column(name = "entity_type", length = 50)
    private String entityType; // LAND, INVESTMENT, USER, etc.

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(columnDefinition = "TEXT")
    private String metadata; // JSON string for additional data

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum ActivityType {
        // Authentication
        LOGIN,
        LOGOUT,
        SIGNUP,
        EMAIL_VERIFIED,
        PASSWORD_CHANGED,
        
        // Profile
        PROFILE_UPDATED,
        PROFILE_PICTURE_UPDATED,
        ROLE_ADDED,
        
        // Land Activities
        LAND_CREATED,
        LAND_UPDATED,
        LAND_DELETED,
        LAND_VIEWED,
        
        // Investment Activities
        INVESTMENT_CREATED,
        INVESTMENT_UPDATED,
        INVESTMENT_APPROVED,
        INVESTMENT_REJECTED,
        INVESTMENT_CANCELLED,
        
        // Ratings
        RATING_GIVEN,
        RATING_RECEIVED,
        
        // Other
        DOCUMENT_UPLOADED,
        VERIFICATION_SUBMITTED,
        VERIFICATION_APPROVED,
        VERIFICATION_REJECTED
    }
}
