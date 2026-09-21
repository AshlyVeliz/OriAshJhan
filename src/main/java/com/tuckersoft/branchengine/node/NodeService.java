package com.tuckersoft.branchengine.node;

import com.tuckersoft.branchengine.error.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class NodeService {

    private final StoryNodeRepository nodes;

    public NodeService(StoryNodeRepository nodes) {
        this.nodes = nodes;
    }

    @Transactional
    public NodeResponse create(NodeRequest request) {
        if (nodes.existsByNodeCode(request.nodeCode())) {
            throw ApiException.conflict("Ya existe un nodo con codigo " + request.nodeCode());
        }
        StoryNode node = new StoryNode();
        node.setNodeCode(request.nodeCode());
        node.setTitle(request.title());
        node.setSceneText(request.sceneText());
        node.setBranchCapacity(request.branchCapacity());
        node.setCurrentBranches(0);
        node.setPrimaryBranchCode(request.primaryBranchCode());
        node.setGlitchBranchCode(request.glitchBranchCode());
        node.setCreatedAt(Instant.now());
        return NodeResponse.from(nodes.save(node));
    }

    @Transactional(readOnly = true)
    public List<NodeResponse> list() {
        return nodes.findAll().stream().map(NodeResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public NodeResponse get(Long id) {
        return nodes.findById(id).map(NodeResponse::from)
                .orElseThrow(() -> ApiException.notFound("Nodo " + id + " no existe"));
    }
}
