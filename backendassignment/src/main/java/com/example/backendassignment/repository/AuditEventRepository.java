package com.example.backendassignment.repository;

import com.example.backendassignment.model.entity.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {

    // Get full history of a project
    List<AuditEvent> findByEntityIdOrderByTimestampAsc(String entityId);
}
