package com.example.backendassignment.controller;

import com.example.backendassignment.service.ProjectService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    // ================= CREATE =================
    @PostMapping("/{id}")
    public String createProject(@PathVariable("id") String id,
                                @RequestBody Map<String, Object> data) throws Exception {

        projectService.createProject(id, data);
        return "Project created";
    }

    // ================= UPDATE =================
    @PatchMapping("/{id}")
    public String updateProject(@PathVariable("id") String id,
                                @RequestBody Map<String, Object> updates) throws Exception {

        projectService.updateProject(id, updates);
        return "Project updated";
    }
}
