package com.stas.user_service.service;


import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stas.user_service.DTO.SkillDTO;
import com.stas.user_service.DTO.UserDTO;
import com.stas.user_service.DTO.UserSkillDTO;
import com.stas.user_service.entity.Skill;
import com.stas.user_service.entity.User;
import com.stas.user_service.entity.UserSkill;
import com.stas.user_service.repository.SkillRepository;
import com.stas.user_service.repository.UserRepository;

import java.util.Optional;
import java.util.stream.Collectors;



@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;

    public AuthService(UserRepository userRepository,
                       SkillRepository skillRepository) {
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
    }

    // ------------------------------------------
    // SIGNUP
    // ------------------------------------------
    public UserDTO signup(UserDTO dto) {
        if (dto == null) throw new RuntimeException("UserDTO is required");

        // email already exists?
        userRepository.findByEmail(dto.getEmail()).ifPresent(u -> {
            throw new RuntimeException("Email already registered");
        });

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());  // NO ENCODING
        user.setRole(dto.getRole());

        // set skills if any
        if (dto.getUserSkills() != null) {
            dto.getUserSkills().forEach(usDto -> {
                Skill skill = resolveSkillFromDto(usDto);   // <-- robust resolver
                UserSkill userSkill = new UserSkill(user, skill);
                user.getUserSkills().add(userSkill);
            });
        }

        return toDTO(userRepository.save(user));
    }

    // ------------------------------------------
    // LOGIN
    // ------------------------------------------
    public UserDTO login(UserDTO dto) {
        if (dto == null) throw new RuntimeException("UserDTO is required");

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // since there's no password encoding, direct comparison
        if (!user.getPassword().equals(dto.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        return toDTO(user);
    }

    // ------------------------------------------
    // UPDATE
    // ------------------------------------------
    public UserDTO update(UserDTO dto) {
        if (dto == null || dto.getId() == null)
            throw new RuntimeException("User id required");

        User user = userRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (dto.getName() != null) user.setName(dto.getName());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getRole() != null) user.setRole(dto.getRole());

        // update skills if provided
        if (dto.getUserSkills() != null) {
            user.getUserSkills().clear();
            dto.getUserSkills().forEach(usDto -> {
                Skill skill = resolveSkillFromDto(usDto);
                UserSkill userSkill = new UserSkill(user, skill);
                user.getUserSkills().add(userSkill);
            });
        }

        // update password also here if provided
        if (dto.getPassword() != null) {
            user.setPassword(dto.getPassword());
        }

        return toDTO(userRepository.save(user));
    }

    // ------------------------------------------
    // CHANGE PASSWORD
    // ------------------------------------------
    public void changePassword(UserDTO dto) {
        if (dto == null || dto.getId() == null)
            throw new RuntimeException("User id required");

        User user = userRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (dto.getPassword() == null)
            throw new RuntimeException("New password required");

        // direct password set → NO security
        user.setPassword(dto.getPassword());

        userRepository.save(user);
    }

    // ----------------------------------------------------
    // Robust skill resolver: uses id if provided, otherwise find-by-name or create safely
    // ----------------------------------------------------
    private Skill resolveSkillFromDto(UserSkillDTO usDto) {
        if (usDto == null || usDto.getSkill() == null) {
            throw new RuntimeException("UserSkillDTO.skill is required");
        }

        // 1) If skill id provided -> load by id
        if (usDto.getSkill().getId() != null) {
            return skillRepository.findById(usDto.getSkill().getId())
                    .orElseThrow(() -> new RuntimeException("Skill not found: id=" + usDto.getSkill().getId()));
        }

        // 2) Otherwise use name (find-or-create)
        String name = Optional.ofNullable(usDto.getSkill().getName())
                .map(String::trim)
                .orElseThrow(() -> new RuntimeException("Skill name required"));

        // try find by name
        Optional<Skill> existing = skillRepository.findByName(name);
        if (existing.isPresent()) return existing.get();

        // not found -> attempt create
        Skill newSkill = new Skill();
        newSkill.setName(name);

        try {
            return skillRepository.save(newSkill);
        } catch (DataIntegrityViolationException ex) {
            // concurrent insert happened -> someone else created the same skill
            return skillRepository.findByName(name)
                    .orElseThrow(() -> new RuntimeException("Skill exists but could not be loaded: " + name));
        }
    }

    // ----------------------------------------------------
    // DTO mapping (same as yours)
    // ----------------------------------------------------
    private UserDTO toDTO(User u) {
        if (u == null) return null;

        UserDTO dto = new UserDTO();
        dto.setId(u.getId());
        dto.setName(u.getName());
        dto.setEmail(u.getEmail());
        dto.setPassword(u.getPassword());  // remove later for security
        dto.setRole(u.getRole());

        if (u.getUserSkills() != null) {
            dto.setUserSkills(
                u.getUserSkills().stream().map(us -> {
                    Skill s = us.getSkill();
                    SkillDTO skillDto = new SkillDTO(s.getId(), s.getName());

                    UserSkillDTO usDto = new UserSkillDTO();
                    usDto.setId(us.getId());
                    usDto.setUserId(u.getId());    // avoid embedding whole User
                    usDto.setSkill(skillDto);
                    return usDto;
                }).collect(Collectors.toList())
            );
        }

        return dto;
    }

}
