package com.stas.user_service.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.stas.user_service.DTO.CreateOrUpdateUserRequest;
import com.stas.user_service.DTO.SkillRequest;
import com.stas.user_service.DTO.UserManagementDTO;
import com.stas.user_service.entity.Skill;
import com.stas.user_service.entity.User;
import com.stas.user_service.entity.UserSkill;
import com.stas.user_service.enums.RoleEnum;
import com.stas.user_service.repository.SkillRepository;
import com.stas.user_service.repository.UserRepository;
import com.stas.user_service.repository.UserSkillRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminService {

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;

    @Autowired
    public AdminService(UserRepository userRepository,
                        SkillRepository skillRepository,
                        UserSkillRepository userSkillRepository) {
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.userSkillRepository = userSkillRepository;
    }

    // 1: Admin dashboard data
    public ResponseEntity<Map<String, Object>> getAdminDashboardData() {
        long totalUsers = userRepository.count();
        long totalSkills = skillRepository.count();
        long developers = userRepository.countByRole(RoleEnum.DEVELOPER);
        long managers   = userRepository.countByRole(RoleEnum.MANAGER);
        long clients    = userRepository.countByRole(RoleEnum.CLIENT);


        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("totalSkills", totalSkills);
        stats.put("countsByRole", Map.of("DEVELOPER", developers, "MANAGER", managers, "CLIENT", clients));

        // recent users (last 10 by id desc as example)
        Pageable p = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));
        Page<User> recent = userRepository.findAll(p);
        List<UserManagementDTO> recentDtos = recent.getContent().stream()
                .map(this::toManagementDto)
                .collect(Collectors.toList());
        stats.put("recentUsers", recentDtos);

        return ResponseEntity.ok(stats);
    }

    // 2: paged list for management
    public ResponseEntity<Page<UserManagementDTO>> getAllUsersForManagement(int page, int limit, String roleFilter) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, limit), Sort.by(Sort.Direction.DESC, "id"));
        Page<User> pageResult;
        if (roleFilter == null || roleFilter.isBlank()) {
            pageResult = userRepository.findAll(pageable);
        } else {
            RoleEnum roleEnum;
            try {
                roleEnum = RoleEnum.valueOf(roleFilter.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role filter");
            }
            pageResult = new PageImpl<>(userRepository.findByRole(roleEnum), pageable, userRepository.findByRole(roleEnum).size());
            // note: if user count large, prefer repository pageable method (we added earlier)
        }
        Page<UserManagementDTO> dtoPage = pageResult.map(this::toManagementDto);
        return ResponseEntity.ok(dtoPage);
    }

    // 3: create admin (creates a user with ADMIN role)
    public ResponseEntity<UserManagementDTO> createAdmin(CreateOrUpdateUserRequest req) {
        if (req.getEmail() == null || req.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email required");
        }
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }
        User u = new User();
        u.setName(req.getName());
        u.setEmail(req.getEmail());
        // TODO: hash password in real app
        u.setPassword(req.getPassword() == null ? "" : req.getPassword());
        u.setRole(RoleEnum.ADMIN);
        User saved = userRepository.save(u);
        return ResponseEntity.status(HttpStatus.CREATED).body(toManagementDto(saved));
    }

    // 4: update user
    public ResponseEntity<UserManagementDTO> updateUser(Long userId, CreateOrUpdateUserRequest req) {
        User u = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (req.getName() != null) u.setName(req.getName());
        if (req.getEmail() != null && !req.getEmail().equals(u.getEmail())) {
            if (userRepository.findByEmail(req.getEmail()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
            }
            u.setEmail(req.getEmail());
        }
        if (req.getPassword() != null) {
            // TODO: hash password
            u.setPassword(req.getPassword());
        }
        if (req.getRole() != null) {
            try {
                RoleEnum roleEnum = RoleEnum.valueOf(req.getRole().toUpperCase());
                u.setRole(roleEnum);
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role");
            }
        }
        User saved = userRepository.save(u);
        return ResponseEntity.ok(toManagementDto(saved));
    }

    // 5: delete user (also cascade user-skills via orphanRemoval or manually)
    public ResponseEntity<Void> deleteUser(Long userId) {
        User u = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        // remove userSkills explicitly if not cascaded
        List<UserSkill> us = userSkillRepository.findByUserId(userId);
        if (us != null && !us.isEmpty()) {
            userSkillRepository.deleteAll(us);
        }
        userRepository.delete(u);
        return ResponseEntity.noContent().build();
    }

    // 6: get all skills
    public ResponseEntity<List<String>> getAllSkills() {
        List<Skill> skills = skillRepository.findAll();
        List<String> names = skills.stream().map(Skill::getName).collect(Collectors.toList());
        return ResponseEntity.ok(names);
    }

    // 7: add skill
    public ResponseEntity<String> addSkill(SkillRequest req) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Skill name required");
        }
        Optional<Skill> exist = skillRepository.findByNameIgnoreCase(req.getName());
        if (exist.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Skill already exists");
        }
        Skill s = new Skill(req.getName().trim());
        skillRepository.save(s);
        return ResponseEntity.status(HttpStatus.CREATED).body("Skill created");
    }

 // 8: delete skill by id
    public ResponseEntity<Void> deleteSkill(Long skillId) {
        // ensure skill exists
        if (!skillRepository.existsById(skillId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill not found");
        }

        // remove any user-skill mappings (bulk delete)
        userSkillRepository.deleteBySkillId(skillId);
        // alternatively, if you want row count:
        // int removed = userSkillRepository.deleteAllBySkillId(skillId);

        // delete the skill itself
        skillRepository.deleteById(skillId);

        return ResponseEntity.noContent().build();
    }


    // helper mapping
    private UserManagementDTO toManagementDto(User u) {
        List<String> skills = userSkillRepository.findByUserId(u.getId()).stream()
                .map(us -> us.getSkill().getName())
                .collect(Collectors.toList());
        return new UserManagementDTO(u.getId(), u.getName(), u.getEmail(), u.getRole() == null ? null : u.getRole().name(), skills);
    }
}

