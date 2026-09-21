package com.tuckersoft.branchengine.playthrough;

import com.tuckersoft.branchengine.decision.Decision;
import com.tuckersoft.branchengine.decision.DecisionRepository;
import com.tuckersoft.branchengine.error.ApiException;
import com.tuckersoft.branchengine.node.StoryNode;
import com.tuckersoft.branchengine.node.StoryNodeRepository;
import com.tuckersoft.branchengine.user.User;
import com.tuckersoft.branchengine.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class PlaythroughService {

    private final PlaythroughRepository playthroughs;
    private final StoryNodeRepository nodes;
    private final DecisionRepository decisions;
    private final UserService userService;

    public PlaythroughService(PlaythroughRepository playthroughs, StoryNodeRepository nodes,
                              DecisionRepository decisions, UserService userService) {
        this.playthroughs = playthroughs;
        this.nodes = nodes;
        this.decisions = decisions;
        this.userService = userService;
    }

    @Transactional
    public PlaythroughResponse create(PlaythroughRequest request) {
        User user = userService.currentUser();
        StoryNode node = nodes.findByNodeCode(request.startNodeCode())
                .orElseThrow(() -> ApiException.notFound("Nodo " + request.startNodeCode() + " no existe"));
        if (playthroughs.existsByPlayerTag(request.playerTag())) {
            throw ApiException.conflict("Ya existe una partida con playerTag " + request.playerTag());
        }
        if (node.getCurrentBranches() >= node.getBranchCapacity()) {
            throw ApiException.badRequest("El nodo " + node.getNodeCode() + " esta lleno");
        }
        Instant now = Instant.now();
        Playthrough p = new Playthrough();
        p.setPlayerTag(request.playerTag());
        p.setUser(user);
        p.setCurrentNode(node);
        p.setStartNodeCode(node.getNodeCode());
        p.setLucidity(100);
        p.setControlLevel(0);
        p.setStatus(Playthrough.ACTIVA);
        p.setEndingCode(null);
        p.setCreatedAt(now);
        p.setUpdatedAt(now);

        node.setCurrentBranches(node.getCurrentBranches() + 1);
        nodes.save(node);
        return PlaythroughResponse.from(playthroughs.save(p));
    }

    @Transactional(readOnly = true)
    public List<PlaythroughResponse> list() {
        User user = userService.currentUser();
        List<Playthrough> found = User.ROLE_ADMIN.equals(user.getRole())
                ? playthroughs.findAllByOrderByCreatedAtDescIdDesc()
                : playthroughs.findByUserOrderByCreatedAtDescIdDesc(user);
        return found.stream().map(PlaythroughResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PlaythroughResponse get(Long id) {
        return PlaythroughResponse.from(readable(id));
    }

    @Transactional(readOnly = true)
    public PathResponse path(Long id) {
        Playthrough p = readable(id);
        List<PathResponse.Step> steps = new ArrayList<>();
        int order = 1;
        for (Decision d : decisions
                .findByPlaythroughIdAndResolvedNodeCodeIsNotNullOrderByCreatedAtAscIdAsc(p.getId())) {
            steps.add(new PathResponse.Step(order++, d.getId(), d.getNode().getNodeCode(),
                    d.getResolvedNodeCode(), d.getBranchType(), d.getImpactLevel(), d.getCreatedAt()));
        }
        return new PathResponse(p.getId(), p.getPlayerTag(), p.getStatus(), p.getEndingCode(),
                p.getStartNodeCode(), p.getCurrentNode().getNodeCode(), steps);
    }

    /** Lectura: el dueño o un administrador (supervisor). */
    private Playthrough readable(Long id) {
        User user = userService.currentUser();
        Playthrough p = playthroughs.findById(id)
                .orElseThrow(() -> ApiException.notFound("Partida " + id + " no existe"));
        boolean owner = p.getUser().getId().equals(user.getId());
        if (!owner && !User.ROLE_ADMIN.equals(user.getRole())) {
            throw ApiException.forbidden("La partida no es tuya");
        }
        return p;
    }
}
