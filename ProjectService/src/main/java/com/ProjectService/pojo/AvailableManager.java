package com.ProjectService.pojo;


public class AvailableManager {
	private Long id;
	private String name;
	private int projectCount;

	public AvailableManager(User manager) {
		this.id = manager.getId();
		this.name = manager.getName();
	}

	public AvailableManager() {
		// TODO Auto-generated constructor stub
	}

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

	public int getProjectCount() {
		return projectCount;
	}

	public void setProjectCount(int projectCount) {
		this.projectCount = projectCount;
	}

	
	
	
}
