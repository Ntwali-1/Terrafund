package com.services.user_service.service.impl;

import com.services.user_service.dto.RateUserRequest;
import com.services.user_service.dto.UserRatingResponse;
import com.services.user_service.entity.User;
import com.services.user_service.entity.UserRating;
import com.services.user_service.entity.UserRating.RatingType;
import com.services.user_service.repository.UserRatingRepository;
import com.services.user_service.repository.UserRepository;
import com.services.user_service.service.UserRatingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRatingServiceImpl implements UserRatingService {

    private final UserRatingRepository ratingRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserRatingResponse rateUser(RateUserRequest request, Long raterUserId, String raterName) {
        // Validate users exist
        User ratedUser = userRepository.findById(request.getRatedUserId())
                .orElseThrow(() -> new RuntimeException("User to be rated not found"));

        if (raterUserId.equals(request.getRatedUserId())) {
            throw new RuntimeException("You cannot rate yourself");
        }

        // Check if already rated
        if (ratingRepository.existsByRatedUserIdAndRaterUserId(request.getRatedUserId(), raterUserId)) {
            throw new RuntimeException("You have already rated this user");
        }

        // Create rating
        UserRating rating = new UserRating();
        rating.setRatedUserId(request.getRatedUserId());
        rating.setRaterUserId(raterUserId);
        rating.setRaterName(raterName);
        rating.setRating(request.getRating());
        rating.setComment(request.getComment());
        rating.setInvestmentId(request.getInvestmentId());
        rating.setLandId(request.getLandId());
        rating.setRatingType(RatingType.valueOf(request.getRatingType()));

        UserRating savedRating = ratingRepository.save(rating);
        log.info("User {} rated user {} with {} stars", raterUserId, request.getRatedUserId(), request.getRating());

        // Update average rating
        updateUserAverageRating(request.getRatedUserId());

        return mapToResponse(savedRating);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserRatingResponse> getUserRatings(Long userId, Pageable pageable) {
        return ratingRepository.findByRatedUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public void updateUserAverageRating(Long userId) {
        Double averageRating = ratingRepository.calculateAverageRating(userId);
        Long totalRatings = ratingRepository.countRatings(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setAverageRating(averageRating != null ? averageRating : 0.0);
        user.setTotalRatings(totalRatings != null ? totalRatings.intValue() : 0);

        userRepository.save(user);
        log.info("Updated average rating for user {}: {} ({} ratings)", 
                userId, averageRating, totalRatings);
    }

    private UserRatingResponse mapToResponse(UserRating rating) {
        UserRatingResponse response = new UserRatingResponse();
        response.setId(rating.getId());
        response.setRaterUserId(rating.getRaterUserId());
        response.setRaterName(rating.getRaterName());
        response.setRating(rating.getRating());
        response.setComment(rating.getComment());
        response.setRatingType(rating.getRatingType().name());
        response.setInvestmentId(rating.getInvestmentId());
        response.setLandId(rating.getLandId());
        response.setCreatedAt(rating.getCreatedAt());
        return response;
    }
}
