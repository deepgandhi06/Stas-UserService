package com.ProjectService.pojo;

public class UserSkill {

    private Long id;
    private User user;
    private Skill skill;

    // No-args constructor
    public UserSkill() {
    }

    // All-args constructor
    public UserSkill(Long id, User user, Skill skill) {
        this.id = id;
        this.user = user;
        this.skill = skill;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Skill getSkill() {
        return skill;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    @Override
    public String toString() {
        return "UserSkill{" +
                "id=" + id +
                ", user=" + user +
                ", skill=" + skill +
                '}';
    }
}
