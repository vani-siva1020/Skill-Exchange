package com.skillexchange.dto;

import com.skillexchange.model.SkillSession;
import jakarta.validation.constraints.NotNull;

public class UpdateSessionStatusRequest {

    @NotNull(message = "status is required")
    private SkillSession.Status status;

    public SkillSession.Status getStatus() { return status; }
    public void setStatus(SkillSession.Status status) { this.status = status; }
}
