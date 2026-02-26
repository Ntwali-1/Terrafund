package com.services.user_service.service;

import com.services.user_service.dto.UserActivityResponse;
import com.services.user_service.entity.UserActivity.ActivityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserActivityService {

    void logActivity(Long userId, ActivityType activityType, String description, 
                    String entityType, Long entityId, String ipAddress, String userAgent);

    Page<UserActivityResponse> getUserActivities(Long userId, Pageable pageable);

    Page<UserActivityResponse> getUserActivitiesByType(Long userId, ActivityType activityType, Pageable pageable);

    List<UserActivityResponse> getRecentActivities(Long userId, int limit);
}
