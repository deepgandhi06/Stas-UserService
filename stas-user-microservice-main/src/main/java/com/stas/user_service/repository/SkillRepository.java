package com.stas.user_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stas.user_service.entity.Skill;

public interface SkillRepository extends JpaRepository<Skill , Long> {
	Optional<Skill> findByName(String name);
	Boolean existsByName(String name);
	Optional<Skill> findByNameIgnoreCase(String name);
}
