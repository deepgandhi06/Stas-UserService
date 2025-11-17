package com.stas.user_service.DTO;



import java.util.List;

// response DTO for users shown in admin UI (no password)
public class UserManagementDTO {
    private Long id;
    private String name;
    private String email;
    private String role;
    private List<String> skills;

    public UserManagementDTO() {}

    public UserManagementDTO(Long id, String name, String email, String role, List<String> skills) {
        this.id = id; this.name = name; this.email = email; this.role = role; this.skills = skills;
    }

    // getters / setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }
}

