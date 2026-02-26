package com.services.user_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_ratings", indexes = {
        @Index(name = "idx_rated_user_id", columnList = "rated_user_id"),
        @Index(name = "idx_rater_user_id", columnList = "rater_user_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rated_user_id", nullable = false)
    private Long ratedUserId; // User being rated

    @Column(name = "rater_user_id", nullable = false)
    private Long raterUserId; // User giving the rating

    @Column(name = "rater_name")
    private String raterName;

    @Column(nullable = false)
    private Integer rating; // 1-5 stars

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "investment_id")
    private Long investmentId; // Related investment (optional)

    @Column(name = "land_id")
    private Long landId; // Related land (optional)

    @Enumerated(EnumType.STRING)
    @Column(name = "rating_type", nullable = false)
    private RatingType ratingType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum RatingType {
        AS_INVESTOR,    // Rating user as an investor
        AS_LAND_OWNER,  // Rating user as a land owner
        GENERAL         // General rating
    }
}
