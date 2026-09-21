package com.tuckersoft.branchengine.node;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "story_nodes")
public class StoryNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String nodeCode;

    @Column(nullable = false, length = 80)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String sceneText;

    @Column(nullable = false)
    private Integer branchCapacity;

    @Column(nullable = false)
    private Integer currentBranches = 0;

    private String primaryBranchCode;

    private String glitchBranchCode;

    @Column(nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "currentNode")
    private List<com.tuckersoft.branchengine.playthrough.Playthrough> playthroughs = new ArrayList<>();

    @OneToMany(mappedBy = "node")
    private List<com.tuckersoft.branchengine.decision.Decision> decisions = new ArrayList<>();

    public List<com.tuckersoft.branchengine.playthrough.Playthrough> getPlaythroughs() { return playthroughs; }
    public List<com.tuckersoft.branchengine.decision.Decision> getDecisions() { return decisions; }
    public Long getId() { return id; }
    public String getNodeCode() { return nodeCode; }
    public void setNodeCode(String nodeCode) { this.nodeCode = nodeCode; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSceneText() { return sceneText; }
    public void setSceneText(String sceneText) { this.sceneText = sceneText; }
    public Integer getBranchCapacity() { return branchCapacity; }
    public void setBranchCapacity(Integer branchCapacity) { this.branchCapacity = branchCapacity; }
    public Integer getCurrentBranches() { return currentBranches; }
    public void setCurrentBranches(Integer currentBranches) { this.currentBranches = currentBranches; }
    public String getPrimaryBranchCode() { return primaryBranchCode; }
    public void setPrimaryBranchCode(String primaryBranchCode) { this.primaryBranchCode = primaryBranchCode; }
    public String getGlitchBranchCode() { return glitchBranchCode; }
    public void setGlitchBranchCode(String glitchBranchCode) { this.glitchBranchCode = glitchBranchCode; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
