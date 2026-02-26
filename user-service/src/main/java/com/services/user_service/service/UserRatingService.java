package com.services.user_service.service;

import com.services.user_service.dto.RateUserRequest;
import com.services.user_service.dto.UserRatingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRatingService {

    UserRatingResponse rateUser(RateUserRequest request, Long raterUserId, String raterName);

    Page<UserRatingResponse> getUserRatings(Long userId, Pageable pageable);

    void updateUserAverageRating(Long userId);
}
