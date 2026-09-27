package com.skillexchange.repository;

import com.skillexchange.model.SkillSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SessionRepository extends JpaRepository<SkillSession, Long> {

    @Query("SELECT s FROM SkillSession s WHERE s.teacher.id = :userId OR s.learner.id = :userId ORDER BY s.scheduledAt DESC")
    List<SkillSession> findAllForUser(@Param("userId") Long userId);
}
