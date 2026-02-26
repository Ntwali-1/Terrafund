package com.services.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRatingResponse {

    private Long id;
    private Long raterUserId;
    private String raterName;
    private Integer rating;
    private String comment;
    private String ratingType;
    private Long investmentId;
    private Long landId;
    private LocalDateTime createdAt;
}
