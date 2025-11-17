package com.ProjectService.controller;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ProjectService.dto.ProjectDTO;
import com.ProjectService.pojo.AvailableManager;
import com.ProjectService.service.ClientService;
import com.ProjectService.service.ProjectService;

import java.util.List;

@RestController
@RequestMapping("/api/client")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private ProjectService projectService;

    @GetMapping("/dashboard-data")
    public ResponseEntity<Object> getClientDashboardData() {
        // delegate — service handles identity resolution and returns proper ResponseEntity
        return clientService.getClientDashboardData();
    }

    @GetMapping("/projects")
    public Page<ProjectDTO> getProjects(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "5") int limit) {

        // projectService should return a ResponseEntity (paged DTO or list + metadata)
        return projectService.findProjectsForClient(page, limit);
    }

    @GetMapping("/projects/{id}")
    public ProjectDTO getProjectById(@PathVariable Long id) {
        return projectService.getProjectById(id);
    }

    @PutMapping("/projects/{projectId}")
    public ProjectDTO updateProject(@PathVariable Long projectId,
                                                    @RequestBody ProjectDTO newProject) {
        return projectService.clientUpdateProject(projectId, newProject);
    }

    @PostMapping("/project")
    public ProjectDTO createNewProject(@RequestBody ProjectDTO newProject) {
        return projectService.createNewProject(newProject);
    }

    @GetMapping("/available-managers")
    public ResponseEntity<List<AvailableManager>> getAvailableManagers() {
        return clientService.getAvailableManagers();
    }
}
