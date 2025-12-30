package com.example.backendassignment.controller;

import com.example.backendassignment.service.ReconstructionService;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/projects")
public class ReconstructionController {

    private final ReconstructionService reconstructionService;

    public ReconstructionController(ReconstructionService reconstructionService) {
        this.reconstructionService = reconstructionService;
    }

    /**
     * Reconstruct project state at a given timestamp
     * Example:
     * GET /projects/P1/state?at=2025-12-30T16:03:00Z
     */
    @GetMapping("/{id}/state")
    public Map<String, Object> getStateAtTime(
            @PathVariable("id") String id,
            @RequestParam("at") String atTime
    ) throws Exception {

        Instant timestamp = Instant.parse(atTime);
        return reconstructionService.reconstructState(id, timestamp);
    }
}
