package com.ProjectService.pojo;



import java.time.LocalDateTime;

public class Feedback {
    private Long id;
    private int rating;
    private String content;
    private LocalDateTime createdAt;

    // references by id
    private Long authorId;
    private Long recipientId;
    private Long projectId;
    private Long taskId;

    public Feedback() {}

    public Feedback(Long id, int rating, String content, LocalDateTime createdAt,
                       Long authorId, Long recipientId, Long projectId, Long taskId) {
        this.id = id;
        this.rating = rating;
        this.content = content;
        this.createdAt = createdAt;
        this.authorId = authorId;
        this.recipientId = recipientId;
        this.projectId = projectId;
        this.taskId = taskId;
    }

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
}
