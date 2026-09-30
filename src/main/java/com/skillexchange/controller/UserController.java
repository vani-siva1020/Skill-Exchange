package com.skillexchange.controller;

import com.skillexchange.dto.UpdateProfileRequest;
import com.skillexchange.model.MatchResult;
import com.skillexchange.model.User;
import com.skillexchange.repository.UserRepository;
import com.skillexchange.service.MatchService;
import com.skillexchange.service.ReviewService;
import com.skillexchange.service.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final MatchService matchService;
    private final ReviewService reviewService;
    private final TokenService tokenService;

    public UserController(UserRepository userRepository, MatchService matchService,
                          ReviewService reviewService, TokenService tokenService) {
        this.userRepository = userRepository;
        this.matchService = matchService;
        this.reviewService = reviewService;
        this.tokenService = tokenService;
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

    // Update the logged-in user's bio/experience text and certificate photo
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestHeader(value = "Authorization", required = false) String token,
                                           @RequestBody UpdateProfileRequest req) {
        Optional<Long> userId = tokenService.resolve(token);
        if (userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Please log in first"));
        }
        return userRepository.findById(userId.get())
                .<ResponseEntity<?>>map(user -> {
                    if (req.getBio() != null) user.setBio(req.getBio());
                    if (req.getCertificatePhoto() != null) user.setCertificatePhoto(req.getCertificatePhoto());
                    return ResponseEntity.ok(userRepository.save(user));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found")));
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