package com.tuckersoft.branchengine.decision;

import java.time.Instant;

public record RealityLogResponse(Long id, Long decisionId, String recipientEmail, String subject,
                                 String logStatus, String errorMessage, Instant sentAt, Instant createdAt) {

    public static RealityLogResponse from(RealityLog l) {
        return new RealityLogResponse(l.getId(), l.getDecision().getId(), l.getRecipientEmail(),
                l.getSubject(), l.getLogStatus(), l.getErrorMessage(), l.getSentAt(), l.getCreatedAt());
    }
}
