package com.tuckersoft.branchengine.node;

import java.time.Instant;

public record NodeResponse(Long id, String nodeCode, String title, String sceneText, Integer branchCapacity,
                           Integer currentBranches, String primaryBranchCode, String glitchBranchCode,
                           Instant createdAt) {

    public static NodeResponse from(StoryNode n) {
        return new NodeResponse(n.getId(), n.getNodeCode(), n.getTitle(), n.getSceneText(),
                n.getBranchCapacity(), n.getCurrentBranches(), n.getPrimaryBranchCode(),
                n.getGlitchBranchCode(), n.getCreatedAt());
    }
}
