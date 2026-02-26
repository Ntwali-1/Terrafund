package com.services.user_service.repository;

import com.services.user_service.entity.UserRating;
import com.services.user_service.entity.UserRating.RatingType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRatingRepository extends JpaRepository<UserRating, Long> {

    Page<UserRating> findByRatedUserIdOrderByCreatedAtDesc(Long ratedUserId, Pageable pageable);

    List<UserRating> findByRatedUserId(Long ratedUserId);

    Optional<UserRating> findByRatedUserIdAndRaterUserId(Long ratedUserId, Long raterUserId);

    boolean existsByRatedUserIdAndRaterUserId(Long ratedUserId, Long raterUserId);

    @Query("SELECT AVG(r.rating) FROM UserRating r WHERE r.ratedUserId = :userId")
    Double calculateAverageRating(@Param("userId") Long userId);

    @Query("SELECT COUNT(r) FROM UserRating r WHERE r.ratedUserId = :userId")
    Long countRatings(@Param("userId") Long userId);

    @Query("SELECT AVG(r.rating) FROM UserRating r WHERE r.ratedUserId = :userId AND r.ratingType = :ratingType")
    Double calculateAverageRatingByType(@Param("userId") Long userId, @Param("ratingType") RatingType ratingType);
}
