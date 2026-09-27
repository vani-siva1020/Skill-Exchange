package com.skillexchange.controller;

import com.skillexchange.dto.ReviewRequest;
import com.skillexchange.model.Review;
import com.skillexchange.service.ReviewService;
import com.skillexchange.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final TokenService tokenService;

    public ReviewController(ReviewService reviewService, TokenService tokenService) {
        this.reviewService = reviewService;
        this.tokenService = tokenService;
    }

    @PostMapping
    public ResponseEntity<?> submit(@RequestHeader(value = "Authorization", required = false) String token,
                                     @Valid @RequestBody ReviewRequest req) {
        Optional<Long> reviewerId = tokenService.resolve(token);
        if (reviewerId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Please log in first"));
        }
        try {
            Review review = reviewService.submit(reviewerId.get(), req);
            return ResponseEntity.status(HttpStatus.CREATED).body(review);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public List<Review> getForUser(@PathVariable Long userId) {
        return reviewService.getForUser(userId);
    }
}
