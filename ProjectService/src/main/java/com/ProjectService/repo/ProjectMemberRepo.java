package com.ProjectService.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ProjectService.entity.ProjectMember;

public interface ProjectMemberRepo extends JpaRepository<ProjectMember, Long> {
	

}
