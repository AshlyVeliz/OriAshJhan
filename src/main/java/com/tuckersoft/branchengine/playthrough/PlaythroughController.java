package com.tuckersoft.branchengine.playthrough;

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
@RequestMapping("/api/v1/playthroughs")
public class PlaythroughController {

    private final PlaythroughService service;

    public PlaythroughController(PlaythroughService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PlaythroughResponse> create(@Valid @RequestBody PlaythroughRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping
    public List<PlaythroughResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public PlaythroughResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping("/{id}/path")
    public PathResponse path(@PathVariable Long id) {
        return service.path(id);
    }
}
