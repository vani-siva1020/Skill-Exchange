package com.skillexchange.controller;

import com.skillexchange.model.MatchResult;
import com.skillexchange.model.User;
import com.skillexchange.repository.UserRepository;
import com.skillexchange.service.MatchService;
import com.skillexchange.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final MatchService matchService;
    private final ReviewService reviewService;

    public UserController(UserRepository userRepository, MatchService matchService, ReviewService reviewService) {
        this.userRepository = userRepository;
        this.matchService = matchService;
        this.reviewService = reviewService;
    }

    // NOTE: registration now happens via POST /api/auth/register (with a password).

    // List every registered user
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get a single user by id
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found")));
    }

    // Real-time match finder: who can teach this user, and who this user can teach
    @GetMapping("/{id}/matches")
    public ResponseEntity<?> getMatches(@PathVariable Long id) {
        try {
            List<MatchResult> matches = matchService.findMatches(id);
            return ResponseEntity.ok(matches);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }
    }

    // Average rating + review count for a user, built from completed-session reviews
    @GetMapping("/{id}/rating")
    public Map<String, Object> getRating(@PathVariable Long id) {
        return reviewService.getRatingSummary(id);
    }

    // Delete a user (e.g. after they leave the platform)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
