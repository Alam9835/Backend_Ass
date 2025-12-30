package com.example.backendassignment.controller;

import com.example.backendassignment.model.entity.AuditEvent;
import com.example.backendassignment.repository.AuditEventRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
public class AuditController {

    private final AuditEventRepository auditEventRepository;

    public AuditController(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    // ================= HISTORY =================
    @GetMapping("/{id}/history")
    public List<AuditEvent> getHistory(@PathVariable("id") String id) {
        return auditEventRepository.findByEntityIdOrderByTimestampAsc(id);
    }

}
