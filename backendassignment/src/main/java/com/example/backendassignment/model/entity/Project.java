package com.example.backendassignment.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    private String id;

    /**
     * Stores project data as JSON string
     * Example: {"price":100,"status":"draft"}
     */
    @Column(columnDefinition = "TEXT")
    private String data;

  

    private boolean deleted = false;

    // ===== getters & setters =====

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
