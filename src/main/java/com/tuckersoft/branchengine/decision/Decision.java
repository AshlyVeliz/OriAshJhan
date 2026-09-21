package com.tuckersoft.branchengine.decision;

import com.tuckersoft.branchengine.node.StoryNode;
import com.tuckersoft.branchengine.playthrough.Playthrough;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "decisions")
public class Decision {

    public static final String REGISTRADA = "REGISTRADA";
    public static final String PROCESANDO = "PROCESANDO";
    public static final String ESTABILIZADA = "ESTABILIZADA";
    public static final String ERROR = "ERROR";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "playthrough_id", nullable = false)
    private Playthrough playthrough;

    /** Nodo de origen: el currentNode al momento de decidir. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "node_id", nullable = false)
    private StoryNode node;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String rawInput;

    @Column(nullable = false, length = 30)
    private String branchType;

    @Column(nullable = false, length = 20)
    private String impactLevel;

    @Column(nullable = false)
    private String handlerUnit;

    @Column(nullable = false)
    private String outcomeCode;

    private String resolvedNodeCode;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "decision")
    private List<RealityLog> realityLogs = new ArrayList<>();

    public Long getId() { return id; }
    public Playthrough getPlaythrough() { return playthrough; }
    public void setPlaythrough(Playthrough playthrough) { this.playthrough = playthrough; }
    public StoryNode getNode() { return node; }
    public void setNode(StoryNode node) { this.node = node; }
    public String getRawInput() { return rawInput; }
    public void setRawInput(String rawInput) { this.rawInput = rawInput; }
    public String getBranchType() { return branchType; }
    public void setBranchType(String branchType) { this.branchType = branchType; }
    public String getImpactLevel() { return impactLevel; }
    public void setImpactLevel(String impactLevel) { this.impactLevel = impactLevel; }
    public String getHandlerUnit() { return handlerUnit; }
    public void setHandlerUnit(String handlerUnit) { this.handlerUnit = handlerUnit; }
    public String getOutcomeCode() { return outcomeCode; }
    public void setOutcomeCode(String outcomeCode) { this.outcomeCode = outcomeCode; }
    public String getResolvedNodeCode() { return resolvedNodeCode; }
    public void setResolvedNodeCode(String resolvedNodeCode) { this.resolvedNodeCode = resolvedNodeCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public List<RealityLog> getRealityLogs() { return realityLogs; }
}
