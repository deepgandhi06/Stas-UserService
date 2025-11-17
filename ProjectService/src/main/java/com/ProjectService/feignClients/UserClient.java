package com.ProjectService.feignClients;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.ProjectService.pojo.User;

//UserClient.java

@FeignClient(name = "user-service", url = "${feign.user-service.url:http://localhost:8081}")
public interface UserClient {
@GetMapping("/api/users/{id}")
User getUserById(@PathVariable("id") Long id);
@GetMapping("/api/users/by-email")
User getUserByEmail(@RequestParam("email") String email);
// returns list of users for a role name
@GetMapping("/api/users/by-role")
List<User> getByRole(@RequestParam("role") String roleName);

@GetMapping
User getCurrentUser();
}
