package com.ProjectService.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ProjectService.dto.ProjectDTO;
import com.ProjectService.entity.Project;
import com.ProjectService.entity.ProjectMember;
import com.ProjectService.enums.ProjectStatus;
import com.ProjectService.feignClients.TaskClient;
import com.ProjectService.feignClients.UserClient;
import com.ProjectService.pojo.Task;
import com.ProjectService.pojo.User;
import com.ProjectService.repo.ProjectMemberRepo;
import com.ProjectService.repo.ProjectRepo;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepo projectRepository;
    private final ProjectMemberRepo projectMemberRepository;
    private final UserClient userClient;
    private final TaskClient taskClient;
    private final HttpServletRequest httpRequest; // optional, may be null in tests

    @Autowired
    public ProjectService(ProjectRepo projectRepository,
                          ProjectMemberRepo projectMemberRepository,
                          UserClient userClient,
                          TaskClient taskClient,
                          HttpServletRequest httpRequest) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userClient = userClient;
        this.taskClient = taskClient;
        this.httpRequest = httpRequest;
    }

    // default ctor for frameworks/tests (kept but not used by Spring autowire)
    public ProjectService() {
        this.projectRepository = null;
        this.projectMemberRepository = null;
        this.userClient = null;
        this.taskClient = null;
        this.httpRequest = null;
    }

    // ---------------------------
    // Public service methods
    // ---------------------------

    /**
     * Create a new project. Returns created ProjectDTO (caller/controller can wrap in ResponseEntity).
     */
    public ProjectDTO createNewProject(ProjectDTO newProject) {
        Long clientId = resolveCurrentUserIdOrFallback(newProject);
        if (clientId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Unable to determine client identity. Ensure API Gateway forwards 'X-User-Id' or 'X-User-Email' header, or include clientId in request body.");
        }

        // Validate optional managerId (exists in user service)
        Long managerId = null;
        if (newProject.getManagerId() != null) {
            try {
                User manager = userClient.getUserById(newProject.getManagerId());
                if (manager == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Manager id is invalid: " + newProject.getManagerId());
                }
                managerId = manager.getId();
            } catch (Exception ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Manager id is invalid: " + newProject.getManagerId());
            }
        }

        Project p = new Project();
        p.setTitle(newProject.getTitle());
        p.setDescription(newProject.getDescription());
        p.setStartDate(newProject.getStartDate());
        p.setEndDate(newProject.getEndDate());
        p.setStatus(ProjectStatus.PENDING);
        p.setClientId(clientId);
        p.setManagerId(managerId);

        Project saved = projectRepository.save(p);

        // Build DTO directly and return
        ProjectDTO dto = new ProjectDTO(
                saved.getId(),
                saved.getTitle(),
                saved.getDescription(),
                saved.getStartDate(),
                saved.getEndDate(),
                saved.getStatus(),
                saved.getCreatedAt(),
                saved.getUpdatedAt(),
                saved.getClientId(),
                null, // clientName - optional enrichment below
                saved.getManagerId(),
                null, // managerName - optional enrichment below
                saved.getMembers() == null ? new HashSet<>() : saved.getMembers().stream().map(ProjectMember::getUserId).collect(Collectors.toSet()),
                Collections.emptySet() // taskIds can be fetched from task-service when needed
        );

        // Optional: enrich clientName & managerName using userClient (best-effort, do not fail if user service unavailable)
        try {
            User clientUser = userClient.getUserById(saved.getClientId());
            if (clientUser != null) dto.setClientName(clientUser.getName());
        } catch (Exception ignored) {}
        if (dto.getManagerId() != null) {
            try {
                User managerUser = userClient.getUserById(dto.getManagerId());
                if (managerUser != null) dto.setManagerName(managerUser.getName());
            } catch (Exception ignored) {}
        }

        return dto;
    }

    /**
     * Update project by client (owner). Ensures the caller is owner via user service /me or header.
     */
    public ProjectDTO clientUpdateProject(Long projectId, ProjectDTO newProject) {
        Project p = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found: " + projectId));

        Long callerId = resolveCurrentUserIdOrFallback(newProject);
        if (callerId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unable to determine authenticated user");
        }

        // ensure current user is the client (owner) of the project
        if (!Objects.equals(p.getClientId(), callerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this project");
        }

        // perform updates (only non-null fields)
        if (newProject.getTitle() != null) p.setTitle(newProject.getTitle());
        if (newProject.getDescription() != null) p.setDescription(newProject.getDescription());
        if (newProject.getStartDate() != null) p.setStartDate(newProject.getStartDate());
        if (newProject.getEndDate() != null) p.setEndDate(newProject.getEndDate());

        if (newProject.getManagerId() != null) {
            try {
                User manager = userClient.getUserById(newProject.getManagerId());
                if (manager == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid manager id: " + newProject.getManagerId());
                }
                p.setManagerId(manager.getId());
            } catch (Exception ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid manager id: " + newProject.getManagerId());
            }
        }

        Project saved = projectRepository.save(p);

        // return DTO (inline)
        ProjectDTO dto = new ProjectDTO(
                saved.getId(),
                saved.getTitle(),
                saved.getDescription(),
                saved.getStartDate(),
                saved.getEndDate(),
                saved.getStatus(),
                saved.getCreatedAt(),
                saved.getUpdatedAt(),
                saved.getClientId(),
                null,
                saved.getManagerId(),
                null,
                saved.getMembers() == null ? new HashSet<>() : saved.getMembers().stream().map(ProjectMember::getUserId).collect(Collectors.toSet()),
                Collections.emptySet()
        );

        // optional enrichment
        try {
            User clientUser = userClient.getUserById(dto.getClientId());
            if (clientUser != null) dto.setClientName(clientUser.getName());
        } catch (Exception ignored) {}
        if (dto.getManagerId() != null) {
            try {
                User managerUser = userClient.getUserById(dto.getManagerId());
                if (managerUser != null) dto.setManagerName(managerUser.getName());
            } catch (Exception ignored) {}
        }
        return dto;
    }

    
    //Completed : 
    @Transactional(readOnly = true)
    public Page<ProjectDTO> findProjectsForClient(int page, int limit) {
        // resolve user id from gateway/feign/header
        Long clientId = resolveCurrentUserIdOrFallback(null);
        if (clientId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found");

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, limit), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Project> projectPage = projectRepository.findByClientId(clientId, pageable);

        List<ProjectDTO> content = projectPage.getContent().stream().map(p -> {
            ProjectDTO dto = new ProjectDTO(
                    p.getId(),
                    p.getTitle(),
                    p.getDescription(),
                    p.getStartDate(),
                    p.getEndDate(),
                    p.getStatus(),
                    p.getCreatedAt(),
                    p.getUpdatedAt(),
                    p.getClientId(),
                    null,
                    p.getManagerId(),
                    null,
                    p.getMembers() == null ? new HashSet<>() : p.getMembers().stream().map(ProjectMember::getUserId).collect(Collectors.toSet()),
                    Collections.emptySet()
            );
            // optional enrichment for names (best-effort)
            try {
                User clientUser = userClient.getUserById(dto.getClientId());
                if (clientUser != null) dto.setClientName(clientUser.getName());
            } catch (Exception ignored) {}
            return dto;
        }).collect(Collectors.toList());

        return new PageImpl<>(content, pageable, projectPage.getTotalElements());
    }

    
    //Completed : 
    public ProjectDTO getProjectById(Long id) {
        Project p = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found: " + id));
        ProjectDTO dto = new ProjectDTO(
                p.getId(),
                p.getTitle(),
                p.getDescription(),
                p.getStartDate(),
                p.getEndDate(),
                p.getStatus(),
                p.getCreatedAt(),
                p.getUpdatedAt(),
                p.getClientId(),
                null,
                p.getManagerId(),
                null,
                p.getMembers() == null ? new HashSet<>() : p.getMembers().stream().map(ProjectMember::getUserId).collect(Collectors.toSet()),
                Collections.emptySet()
        );
        // optional enrich names
        try {
            User clientUser = userClient.getUserById(dto.getClientId());
            if (clientUser != null) dto.setClientName(clientUser.getName());
        } catch (Exception ignored) {}
        if (dto.getManagerId() != null) {
            try {
                User managerUser = userClient.getUserById(dto.getManagerId());
                if (managerUser != null) dto.setManagerName(managerUser.getName());
            } catch (Exception ignored) {}
        }
        return dto;
    }

    //Completed : 
    public ProjectDTO assignManager(Long projectId, Long managerId) {
        Project p = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found: " + projectId));

        User manager;
        try {
            manager = userClient.getUserById(managerId);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Manager not found: " + managerId);
        }
        if (manager == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Manager not found: " + managerId);

        p.setManagerId(manager.getId());
        Project saved = projectRepository.save(p);

        ProjectDTO dto = new ProjectDTO(
                saved.getId(),
                saved.getTitle(),
                saved.getDescription(),
                saved.getStartDate(),
                saved.getEndDate(),
                saved.getStatus(),
                saved.getCreatedAt(),
                saved.getUpdatedAt(),
                saved.getClientId(),
                null,
                saved.getManagerId(),
                null,
                saved.getMembers() == null ? new HashSet<>() : saved.getMembers().stream().map(ProjectMember::getUserId).collect(Collectors.toSet()),
                Collections.emptySet()
        );

        try {
            User managerUser = userClient.getUserById(dto.getManagerId());
            if (managerUser != null) dto.setManagerName(managerUser.getName());
        } catch (Exception ignored) {}
        return dto;
    }

    
    //Completed : 
    public ProjectDTO addMember(Long projectId, Long userId) {
        Project p = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found: " + projectId));

        User u;
        try {
            u = userClient.getUserById(userId);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found: " + userId);
        }
        if (u == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found: " + userId);

        ProjectMember pm = new ProjectMember();
        pm.setProject(p);
        // <-- set the userId (NOT the ProjectMember id)
        pm.setUserId(u.getId());

        projectMemberRepository.save(pm);

        // keep bi-directional consistency if relation exists locally
        if (p.getMembers() == null) p.setMembers(new HashSet<>());
        p.getMembers().add(pm);
        projectRepository.save(p);

        ProjectDTO dto = new ProjectDTO(
                p.getId(),
                p.getTitle(),
                p.getDescription(),
                p.getStartDate(),
                p.getEndDate(),
                p.getStatus(),
                p.getCreatedAt(),
                p.getUpdatedAt(),
                p.getClientId(),
                null,
                p.getManagerId(),
                null,
                p.getMembers().stream().map(ProjectMember::getUserId).collect(Collectors.toSet()),
                Collections.emptySet()
        );

        return dto;
    }


   
    
    //completed : 
    
    public Long createTaskForProject(Long projectId, String title, String description, LocalDate dueDate, Long assignedById) {
        // verify project exists locally
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found: " + projectId));

        // build Task DTO using IDs only
        Task taskReq = new Task();
        taskReq.setTitle(title);
        taskReq.setDescription(description);
        taskReq.setDueDate(dueDate);

        // set ids instead of nested objects
        taskReq.setProjectId(projectId);
        taskReq.setManagerId(assignedById);

        // ensure lists initialized (optional)
        taskReq.setAssignedDeveloperIds(new ArrayList<>());
        taskReq.setRequiredSkillIds(new ArrayList<>());

        // call task-service via Feign
        Task taskResp;
        try {
            taskResp = taskClient.createTask(taskReq);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Task service call failed: " + ex.getMessage());
        }

        if (taskResp == null || taskResp.getId() == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Task service returned invalid response.");
        }

        // OPTIONAL: record taskId mapping locally if needed (project_task table) — omitted here
        return taskResp.getId();
    }

    
    
    
    
    
    
    
    
    
    
    

    // ---------------------------
    // Helpers
    // ---------------------------

    /**
     * Resolve current user id:
     * Try userClient.getCurrentUser() (feign /me) — best-effort
     * Try X-User-Id header
     *  Try X-User-Email header -> userClient.getUserByEmail(...)
     *  Fallback to newProject.getClientId()
     */
    private Long resolveCurrentUserIdOrFallback(ProjectDTO newProject) {
        // 1) try feign /me
        try {
            User currentUser = userClient.getCurrentUser();
            if (currentUser != null && currentUser.getId() != null) return currentUser.getId();
        } catch (Exception ignored) {}

        // 2/3) try headers from request context (if available)
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest req = attrs == null ? this.httpRequest : attrs.getRequest();
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

        // 4) fallback to payload
        if (newProject != null && newProject.getClientId() != null) return newProject.getClientId();
        return null;
    }
}
