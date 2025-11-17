package com.ProjectService.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ProjectService.dto.ProjectDTO;
import com.ProjectService.entity.Project;
import com.ProjectService.entity.ProjectMember;
import com.ProjectService.enums.ProjectStatus;
import com.ProjectService.feignClients.FeedbackClient;
import com.ProjectService.feignClients.TaskClient;
import com.ProjectService.feignClients.UserClient;
import com.ProjectService.pojo.Feedback;
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
public class ManagerService  {

    private final ProjectRepo projectRepo;
    private final ProjectMemberRepo projectMemberRepo;
    private final UserClient userClient;
    private final TaskClient taskClient;
    private final FeedbackClient feedbackClient;

    @Autowired
    public ManagerService(ProjectRepo projectRepo,
                              ProjectMemberRepo projectMemberRepo,
                              UserClient userClient,
                              TaskClient taskClient,
                              FeedbackClient feedbackClient) {
        this.projectRepo = projectRepo;
        this.projectMemberRepo = projectMemberRepo;
        this.userClient = userClient;
        this.taskClient = taskClient;
        this.feedbackClient = feedbackClient;
    }

    // Helper: resolve current user id (manager)
    private Long resolveCurrentUserId() {
        try {
            User me = userClient.getCurrentUser();
            if (me != null && me.getId() != null) return me.getId();
        } catch (Exception ignored) {}

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

    
    
    //completed : 
    public ResponseEntity<?> getManagerDashboardData() {
        Long managerId = resolveCurrentUserId();
        if (managerId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Manager identity not resolved");

        // fetch projects managed by this manager
        List<Project> projects = projectRepo.findByManagerId(managerId);
        if (projects == null) projects = Collections.emptyList();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProjects", projects.size());
        stats.put("pending", projects.stream().filter(p -> p.getStatus() == ProjectStatus.PENDING).count());
        stats.put("ongoing", projects.stream().filter(p -> p.getStatus() == ProjectStatus.ONGOING).count());
        stats.put("completed", projects.stream().filter(p -> p.getStatus() == ProjectStatus.COMPLETED).count());
        stats.put("delayed", projects.stream().filter(p -> p.getStatus() == ProjectStatus.DELAYED).count());

        Map<String, Object> response = new HashMap<>();
        response.put("stats", stats);

        List<ProjectDTO> recent = projects.stream()
                .sorted(Comparator.comparing(Project::getCreatedAt).reversed())
                .limit(10)
                .map(this::toProjectDto)
                .collect(Collectors.toList());
        response.put("recentProjects", recent);

        return ResponseEntity.ok(response);
    }

    
    
    //Completed : 
    public ResponseEntity<?> getProjectsForManager(int page, int limit) {
        Long managerId = resolveCurrentUserId();
        if (managerId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Manager identity not resolved");

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, limit), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Project> pageResult = projectRepo.findByManagerId(managerId, pageable);

        List<ProjectDTO> content = pageResult.getContent().stream().map(this::toProjectDto).collect(Collectors.toList());
        Page<ProjectDTO> dtoPage = new PageImpl<>(content, pageable, pageResult.getTotalElements());
        return ResponseEntity.ok(dtoPage);
    }

    
    
    //Completed : 
    public ResponseEntity<ProjectDTO> getProjectById(Long projectId) {
        Long managerId = resolveCurrentUserId();
        if (managerId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Manager identity not resolved");

        Project p = projectRepo.findById(projectId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        if (!Objects.equals(p.getManagerId(), managerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the manager of this project");
        }
        return ResponseEntity.ok(toProjectDto(p));
    }

    
    //Completed : 
    public ResponseEntity<ProjectDTO> addMember(Long projectId, Long userId) {
        Long managerId = resolveCurrentUserId();
        if (managerId == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Manager identity not resolved");

        Project p = projectRepo.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        if (!Objects.equals(p.getManagerId(), managerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the manager of this project");
        }

        // verify user exists in user-service
        User user;
        try {
            user = userClient.getUserById(userId);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User verification failed: " + ex.getMessage());
        }

        if (user == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found");

        // prevent duplicate
        boolean already = p.getMembers() != null &&
                p.getMembers().stream().anyMatch(m -> Objects.equals(m.getUserId(), userId));
        if (already) {
            // ✅ Return current project DTO with 409 (conflict) status
            return ResponseEntity.status(HttpStatus.CONFLICT).body(toProjectDto(p));
        }

        // add member
        ProjectMember pm = new ProjectMember();
        pm.setProject(p);
        pm.setUserId(user.getId());
        projectMemberRepo.save(pm);

        if (p.getMembers() == null)
            p.setMembers(new HashSet<>());
        p.getMembers().add(pm);
        projectRepo.save(p);

        return ResponseEntity.ok(toProjectDto(p));
    }


    
    //completed 
    public ResponseEntity<ProjectDTO> removeMember(Long projectId, Long userId) {
        Long managerId = resolveCurrentUserId();
        if (managerId == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Manager identity not resolved");

        Project p = projectRepo.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        if (!Objects.equals(p.getManagerId(), managerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the manager of this project");
        }

        Optional<ProjectMember> found = (p.getMembers() == null)
                ? Optional.empty()
                : p.getMembers().stream()
                     .filter(m -> Objects.equals(m.getUserId(), userId))
                     .findFirst();

        if (found.isEmpty()) {
            // ✅ Return current project DTO instead of String, with 404 status
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(toProjectDto(p));
        }

        ProjectMember pm = found.get();
        p.getMembers().remove(pm);
        projectMemberRepo.delete(pm);
        projectRepo.save(p);

        return ResponseEntity.ok(toProjectDto(p));
    }


   //completed 
    public ResponseEntity<ProjectDTO> assignManager(Long projectId, Long managerId) {
        Long callerId = resolveCurrentUserId();
        if (callerId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identity not resolved");

        Project p = projectRepo.findById(projectId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        // TODO: optionally verify new manager role via userClient.getUserById(managerId)
        User manager = null;
        try {
            manager = userClient.getUserById(managerId);
        } catch (Exception ignored) {}
        if (manager == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Manager id invalid");
        }

        p.setManagerId(manager.getId());
        Project saved = projectRepo.save(p);
        return ResponseEntity.ok(toProjectDto(saved));
    }

   
    //completed : 
    public ResponseEntity<?> createTaskForProject(Long projectId, Task task) {
        Long managerId = resolveCurrentUserId();
        if (managerId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identity not resolved");

        Project p = projectRepo.findById(projectId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        if (!Objects.equals(p.getManagerId(), managerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the manager of this project");
        }

        // ensure task has projectId and managerId set correctly
        task.setProjectId(projectId);
        task.setManagerId(managerId);

        // call task service
        Task created;
        try {
            created = taskClient.createTask(task);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Task service call failed: " + ex.getMessage());
        }

        if (created == null || created.getId() == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Task service returned invalid response");
        }

        // optionally persist mapping projectId<->taskId here (not implemented)
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

   
    //completed : 
    public ResponseEntity<?> updateTask(Long taskId, Task task) {
        Long managerId = resolveCurrentUserId();
        if (managerId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identity not resolved");

        // TODO: validate manager has access to task/project. That requires fetching task from task service.
        Task updated;
        try {
            updated = taskClient.updateTask(taskId, task);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Task update failed: " + ex.getMessage());
        }

        return ResponseEntity.ok(updated);
    }

    
   //completed : 
    public ResponseEntity<?> getTasksForProject(Long projectId) {
        Long managerId = resolveCurrentUserId();
        if (managerId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identity not resolved");

        // verify manager access to project
        Project p = projectRepo.findById(projectId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        if (!Objects.equals(p.getManagerId(), managerId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed");

        List<Task> tasks;
        try {
            tasks = taskClient.getTasksByProject(projectId);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch tasks: " + ex.getMessage());
        }

        return ResponseEntity.ok(tasks);
    }

    
    //completed :
    public ResponseEntity<?> getTaskById(Long taskId) {
        Long managerId = resolveCurrentUserId();
        if (managerId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identity not resolved");

        Task task;
        try {
            task = taskClient.getTaskById(taskId);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch task: " + ex.getMessage());
        }
        if (task == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found");

        // TODO: validate manager access to task/project

        return ResponseEntity.ok(task);
    }

    
    //completed : 
    public ResponseEntity<?> assignDeveloperToTask(Long taskId, Long developerId) {
        Long managerId = resolveCurrentUserId();
        if (managerId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identity not resolved");

        // TODO: validate developer role using userClient
        try {
            taskClient.assignDeveloper(taskId, developerId);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to assign developer: " + ex.getMessage());
        }
        return ResponseEntity.ok("Developer assigned");
    }

    //completed : 
    public ResponseEntity<?> removeDeveloperFromTask(Long taskId, Long developerId) {
        Long managerId = resolveCurrentUserId();
        if (managerId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identity not resolved");

        try {
            taskClient.removeDeveloper(taskId, developerId);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to remove developer: " + ex.getMessage());
        }
        return ResponseEntity.ok("Developer removed");
    }

   
    
    //completed : 
    public ResponseEntity<?> changeTaskStatus(Long taskId, String status) {
        Long managerId = resolveCurrentUserId();
        if (managerId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identity not resolved");

        try {
            taskClient.updateTaskStatus(taskId, status);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to change status: " + ex.getMessage());
        }
        return ResponseEntity.ok("Status updated");
    }

    
    
    //completed : 
    public ResponseEntity<?> getAvailableDevelopers(String skill, int page, int limit) {
        // fetch all developers from user-service by role and then filter by skill locally (or call a dedicated endpoint)
        List<User> developers;
        try {
            developers = userClient.getByRole("DEVELOPER");
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch developers: " + ex.getMessage());
        }
        if (developers == null) developers = Collections.emptyList();

        // simple filtering by skill: userFeignDTO should contain list of skill names or we call another endpoint
        List<User> filtered = developers.stream()
                .filter(u -> skill == null || skill.isBlank() || (u.getUserSkills() != null && u.getUserSkills().contains(skill)))
                .collect(Collectors.toList());

        // simple pagination
        int from = Math.min(page * limit, filtered.size());
        int to = Math.min(from + limit, filtered.size());
        List<User> pageList = filtered.subList(from, to);

        return ResponseEntity.ok(pageList);
    }

   
    
    //completed : 
    public ResponseEntity<?> getProjectMembers(Long projectId) {
        Project p = projectRepo.findById(projectId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        Set<Long> memberIds = p.getMembers() == null ? Collections.emptySet() :
                p.getMembers().stream().map(ProjectMember::getUserId).collect(Collectors.toSet());
        return ResponseEntity.ok(memberIds);
    }

    
    //completed : 
    public ResponseEntity<?> giveFeedback(Long projectId, Long taskId, Feedback feedback) {
        Long managerId = resolveCurrentUserId();
        if (managerId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identity not resolved");

        // ensure project exists and manager has access
        Project p = projectRepo.findById(projectId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        if (!Objects.equals(p.getManagerId(), managerId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed");

        // set author id to manager
        feedback.setAuthorId(managerId);
        feedback.setProjectId(projectId);
        feedback.setTaskId(taskId);

        // call feedback service
        Feedback created;
        try {
            created = feedbackClient.createFeedback(feedback);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to persist feedback: " + ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

   
    //completed :
    public ResponseEntity<?> getFeedbacksForProject(Long projectId) {
        List<Feedback> list;
        try {
            list = feedbackClient.getFeedbacksByProject(projectId);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch feedbacks: " + ex.getMessage());
        }
        return ResponseEntity.ok(list);
    }

   //completed : 
    public ResponseEntity<?> getProjectStats(Long projectId) {
        Project p = projectRepo.findById(projectId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        Map<String, Object> stats = new HashMap<>();
        stats.put("projectId", p.getId());
        stats.put("title", p.getTitle());
        stats.put("membersCount", p.getMembers() == null ? 0 : p.getMembers().size());
        stats.put("managerId", p.getManagerId());
        // tasks summary from task service
        try {
            List<Task> tasks = taskClient.getTasksByProject(p.getId());
            stats.put("taskCount", tasks.size());
            long completed = tasks.stream().filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus())).count();
            stats.put("completedTasks", completed);
            long overdue = tasks.stream().filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(LocalDate.now()) && !"COMPLETED".equalsIgnoreCase(t.getStatus())).count();
            stats.put("overdueTasks", overdue);
        } catch (Exception ignored) {
            stats.put("taskCount", "unknown");
        }

        return ResponseEntity.ok(stats);
    }

    // helper to map Project -> ProjectDTO
    private ProjectDTO toProjectDto(Project p) {
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
        dto.setMemberIds(p.getMembers() == null ? new HashSet<>() : p.getMembers().stream().map(ProjectMember::getUserId).collect(Collectors.toSet()));
        dto.setTaskIds(Collections.emptySet()); // tasks via task-service if needed
        return dto;
    }
}

