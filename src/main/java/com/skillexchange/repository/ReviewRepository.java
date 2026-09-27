package com.skillexchange.repository;

import com.skillexchange.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByReviewee_IdOrderByCreatedAtDesc(Long revieweeId);
    Optional<Review> findBySession_IdAndReviewer_Id(Long sessionId, Long reviewerId);
}
