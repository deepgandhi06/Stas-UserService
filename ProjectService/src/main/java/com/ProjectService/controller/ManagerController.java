package com.ProjectService.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ProjectService.dto.ProjectDTO;
import com.ProjectService.pojo.Feedback;
import com.ProjectService.pojo.Task;
import com.ProjectService.service.ManagerService;
import com.ProjectService.service.ProjectService;

/**
 * ManagerController - full set of manager endpoints (18 mappings).
 *
 * Controllers are intentionally thin. All logic (auth resolution, permission checks,
 * Feign calls to user/task services, persistence) must live inside ManagerService.
 *
 * TODOs are left where Feign clients / cross-service calls will be needed inside the service.
 */
@RestController
@RequestMapping("/api/manager")
public class ManagerController {

    private final ManagerService managerService;
    private final ProjectService projectService;

    @Autowired
    public ManagerController(ManagerService managerService, ProjectService projectService) {
        this.managerService = managerService;
        this.projectService = projectService;
    }

    // 1. Manager dashboard (summary/stats)
    @GetMapping("/dashboard-data")
    public ResponseEntity<?> getManagerDashboardData() {
        return managerService.getManagerDashboardData();
    }

    // 2. List projects for manager (paged)
    @GetMapping("/projects")
    public ResponseEntity<?> getProjectsForManager(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return managerService.getProjectsForManager(page, limit);
    }

    // 3. Get single project details
    @GetMapping("/projects/{projectId}")
    public ResponseEntity<ProjectDTO> getProjectById(@PathVariable Long projectId) {
        return managerService.getProjectById(projectId);
    }

    // 4. Add a member (developer) to a project
    @PostMapping("/projects/{projectId}/members/{userId}")
    public ResponseEntity<ProjectDTO> addMemberToProject(@PathVariable Long projectId,
                                                         @PathVariable Long userId) {
        // TODO: ManagerService should verify manager permission and call UserClient to confirm user exists.
        return managerService.addMember(projectId, userId);
    }

    // 5. Remove a member from a project
    @DeleteMapping("/projects/{projectId}/members/{userId}")
    public ResponseEntity<ProjectDTO> removeMemberFromProject(@PathVariable Long projectId,
                                                              @PathVariable Long userId) {
        return managerService.removeMember(projectId, userId);
    }

    // 6. Assign or change project manager
    @PostMapping("/projects/{projectId}/assign-manager/{managerId}")
    public ResponseEntity<ProjectDTO> assignManager(@PathVariable Long projectId,
                                                    @PathVariable Long managerId) {
        // TODO: ManagerService should validate managerId role via UserClient if necessary
        return managerService.assignManager(projectId, managerId);
    }

    // 7. Create a task for project (accept Task DTO you already have)
    @PostMapping("/projects/{projectId}/tasks")
    public ResponseEntity<?> createTaskForProject(@PathVariable Long projectId,
                                                  @RequestBody Task task) {
        // TODO: ManagerService must validate manager permission, then call TaskClient.createTask(task)
        return managerService.createTaskForProject(projectId, task);
    }

    // 8. Update task (task metadata/status/description)
    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<?> updateTask(@PathVariable Long taskId,
                                        @RequestBody Task task) {
        // TODO: validate manager permission for this task/project
        return managerService.updateTask(taskId, task);
    }

    // 9. Get all tasks for a project
    @GetMapping("/projects/{projectId}/tasks")
    public ResponseEntity<?> getTasksForProject(@PathVariable Long projectId) {
        return managerService.getTasksForProject(projectId);
    }

    // 10. Get single task by id
    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<?> getTaskById(@PathVariable Long taskId) {
        return managerService.getTaskById(taskId);
    }

    // 11. Assign a developer to a task
    @PostMapping("/tasks/{taskId}/assign-developer/{developerId}")
    public ResponseEntity<?> assignDeveloperToTask(@PathVariable Long taskId,
                                                   @PathVariable Long developerId) {
        // TODO: verify developer role via UserClient inside service
        return managerService.assignDeveloperToTask(taskId, developerId);
    }

    // 12. Remove a developer from a task
    @DeleteMapping("/tasks/{taskId}/remove-developer/{developerId}")
    public ResponseEntity<?> removeDeveloperFromTask(@PathVariable Long taskId,
                                                     @PathVariable Long developerId) {
        return managerService.removeDeveloperFromTask(taskId, developerId);
    }

    // 13. Change task status (e.g., IN_PROGRESS, IN_REVIEW, COMPLETED)
    @PostMapping("/tasks/{taskId}/status")
    public ResponseEntity<?> changeTaskStatus(@PathVariable Long taskId,
                                              @RequestParam("status") String status) {
        return managerService.changeTaskStatus(taskId, status);
    }

    // 14. Get available developers (optionally filter by skill & paginate)
    @GetMapping("/available-developers")
    public ResponseEntity<?> getAvailableDevelopers(
            @RequestParam(value = "skill", required = false) String skill,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        // TODO: ManagerService will likely call UserClient.getByRole("DEVELOPER") and filter by skill
        return managerService.getAvailableDevelopers(skill, page, limit);
    }

    // 15. Get project members (list of userIds)
    @GetMapping("/projects/{projectId}/members")
    public ResponseEntity<?> getProjectMembers(@PathVariable Long projectId) {
        return managerService.getProjectMembers(projectId);
    }

    // 16. Give feedback (manager -> developer or manager -> task)
    @PostMapping("/projects/{projectId}/tasks/{taskId}/feedback")
    public ResponseEntity<?> giveFeedback(@PathVariable Long projectId,
                                          @PathVariable Long taskId,
                                          @RequestBody Feedback feedback) {
        // TODO: Service should call Feedback service or persist feedback if internal.
        return managerService.giveFeedback(projectId, taskId, feedback);
    }

    // 17. Get feedbacks for a project
    @GetMapping("/projects/{projectId}/feedbacks")
    public ResponseEntity<?> getFeedbacksForProject(@PathVariable Long projectId) {
        return managerService.getFeedbacksForProject(projectId);
    }

    // 18. Get project statistics (detailed)
    @GetMapping("/projects/{projectId}/stats")
    public ResponseEntity<?> getProjectStats(@PathVariable Long projectId) {
        return managerService.getProjectStats(projectId);
    }
}
