package com.tuckersoft.branchengine.decision;

import com.tuckersoft.branchengine.error.ApiException;
import com.tuckersoft.branchengine.node.StoryNode;
import com.tuckersoft.branchengine.node.StoryNodeRepository;
import com.tuckersoft.branchengine.playthrough.Playthrough;
import com.tuckersoft.branchengine.playthrough.PlaythroughRepository;
import com.tuckersoft.branchengine.user.User;
import com.tuckersoft.branchengine.user.UserService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DecisionService {

    static final String ENDING_PAC_SYMBOL = "ENDING_PAC_SYMBOL";
    static final String ENDING_WHITE_BEAR = "ENDING_WHITE_BEAR";
    static final String ENDING_NETFLIX_CUT = "ENDING_NETFLIX_CUT";

    private final DecisionRepository decisions;
    private final RealityLogRepository logs;
    private final PlaythroughRepository playthroughs;
    private final StoryNodeRepository nodes;
    private final UserService userService;
    private final ApplicationEventPublisher events;

    public DecisionService(DecisionRepository decisions, RealityLogRepository logs,
                           PlaythroughRepository playthroughs, StoryNodeRepository nodes,
                           UserService userService, ApplicationEventPublisher events) {
        this.decisions = decisions;
        this.logs = logs;
        this.playthroughs = playthroughs;
        this.nodes = nodes;
        this.userService = userService;
        this.events = events;
    }

    @Transactional
    public DecisionResponse create(DecisionRequest request, String simulate) {
        // 1. Usuario del token y propiedad de la partida (escritura: tambien para el admin)
        User user = userService.currentUser();
        Playthrough p = playthroughs.findById(request.playthroughId())
                .orElseThrow(() -> ApiException.notFound("Partida " + request.playthroughId() + " no existe"));
        if (!p.getUser().getId().equals(user.getId())) {
            throw ApiException.forbidden("La partida no es tuya");
        }
        // 2. La partida debe estar ACTIVA
        if (!Playthrough.ACTIVA.equals(p.getStatus())) {
            throw ApiException.conflict("La partida ya esta FINALIZADA");
        }
        // 3. Clasificar y derivar unidad y consecuencia
        String branchType = BranchClassifier.classify(request.rawInput());
        Instant now = Instant.now();

        Decision d = new Decision();
        d.setPlaythrough(p);
        d.setNode(p.getCurrentNode());
        d.setRawInput(request.rawInput());
        d.setBranchType(branchType);
        d.setImpactLevel(request.impactLevel());
        d.setHandlerUnit(BranchClassifier.handlerUnit(branchType));
        d.setOutcomeCode(BranchClassifier.outcomeCode(branchType));
        d.setCreatedAt(now);
        d.setUpdatedAt(now);

        // 4. Entrada corrupta: se guarda con ERROR, sin tocar la partida ni publicar evento
        if (BranchClassifier.ENTRADA_CORRUPTA.equals(branchType)) {
            d.setResolvedNodeCode(null);
            d.setStatus(Decision.ERROR);
            return DecisionResponse.from(decisions.save(d));
        }

        // 5. Stats, nodo destino y estado de la partida
        applyStats(p, request.impactLevel());
        String target = resolveTarget(p.getCurrentNode(), branchType, request.impactLevel());
        d.setResolvedNodeCode(target);
        resolveState(p, target);
        p.setUpdatedAt(now);

        // 6 y 7. Guardar partida y decision
        playthroughs.save(p);
        d.setStatus(Decision.REGISTRADA);
        Decision saved = decisions.save(d);
        DecisionResponse response = DecisionResponse.from(saved);

        // 8. Publicar el evento: el listener corre despues del COMMIT
        events.publishEvent(new DecisionCommittedEvent(saved.getId(), p.getUser().getEmail(),
                p.getUser().getDisplayName(), p.getPlayerTag(), saved.getBranchType(),
                saved.getImpactLevel(), saved.getHandlerUnit(), saved.getOutcomeCode(),
                saved.getNode().getNodeCode(), saved.getResolvedNodeCode(), p.getStatus(),
                p.getLucidity(), p.getControlLevel(), p.getEndingCode(), saved.getRawInput(),
                saved.getCreatedAt(), simulate));
        return response;
    }

    @Transactional(readOnly = true)
    public PageResponse<DecisionResponse> list(String branchType, String impactLevel, String status,
                                               Long playthroughId, int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw ApiException.badRequest("page debe ser >= 0 y size entre 1 y 100");
        }
        User user = userService.currentUser();
        boolean admin = User.ROLE_ADMIN.equals(user.getRole());

        Specification<Decision> spec = (root, query, cb) -> {
            List<Predicate> filters = new ArrayList<>();
            if (!admin) {
                filters.add(cb.equal(root.get("playthrough").get("user").get("id"), user.getId()));
            }
            if (branchType != null && !branchType.isBlank()) {
                filters.add(cb.equal(root.get("branchType"), branchType));
            }
            if (impactLevel != null && !impactLevel.isBlank()) {
                filters.add(cb.equal(root.get("impactLevel"), impactLevel));
            }
            if (status != null && !status.isBlank()) {
                filters.add(cb.equal(root.get("status"), status));
            }
            if (playthroughId != null) {
                filters.add(cb.equal(root.get("playthrough").get("id"), playthroughId));
            }
            return cb.and(filters.toArray(new Predicate[0]));
        };
        Page<Decision> result = decisions.findAll(spec,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id")));
        return PageResponse.of(result, DecisionResponse::from);
    }

    @Transactional(readOnly = true)
    public DecisionResponse get(Long id) {
        return DecisionResponse.from(readable(id));
    }

    @Transactional(readOnly = true)
    public List<RealityLogResponse> realityLogs(Long id) {
        Decision d = readable(id);
        return logs.findByDecisionIdOrderByCreatedAtAscIdAsc(d.getId()).stream()
                .map(RealityLogResponse::from).toList();
    }

    /** Lectura: el dueño o un administrador (supervisor). */
    private Decision readable(Long id) {
        User user = userService.currentUser();
        Decision d = decisions.findById(id)
                .orElseThrow(() -> ApiException.notFound("Decision " + id + " no existe"));
        boolean owner = d.getPlaythrough().getUser().getId().equals(user.getId());
        if (!owner && !User.ROLE_ADMIN.equals(user.getRole())) {
            throw ApiException.forbidden("La decision no es tuya");
        }
        return d;
    }

    private void applyStats(Playthrough p, String impact) {
        int lucidity;
        int control;
        switch (impact) {
            case "LEVE" -> { lucidity = -5; control = 5; }
            case "MODERADO" -> { lucidity = -15; control = 10; }
            case "GRAVE" -> { lucidity = -30; control = 20; }
            default -> { lucidity = -40; control = 45; } // CRITICO
        }
        p.setLucidity(Math.max(0, Math.min(100, p.getLucidity() + lucidity)));
        p.setControlLevel(Math.max(0, Math.min(100, p.getControlLevel() + control)));
    }

    private String resolveTarget(StoryNode origin, String branchType, String impact) {
        boolean glitch = BranchClassifier.RUPTURA_CUARTA_PARED.equals(branchType) || "CRITICO".equals(impact);
        return glitch ? origin.getGlitchBranchCode() : origin.getPrimaryBranchCode();
    }

    /** Orden exacto del enunciado: control, lucidez, destino inexistente, continuar. */
    private void resolveState(Playthrough p, String target) {
        if (p.getControlLevel() >= 100) {
            finish(p, ENDING_PAC_SYMBOL);
        } else if (p.getLucidity() <= 0) {
            finish(p, ENDING_WHITE_BEAR);
        } else {
            Optional<StoryNode> next = target == null ? Optional.empty() : nodes.findByNodeCode(target);
            if (next.isEmpty()) {
                finish(p, ENDING_NETFLIX_CUT);
            } else {
                p.setStatus(Playthrough.ACTIVA);
                p.setCurrentNode(next.get());
            }
        }
    }

    private void finish(Playthrough p, String ending) {
        p.setStatus(Playthrough.FINALIZADA);
        p.setEndingCode(ending);
    }
}
