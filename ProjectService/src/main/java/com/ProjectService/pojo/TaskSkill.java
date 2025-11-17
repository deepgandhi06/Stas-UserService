package com.ProjectService.pojo;

public class TaskSkill {

    private Long id;
    private Task task;
    private Skill skill;

    // No-args constructor
    public TaskSkill() {
    }

    // All-args constructor
    public TaskSkill(Long id, Task task, Skill skill) {
        this.id = id;
        this.task = task;
        this.skill = skill;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Task getTask() {
        return task;
    }

    public Skill getSkill() {
        return skill;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    @Override
    public String toString() {
        return "TaskSkill{" +
                "id=" + id +
                ", task=" + task +
                ", skill=" + skill +
                '}';
    }
}
