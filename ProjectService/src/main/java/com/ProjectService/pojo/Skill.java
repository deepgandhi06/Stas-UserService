package com.ProjectService.pojo;

public class Skill {

    private Long id;
    private String name;

    // No-args constructor
    public Skill() {
    }

    // All-args constructor
    public Skill(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters
    public Long getId() {
    	return id;
    }
}
