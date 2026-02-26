package com.services.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityResponse {

    private Long id;
    private String activityType;
    private String description;
    private String entityType;
    private Long entityId;
    private LocalDateTime createdAt;
}
