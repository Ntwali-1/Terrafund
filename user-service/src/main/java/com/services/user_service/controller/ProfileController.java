package com.services.user_service.controller;

import com.services.user_service.dto.*;
import com.services.user_service.entity.User;
import com.services.user_service.entity.UserActivity.ActivityType;
import com.services.user_service.repository.UserRepository;
import com.services.user_service.service.UserActivityService;
import com.services.user_service.service.UserRatingService;
import com.services.user_service.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final UserActivityService activityService;
    private final UserRatingService ratingService;
    private final UserRepository userRepository;

    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            HttpServletRequest httpRequest) {
        
        Long userId = getCurrentUserId();
        UserResponse user = userService.updateProfile(userId, request);

        // Log activity
        activityService.logActivity(
                userId,
                ActivityType.PROFILE_UPDATED,
                "User updated their profile",
                "USER",
                userId,
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent")
        );

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Profile updated successfully");
        response.put("user", user);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/kyc/submit")
    public ResponseEntity<Map<String, Object>> submitKYC(
            @Valid @RequestBody SubmitKYCRequest request,
            HttpServletRequest httpRequest) {
        
        Long userId = getCurrentUserId();
        UserResponse user = userService.submitKYC(userId, request);

        // Log activity
        activityService.logActivity(
                userId,
                ActivityType.VERIFICATION_SUBMITTED,
                "User submitted KYC documents",
                "USER",
                userId,
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent")
        );

        Map<String, Object> response = new HashMap<>();
        response.put("message", "KYC documents submitted successfully. Awaiting verification.");
        response.put("user", user);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/kyc/approve/{userId}")
    public ResponseEntity<Map<String, Object>> approveKYC(
            @PathVariable Long userId,
            @RequestParam(required = false) String notes) {
        
        Long adminId = getCurrentUserId();
        UserResponse user = userService.approveKYC(userId, adminId, notes);

        // Log activity
        activityService.logActivity(
                userId,
                ActivityType.VERIFICATION_APPROVED,
                "KYC verification approved by admin",
                "USER",
                userId,
                null,
                null
        );

        Map<String, Object> response = new HashMap<>();
        response.put("message", "KYC approved successfully");
        response.put("user", user);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/kyc/reject/{userId}")
    public ResponseEntity<Map<String, Object>> rejectKYC(
            @PathVariable Long userId,
            @RequestParam String reason) {
        
        Long adminId = getCurrentUserId();
        UserResponse user = userService.rejectKYC(userId, adminId, reason);

        // Log activity
        activityService.logActivity(
                userId,
                ActivityType.VERIFICATION_REJECTED,
                "KYC verification rejected: " + reason,
                "USER",
                userId,
                null,
                null
        );

        Map<String, Object> response = new HashMap<>();
        response.put("message", "KYC rejected");
        response.put("reason", reason);
        response.put("user", user);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/profile-completion")
    public ResponseEntity<Map<String, Object>> getProfileCompletion() {
        Long userId = getCurrentUserId();
        UserResponse user = userService.getCurrentUser(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("profileCompletionPercentage", user.getProfileCompletionPercentage());
        response.put("isComplete", user.getProfileCompletionPercentage() >= 100);

        return ResponseEntity.ok(response);
    }

    // Activity endpoints
    @GetMapping("/activities")
    public ResponseEntity<Page<UserActivityResponse>> getMyActivities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Long userId = getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        Page<UserActivityResponse> activities = activityService.getUserActivities(userId, pageable);

        return ResponseEntity.ok(activities);
    }

    @GetMapping("/activities/recent")
    public ResponseEntity<List<UserActivityResponse>> getRecentActivities(
            @RequestParam(defaultValue = "10") int limit) {
        
        Long userId = getCurrentUserId();
        List<UserActivityResponse> activities = activityService.getRecentActivities(userId, limit);

        return ResponseEntity.ok(activities);
    }

    @GetMapping("/{userId}/activities")
    public ResponseEntity<Page<UserActivityResponse>> getUserActivities(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<UserActivityResponse> activities = activityService.getUserActivities(userId, pageable);

        return ResponseEntity.ok(activities);
    }

    // Rating endpoints
    @PostMapping("/rate")
    public ResponseEntity<Map<String, Object>> rateUser(
            @Valid @RequestBody RateUserRequest request,
            HttpServletRequest httpRequest) {
        
        Long raterUserId = getCurrentUserId();
        User rater = userRepository.findById(raterUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserRatingResponse rating = ratingService.rateUser(request, raterUserId, rater.getFullName());

        // Log activity
        activityService.logActivity(
                raterUserId,
                ActivityType.RATING_GIVEN,
                "Rated user " + request.getRatedUserId() + " with " + request.getRating() + " stars",
                "USER",
                request.getRatedUserId(),
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent")
        );

        activityService.logActivity(
                request.getRatedUserId(),
                ActivityType.RATING_RECEIVED,
                "Received " + request.getRating() + " star rating from " + rater.getFullName(),
                "USER",
                raterUserId,
                null,
                null
        );

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Rating submitted successfully");
        response.put("rating", rating);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/ratings")
    public ResponseEntity<Page<UserRatingResponse>> getUserRatings(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<UserRatingResponse> ratings = ratingService.getUserRatings(userId, pageable);

        return ResponseEntity.ok(ratings);
    }

    @GetMapping("/ratings")
    public ResponseEntity<Page<UserRatingResponse>> getMyRatings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Long userId = getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        Page<UserRatingResponse> ratings = ratingService.getUserRatings(userId, pageable);

        return ResponseEntity.ok(ratings);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return user.getId();
        }

        throw new RuntimeException("User not authenticated");
    }
}
