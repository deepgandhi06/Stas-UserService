package com.stas.user_service.DTO;



import java.util.List;

import com.stas.user_service.enums.RoleEnum;



import java.util.List;
import com.stas.user_service.enums.RoleEnum;

public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String password;
    private RoleEnum role;
    private List<UserSkillDTO> userSkills;

    public UserDTO() {}

    public UserDTO(Long id, String name, String email, String password, RoleEnum role, List<UserSkillDTO> userSkills) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.userSkills = userSkills;
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
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public RoleEnum getRole() {
		return role;
	}
	public void setRole(RoleEnum role) {
		this.role = role;
	}
	public List<UserSkillDTO> getUserSkills() {
		return userSkills;
	}
	public void setUserSkills(List<UserSkillDTO> userSkills) {
		this.userSkills = userSkills;
	}
	
	
    
}

