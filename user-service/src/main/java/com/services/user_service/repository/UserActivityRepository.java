package com.services.user_service.repository;

import com.services.user_service.entity.UserActivity;
import com.services.user_service.entity.UserActivity.ActivityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {

    Page<UserActivity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<UserActivity> findByUserIdAndActivityTypeOrderByCreatedAtDesc(
            Long userId, ActivityType activityType, Pageable pageable);

    List<UserActivity> findByUserIdAndCreatedAtBetween(
            Long userId, LocalDateTime startDate, LocalDateTime endDate);

    long countByUserIdAndActivityType(Long userId, ActivityType activityType);

    List<UserActivity> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);
}
