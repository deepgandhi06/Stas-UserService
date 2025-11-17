package com.ProjectService.pojo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Task {
    private Long id;
    private String title;
    private String description;
    private String status; // or TaskStatus enum name, use String to keep Feign simple
    private LocalDate dueDate;

    // REPLACED nested objects with IDs
    private Long projectId;
    private Long managerId; // manager user id

    // lists of related IDs (lightweight)
    private List<Long> assignedDeveloperIds = new ArrayList<>();
    private List<Long> requiredSkillIds = new ArrayList<>();

    public Task() {}

    public Task(Long id, String title, String description, String status, LocalDate dueDate,
                Long projectId, Long managerId, List<Long> assignedDeveloperIds, List<Long> requiredSkillIds) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.dueDate = dueDate;
        this.projectId = projectId;
        this.managerId = managerId;
        this.assignedDeveloperIds = assignedDeveloperIds == null ? new ArrayList<>() : assignedDeveloperIds;
        this.requiredSkillIds = requiredSkillIds == null ? new ArrayList<>() : requiredSkillIds;
    }

    // --- getters & setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public Long getManagerId() { return managerId; }
    public void setManagerId(Long managerId) { this.managerId = managerId; }

    public List<Long> getAssignedDeveloperIds() { return assignedDeveloperIds; }
    public void setAssignedDeveloperIds(List<Long> assignedDeveloperIds) {
        this.assignedDeveloperIds = assignedDeveloperIds == null ? new ArrayList<>() : assignedDeveloperIds;
    }

    public List<Long> getRequiredSkillIds() { return requiredSkillIds; }
    public void setRequiredSkillIds(List<Long> requiredSkillIds) {
        this.requiredSkillIds = requiredSkillIds == null ? new ArrayList<>() : requiredSkillIds;
    }
}
