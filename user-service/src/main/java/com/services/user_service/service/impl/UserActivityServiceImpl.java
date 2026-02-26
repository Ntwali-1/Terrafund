package com.services.user_service.service.impl;

import com.services.user_service.dto.UserActivityResponse;
import com.services.user_service.entity.UserActivity;
import com.services.user_service.entity.UserActivity.ActivityType;
import com.services.user_service.repository.UserActivityRepository;
import com.services.user_service.service.UserActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserActivityServiceImpl implements UserActivityService {

    private final UserActivityRepository activityRepository;

    @Override
    @Transactional
    public void logActivity(Long userId, ActivityType activityType, String description,
                           String entityType, Long entityId, String ipAddress, String userAgent) {
        try {
            UserActivity activity = new UserActivity();
            activity.setUserId(userId);
            activity.setActivityType(activityType);
            activity.setDescription(description);
            activity.setEntityType(entityType);
            activity.setEntityId(entityId);
            activity.setIpAddress(ipAddress);
            activity.setUserAgent(userAgent);

            activityRepository.save(activity);
            log.info("Activity logged: userId={}, type={}", userId, activityType);
        } catch (Exception e) {
            log.error("Failed to log activity for user: {}", userId, e);
            // Don't throw exception - activity logging should not break main flow
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserActivityResponse> getUserActivities(Long userId, Pageable pageable) {
        return activityRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserActivityResponse> getUserActivitiesByType(Long userId, ActivityType activityType, Pageable pageable) {
        return activityRepository.findByUserIdAndActivityTypeOrderByCreatedAtDesc(userId, activityType, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserActivityResponse> getRecentActivities(Long userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return activityRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private UserActivityResponse mapToResponse(UserActivity activity) {
        UserActivityResponse response = new UserActivityResponse();
        response.setId(activity.getId());
        response.setActivityType(activity.getActivityType().name());
        response.setDescription(activity.getDescription());
        response.setEntityType(activity.getEntityType());
        response.setEntityId(activity.getEntityId());
        response.setCreatedAt(activity.getCreatedAt());
        return response;
    }
}
