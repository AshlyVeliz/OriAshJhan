package com.tuckersoft.branchengine.decision;

import com.tuckersoft.branchengine.playthrough.Playthrough;

import java.time.Instant;

public record DecisionResponse(Long id, Long playthroughId, String playerTag, String sourceNodeCode,
                               String resolvedNodeCode, String rawInput, String branchType,
                               String impactLevel, String handlerUnit, String outcomeCode, String status,
                               String playthroughStatus, Integer lucidity, Integer controlLevel,
                               String endingCode, Instant createdAt, Instant updatedAt) {

    public static DecisionResponse from(Decision d) {
        Playthrough p = d.getPlaythrough();
        return new DecisionResponse(d.getId(), p.getId(), p.getPlayerTag(), d.getNode().getNodeCode(),
                d.getResolvedNodeCode(), d.getRawInput(), d.getBranchType(), d.getImpactLevel(),
                d.getHandlerUnit(), d.getOutcomeCode(), d.getStatus(), p.getStatus(), p.getLucidity(),
                p.getControlLevel(), p.getEndingCode(), d.getCreatedAt(), d.getUpdatedAt());
    }
}
