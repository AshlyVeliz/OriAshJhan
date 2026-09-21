package com.tuckersoft.branchengine.playthrough;

import com.tuckersoft.branchengine.decision.Decision;
import com.tuckersoft.branchengine.node.StoryNode;
import com.tuckersoft.branchengine.user.User;
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
@Table(name = "playthroughs")
public class Playthrough {

    public static final String ACTIVA = "ACTIVA";
    public static final String FINALIZADA = "FINALIZADA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String playerTag;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String startNodeCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "current_node_id", nullable = false)
    private StoryNode currentNode;

    @Column(nullable = false)
    private Integer lucidity = 100;

    @Column(nullable = false)
    private Integer controlLevel = 0;

    @Column(nullable = false, length = 20)
    private String status = ACTIVA;

    private String endingCode;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "playthrough")
    private List<Decision> decisions = new ArrayList<>();

    public Long getId() { return id; }
    public String getPlayerTag() { return playerTag; }
    public void setPlayerTag(String playerTag) { this.playerTag = playerTag; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getStartNodeCode() { return startNodeCode; }
    public void setStartNodeCode(String startNodeCode) { this.startNodeCode = startNodeCode; }
    public StoryNode getCurrentNode() { return currentNode; }
    public void setCurrentNode(StoryNode currentNode) { this.currentNode = currentNode; }
    public Integer getLucidity() { return lucidity; }
    public void setLucidity(Integer lucidity) { this.lucidity = lucidity; }
    public Integer getControlLevel() { return controlLevel; }
    public void setControlLevel(Integer controlLevel) { this.controlLevel = controlLevel; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getEndingCode() { return endingCode; }
    public void setEndingCode(String endingCode) { this.endingCode = endingCode; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public List<Decision> getDecisions() { return decisions; }
}
