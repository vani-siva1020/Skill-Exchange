package com.skillexchange.controller;

import com.skillexchange.dto.BookSessionRequest;
import com.skillexchange.dto.UpdateSessionStatusRequest;
import com.skillexchange.model.SkillSession;
import com.skillexchange.service.SessionService;
import com.skillexchange.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;
    private final TokenService tokenService;

    public SessionController(SessionService sessionService, TokenService tokenService) {
        this.sessionService = sessionService;
        this.tokenService = tokenService;
    }

    // Book a session with a teacher (caller = learner, identified by their token)
    @PostMapping
    public ResponseEntity<?> book(@RequestHeader(value = "Authorization", required = false) String token,
                                   @Valid @RequestBody BookSessionRequest req) {
        Optional<Long> learnerId = tokenService.resolve(token);
        if (learnerId.isEmpty()) {
            return unauthorized();
        }
        try {
            SkillSession session = sessionService.book(learnerId.get(), req);
            return ResponseEntity.status(HttpStatus.CREATED).body(session);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    // All sessions (past and upcoming) where the given user is teacher or learner
    @GetMapping("/user/{userId}")
    public List<SkillSession> getForUser(@PathVariable Long userId) {
        return sessionService.getForUser(userId);
    }

    // Confirm / cancel / mark-completed a session
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@RequestHeader(value = "Authorization", required = false) String token,
                                           @PathVariable Long id,
                                           @Valid @RequestBody UpdateSessionStatusRequest req) {
        Optional<Long> userId = tokenService.resolve(token);
        if (userId.isEmpty()) {
            return unauthorized();
        }
        try {
            SkillSession updated = sessionService.updateStatus(id, userId.get(), req.getStatus());
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
        }
    }

    private ResponseEntity<?> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Please log in first"));
    }
}
