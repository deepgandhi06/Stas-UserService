package com.ProjectService.feignClients;


import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.ProjectService.pojo.Task;
@FeignClient(name = "task-service", url = "${feign.task-service.url:http://localhost:8082}")
public interface TaskClient {
@PostMapping("/api/tasks")
Task createTask(Task request);

@PutMapping("/api/tasks/{taskId}")
Task updateTask(@PathVariable("taskId") Long taskId, @RequestBody Task task);

@GetMapping("/api/tasks/project/{projectId}")
List<Task> getTasksByProject(@PathVariable("projectId") Long projectId);

@GetMapping("/api/tasks/{taskId}")
Task getTaskById(@PathVariable("taskId") Long taskId);

@PostMapping("/api/tasks/{taskId}/assign-developer/{developerId}")
void assignDeveloper(@PathVariable("taskId") Long taskId, @PathVariable("developerId") Long developerId);

@DeleteMapping("/api/tasks/{taskId}/remove-developer/{developerId}")
void removeDeveloper(@PathVariable("taskId") Long taskId, @PathVariable("developerId") Long developerId);

@PutMapping("/api/tasks/{taskId}/status")
void updateTaskStatus(@PathVariable("taskId") Long taskId,
                      @RequestParam("status") String status);

}
