package com.ProjectService.feignClients;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.ProjectService.pojo.Feedback;

import java.util.List;

@FeignClient(name = "feedback-service", url = "${feign.feedback-service.url:http://localhost:8090}")
public interface FeedbackClient {

    @PostMapping("/api/feedbacks")
    Feedback createFeedback(@RequestBody Feedback feedback);

    @GetMapping("/api/feedbacks/project/{projectId}")
    List<Feedback> getFeedbacksByProject(@PathVariable("projectId") Long projectId);

    @GetMapping("/api/feedbacks/task/{taskId}")
    List<Feedback> getFeedbacksByTask(@PathVariable("taskId") Long taskId);

    @GetMapping("/api/feedbacks/user/{userId}")
    List<Feedback> getFeedbacksByUser(@PathVariable("userId") Long userId);
}

