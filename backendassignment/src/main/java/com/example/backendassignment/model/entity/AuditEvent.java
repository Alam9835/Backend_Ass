package com.example.backendassignment.model.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "audit_events")
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Always "project" for this assignment
    private String entityType;

    // Project ID (e.g. P1)
    private String entityId;

    // CREATE / UPDATE / DELETE
    private String action;

    // When the change happened
    private Instant timestamp;

    /**
     * Stores diff as JSON string
     * Example:
     * {
     *   "price": { "old": 100, "new": 120 }
     * }
     */
    @Column(columnDefinition = "TEXT")
    private String diff;


    @PrePersist
    public void onCreate() {
        this.timestamp = Instant.now();
    }

    // ===== getters & setters =====

    public Long getId() {
        return id;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getDiff() {
        return diff;
    }

    public void setDiff(String diff) {
        this.diff = diff;
    }
}
