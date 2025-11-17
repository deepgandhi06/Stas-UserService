package com.ProjectService.pojo;

public class UserTask {

    private Long id;
    private User developer;
    private Task task;

    // No-args constructor
    public UserTask() {
    }

    // All-args constructor
    public UserTask(Long id, User developer, Task task) {
        this.id = id;
        this.developer = developer;
        this.task = task;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public User getDeveloper() {
        return developer;
    }

    public Task getTask() {
        return task;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setDeveloper(User developer) {
        this.developer = developer;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    @Override
    public String toString() {
        return "UserTask{" +
                "id=" + id +
                ", developer=" + developer +
                ", task=" + task +
                '}';
    }
}
