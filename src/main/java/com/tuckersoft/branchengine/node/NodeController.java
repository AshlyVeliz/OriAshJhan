package com.tuckersoft.branchengine.node;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/nodes")
public class NodeController {

    private final NodeService service;

    public NodeController(NodeService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<NodeResponse> create(@Valid @RequestBody NodeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping
    public List<NodeResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public NodeResponse get(@PathVariable Long id) {
        return service.get(id);
    }
}
