package com.stas.user_service.DTO;

import com.stas.user_service.entity.Skill;
import com.stas.user_service.entity.User;



public class UserSkillDTO {
    private Long id;
    private Long userId;
    private SkillDTO skill;

    public UserSkillDTO() {}
    public UserSkillDTO(Long id, Long userId, SkillDTO skill) {
        this.id = id; this.userId = userId; this.skill = skill;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public SkillDTO getSkill() { return skill; }
    public void setSkill(SkillDTO skill) { this.skill = skill; }
}





