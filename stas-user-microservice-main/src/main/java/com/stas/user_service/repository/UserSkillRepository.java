package com.stas.user_service.repository;

import com.stas.user_service.entity.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {

    // Find all skills for a user
    List<UserSkill> findByUserId(Long userId);

    // Find all user-skill mappings for a skill
    List<UserSkill> findBySkillId(Long skillId);

    // --- Option A: simple derived delete (one-liner) ---
    // Generates: DELETE FROM user_skills WHERE skill_id = ?
    void deleteBySkillId(Long skillId);

    // --- Option B: explicit bulk delete returning count (alternative) ---
    // Uncomment & use if you want the deleted-row count and prefer JPQL.
    // Note: requires @Modifying and transactional context.
    @Modifying
    @Transactional
    @Query("DELETE FROM UserSkill us WHERE us.skillId = :skillId")
    int deleteAllBySkillId(@Param("skillId") Long skillId);
}
