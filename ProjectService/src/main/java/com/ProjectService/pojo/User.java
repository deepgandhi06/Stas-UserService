package com.ProjectService.pojo;



import java.util.List;

import com.ProjectService.entity.Project;
import com.ProjectService.entity.ProjectMember;
import com.ProjectService.enums.Role;

public class User {

    private Long id;
    private String name;
    private String email;
    private String password;
    private Role role;
    private List<ProjectMember> projectMemberships;
    private List<UserTask> assignedTasks;
    private List<UserSkill> userSkills;
    private List<Feedback> feedbacksReceived;
    private List<Feedback> feedbacksGiven;
    private List<Project> projects;

    // No-args constructor
    public User() {
    }

    // All-args constructor
    public User(Long id, String name, String email, String password, Role role,
                List<ProjectMember> projectMemberships, List<UserTask> assignedTasks,
                List<UserSkill> userSkills, List<Feedback> feedbacksReceived,
                List<Feedback> feedbacksGiven, List<Project> projects) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.projectMemberships = projectMemberships;
        this.assignedTasks = assignedTasks;
        this.userSkills = userSkills;
        this.feedbacksReceived = feedbacksReceived;
        this.feedbacksGiven = feedbacksGiven;
        this.projects = projects;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public List<ProjectMember> getProjectMemberships() {
        return projectMemberships;
    }

    public void setProjectMemberships(List<ProjectMember> projectMemberships) {
        this.projectMemberships = projectMemberships;
    }

    public List<UserTask> getAssignedTasks() {
        return assignedTasks;
    }

    public void setAssignedTasks(List<UserTask> assignedTasks) {
        this.assignedTasks = assignedTasks;
    }

    public List<UserSkill> getUserSkills() {
        return userSkills;
    }

    public void setUserSkills(List<UserSkill> userSkills) {
        this.userSkills = userSkills;
    }

    public List<Feedback> getFeedbacksReceived() {
        return feedbacksReceived;
    }

    public void setFeedbacksReceived(List<Feedback> feedbacksReceived) {
        this.feedbacksReceived = feedbacksReceived;
    }

    public List<Feedback> getFeedbacksGiven() {
        return feedbacksGiven;
    }

    public void setFeedbacksGiven(List<Feedback> feedbacksGiven) {
        this.feedbacksGiven = feedbacksGiven;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }
}
