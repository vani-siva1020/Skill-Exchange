package com.skillexchange.service;

import com.skillexchange.dto.ReviewRequest;
import com.skillexchange.model.Review;
import com.skillexchange.model.SkillSession;
import com.skillexchange.model.User;
import com.skillexchange.repository.ReviewRepository;
import com.skillexchange.repository.SessionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final SessionRepository sessionRepository;

    public ReviewService(ReviewRepository reviewRepository, SessionRepository sessionRepository) {
        this.reviewRepository = reviewRepository;
        this.sessionRepository = sessionRepository;
    }

    public Review submit(Long reviewerId, ReviewRequest req) {
        SkillSession session = sessionRepository.findById(req.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (session.getStatus() != SkillSession.Status.COMPLETED) {
            throw new IllegalStateException("You can only review a completed session");
        }

        User reviewer;
        User reviewee;
        if (session.getLearner().getId().equals(reviewerId)) {
            reviewer = session.getLearner();
            reviewee = session.getTeacher();
        } else if (session.getTeacher().getId().equals(reviewerId)) {
            reviewer = session.getTeacher();
            reviewee = session.getLearner();
        } else {
            throw new SecurityException("Only participants of this session can leave a review");
        }

        if (reviewRepository.findBySession_IdAndReviewer_Id(session.getId(), reviewerId).isPresent()) {
            throw new IllegalStateException("You already reviewed this session");
        }

        Review review = new Review();
        review.setSession(session);
        review.setReviewer(reviewer);
        review.setReviewee(reviewee);
        review.setRating(req.getRating());
        review.setComment(req.getComment());
        return reviewRepository.save(review);
    }

    public List<Review> getForUser(Long userId) {
        return reviewRepository.findByReviewee_IdOrderByCreatedAtDesc(userId);
    }

    public Map<String, Object> getRatingSummary(Long userId) {
        List<Review> reviews = getForUser(userId);
        double average = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        return Map.of(
                "average", Math.round(average * 10.0) / 10.0,
                "count", reviews.size()
        );
    }
}
