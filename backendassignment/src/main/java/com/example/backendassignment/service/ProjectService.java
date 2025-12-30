package com.example.backendassignment.service;

import com.example.backendassignment.model.entity.Project;
import com.example.backendassignment.model.entity.AuditEvent;
import com.example.backendassignment.repository.ProjectRepository;
import com.example.backendassignment.repository.AuditEventRepository;
import com.example.backendassignment.util.DiffUtil;

import tools.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final AuditEventRepository auditEventRepository;
    private final ObjectMapper objectMapper;

    public ProjectService(ProjectRepository projectRepository,
                          AuditEventRepository auditEventRepository,
                          ObjectMapper objectMapper) {
        this.projectRepository = projectRepository;
        this.auditEventRepository = auditEventRepository;
        this.objectMapper = objectMapper;
    }

    // ================= CREATE =================
    public void createProject(String id, Map<String, Object> data) throws Exception {
    	
    	System.out.println("CREATE called with id = " + id);
    	System.out.println("DATA = " + data);


        // Convert Map → JSON string
        String jsonData = objectMapper.writeValueAsString(data);

        Project project = new Project();
        project.setId(id);
        project.setData(jsonData);

        projectRepository.save(project);

        // Create audit event
        AuditEvent audit = new AuditEvent();
        audit.setEntityType("project");
        audit.setEntityId(id);
        audit.setAction("CREATE");

        Map<String, Object> diff = new HashMap<>();

        for (String key : data.keySet()) {
            Map<String, Object> change = new HashMap<>();
            change.put("old", null);
            change.put("new", data.get(key));

            diff.put(key, change);
        }


        audit.setDiff(objectMapper.writeValueAsString(diff));
        auditEventRepository.save(audit);
    }

    // ================= UPDATE =================
    public void updateProject(String id, Map<String, Object> updates) throws Exception {

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // Convert existing JSON → Map
        Map<String, Object> oldData =
                objectMapper.readValue(project.getData(), Map.class);

        // Merge updates
        Map<String, Object> newData = new HashMap<>(oldData);
        newData.putAll(updates);

        // Calculate diff
        Map<String, Object> diff = DiffUtil.calculateDiff(oldData, newData);

        // Save updated project
        project.setData(objectMapper.writeValueAsString(newData));
        projectRepository.save(project);

        // Save audit event
        AuditEvent audit = new AuditEvent();
        audit.setEntityType("project");
        audit.setEntityId(id);
        audit.setAction("UPDATE");
        audit.setDiff(objectMapper.writeValueAsString(diff));

        auditEventRepository.save(audit);
    }
}
