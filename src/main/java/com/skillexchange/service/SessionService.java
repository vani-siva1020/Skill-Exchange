package com.skillexchange.service;

import com.skillexchange.dto.BookSessionRequest;
import com.skillexchange.model.SkillSession;
import com.skillexchange.model.User;
import com.skillexchange.repository.SessionRepository;
import com.skillexchange.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    public SessionService(SessionRepository sessionRepository, UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    public SkillSession book(Long learnerId, BookSessionRequest req) {
        User learner = userRepository.findById(learnerId)
                .orElseThrow(() -> new IllegalArgumentException("Learner not found"));
        User teacher = userRepository.findById(req.getTeacherId())
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));

        if (learner.getId().equals(teacher.getId())) {
            throw new IllegalArgumentException("You cannot book a session with yourself");
        }

        SkillSession session = new SkillSession();
        session.setLearner(learner);
        session.setTeacher(teacher);
        session.setSkill(req.getSkill());
        session.setNotes(req.getNotes());
        session.setScheduledAt(req.getScheduledAt());
        session.setStatus(SkillSession.Status.PENDING);
        return sessionRepository.save(session);
    }

    public List<SkillSession> getForUser(Long userId) {
        return sessionRepository.findAllForUser(userId);
    }

    public SkillSession updateStatus(Long sessionId, Long requesterId, SkillSession.Status newStatus) {
        SkillSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        boolean isParticipant = session.getTeacher().getId().equals(requesterId)
                || session.getLearner().getId().equals(requesterId);
        if (!isParticipant) {
            throw new SecurityException("Only the teacher or learner in this session can update it");
        }

        // Only the teacher confirms a booking; either side can cancel; either side can mark completed
        if (newStatus == SkillSession.Status.CONFIRMED && !session.getTeacher().getId().equals(requesterId)) {
            throw new SecurityException("Only the teacher can confirm a session");
        }

        session.setStatus(newStatus);
        return sessionRepository.save(session);
    }
}
