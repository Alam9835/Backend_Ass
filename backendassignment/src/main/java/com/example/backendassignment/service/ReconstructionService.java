package com.example.backendassignment.service;

import com.example.backendassignment.model.entity.AuditEvent;
import com.example.backendassignment.repository.AuditEventRepository;

import tools.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReconstructionService {

    private final AuditEventRepository auditEventRepository;
    private final ObjectMapper objectMapper;

    public ReconstructionService(AuditEventRepository auditEventRepository,
                                 ObjectMapper objectMapper) {
        this.auditEventRepository = auditEventRepository;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> reconstructState(String projectId, Instant atTime)
            throws Exception {

        List<AuditEvent> events =
                auditEventRepository.findByEntityIdOrderByTimestampAsc(projectId);

        Map<String, Object> state = new HashMap<>();

        for (AuditEvent event : events) {

            if (event.getTimestamp().isAfter(atTime)) {
                break;
            }

            // diff is JSON string → convert to Map
            Map<String, Map<String, Object>> diff =
                    objectMapper.readValue(event.getDiff(), Map.class);

            // apply diff
            for (String field : diff.keySet()) {
                state.put(field, diff.get(field).get("new"));
            }
        }

        return state;
    }
}
