package com.ProjectService.dto;

public class ProjectMemberDTO {

    private Long id;

    // Instead of full Project and User entities, use IDs or names for simplicity
    private Long projectId;
    private String projectTitle; // optional for display purposes

    private Long userId;
    private String userName; // optional for display purposes

    // Constructors
    public ProjectMemberDTO() {}

    public ProjectMemberDTO(Long id, Long projectId, String projectTitle, Long userId, String userName) {
        this.id = id;
        this.projectId = projectId;
        this.projectTitle = projectTitle;
        this.userId = userId;
        this.userName = userName;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

   public void setUserName(String userName1) {
        this.userName = userName;
    }
}
