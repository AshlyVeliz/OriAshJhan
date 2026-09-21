package com.tuckersoft.branchengine.decision;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/decisions")
public class DecisionController {

    private final DecisionService service;

    public DecisionController(DecisionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<DecisionResponse> create(
            @Valid @RequestBody DecisionRequest request,
            @RequestHeader(value = "X-Bandersnatch-Simulate", required = false) String simulate) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, simulate));
    }

    @GetMapping
    public PageResponse<DecisionResponse> list(
            @RequestParam(required = false) String branchType,
            @RequestParam(required = false) String impactLevel,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long playthroughId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.list(branchType, impactLevel, status, playthroughId, page, size);
    }

    @GetMapping("/{id}")
    public DecisionResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping("/{id}/reality-logs")
    public List<RealityLogResponse> realityLogs(@PathVariable Long id) {
        return service.realityLogs(id);
    }
}
