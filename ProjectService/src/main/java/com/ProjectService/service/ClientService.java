package com.ProjectService.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ProjectService.dto.ProjectDTO;
import com.ProjectService.entity.Project;
import com.ProjectService.enums.ProjectStatus;
import com.ProjectService.feignClients.UserClient;
import com.ProjectService.pojo.AvailableManager;
import com.ProjectService.pojo.User;
import com.ProjectService.repo.ProjectRepo;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClientService  {

    private final ProjectRepo projectRepo;
    private final UserClient userClient;

    @Autowired
    public ClientService(ProjectRepo projectRepo, UserClient userClient) {
        this.projectRepo = projectRepo;
        this.userClient = userClient;
    }

    /**
     * Controller calls this without arguments. We resolve current user (client) via:
     * 1) userClient.getCurrentUser() (Feign /me) — preferred
     * 2) X-User-Id header
     * 3) X-User-Email header -> userClient.getUserByEmail(...)
     *
     * Returns a ResponseEntity<Object> where body is a Map:
     * {
     *   "stats": { pending:..., active:..., completed:..., overdue:... },
     *   "recentProjects": [ ProjectDTO, ... ]
     * }
     */
    
    public ResponseEntity<Object> getClientDashboardData() {
        Long clientId = resolveCurrentUserId();
        if (clientId == null) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Unable to resolve current user");
        }

        // fetch projects for client
        List<Project> projects = projectRepo.findByClientId(clientId);
        if (projects == null) projects = Collections.emptyList();

        // compute stats
        Map<String, Object> clientDashboardStats = new HashMap<>();
        long pending = projects.stream().filter(p -> p.getStatus() == ProjectStatus.PENDING).count();
        long active = projects.stream().filter(p -> p.getStatus() == ProjectStatus.ONGOING).count();
        long completed = projects.stream().filter(p -> p.getStatus() == ProjectStatus.COMPLETED).count();
        long overdue = projects.stream().filter(p -> p.getStatus() == ProjectStatus.DELAYED).count();

        clientDashboardStats.put("pending", pending);
        clientDashboardStats.put("active", active);
        clientDashboardStats.put("completed", completed);
        clientDashboardStats.put("overdue", overdue);

        // recent projects DTOs (map a few fields)
        List<ProjectDTO> recentProjects = projects.stream()
                .sorted(Comparator.comparing(Project::getCreatedAt).reversed())
                .limit(10)
                .map(p -> {
                    ProjectDTO dto = new ProjectDTO();
                    dto.setId(p.getId());
                    dto.setTitle(p.getTitle());
                    dto.setDescription(p.getDescription());
                    dto.setStartDate(p.getStartDate());
                    dto.setEndDate(p.getEndDate());
                    dto.setStatus(p.getStatus());
                    dto.setCreatedAt(p.getCreatedAt());
                    dto.setUpdatedAt(p.getUpdatedAt());
                    dto.setClientId(p.getClientId());
                    dto.setManagerId(p.getManagerId());
                    // members -> ids
                    if (p.getMembers() != null) {
                        dto.setMemberIds(p.getMembers().stream().map(m -> m.getUserId()).collect(Collectors.toSet()));
                    } else {
                        dto.setMemberIds(new HashSet<>());
                    }
                    // taskIds left empty; task-service can be called later if needed
                    dto.setTaskIds(new HashSet<>());
                    return dto;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("stats", clientDashboardStats);
        response.put("recentProjects", recentProjects);

        return ResponseEntity.ok(response);
    }

    /**
     * Get available managers (from user-service) and attach project counts (from projectRepo).
     * Returns ResponseEntity<List<AvailableManager>>
     */
   
    public ResponseEntity<List<AvailableManager>> getAvailableManagers() {
        // fetch managers from user-service by role
        List<User> managers;
        try {
            managers = userClient.getByRole("MANAGER");
        } catch (Exception ex) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch managers from user service");
        }
        if (managers == null) managers = Collections.emptyList();

        List<AvailableManager> result = managers.stream().map(m -> {
            AvailableManager am = new AvailableManager();
            am.setId(m.getId());
            am.setName(m.getName());
            //am.setEmail(m.getEmail());
            // count projects managed by this manager
            int projectCount = projectRepo.countByManagerId(m.getId());
            am.setProjectCount(projectCount);
            return am;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // -----------------------
    // Helper: resolve current user id
    // -----------------------
    private Long resolveCurrentUserId() {
        // 1) try feign /me
        try {
            User me = userClient.getCurrentUser();
            if (me != null && me.getId() != null) return me.getId();
        } catch (Exception ignored) {}

        // 2) try headers via RequestContextHolder
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest req = attrs.getRequest();
            if (req != null) {
                String idHeader = req.getHeader("X-User-Id");
                if (idHeader != null && !idHeader.isEmpty()) {
                    try { return Long.valueOf(idHeader); } catch (NumberFormatException ignored) {}
                }
                String emailHeader = req.getHeader("X-User-Email");
                if (emailHeader != null && !emailHeader.isEmpty()) {
                    try {
                        User u = userClient.getUserByEmail(emailHeader);
                        if (u != null) return u.getId();
                    } catch (Exception ignored) {}
                }
            }
        }

        return null;
    }
}

