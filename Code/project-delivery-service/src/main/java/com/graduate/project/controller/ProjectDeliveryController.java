package com.graduate.project.controller;

import com.graduate.project.service.ProjectDeliveryService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/project-domain")
public class ProjectDeliveryController {

    private final ProjectDeliveryService service;

    public ProjectDeliveryController(ProjectDeliveryService service) {
        this.service = service;
    }

    @GetMapping("/project-delivery")
    public Map<String, Object> projectDelivery() {
        return service.getProjectDeliveryPage();
    }

    @GetMapping("/work-item-center")
    public Map<String, Object> workItemCenter() {
        return service.getWorkItemPage();
    }

    @PostMapping("/projects")
    public Map<String, Object> createProject(@RequestBody Map<String, Object> request) {
        return service.createProject(request);
    }

    @PutMapping("/projects/{projectId}")
    public Map<String, Object> updateProject(@PathVariable Long projectId, @RequestBody Map<String, Object> request) {
        return service.updateProject(projectId, request);
    }

    @DeleteMapping("/projects/{projectId}")
    public Map<String, Object> deleteProject(@PathVariable Long projectId) {
        return service.deleteProject(projectId);
    }

    @PostMapping("/work-items")
    public Map<String, Object> createWorkItem(@RequestBody Map<String, Object> request) {
        return service.createWorkItem(request);
    }

    @PutMapping("/work-items/{itemId}")
    public Map<String, Object> updateWorkItem(@PathVariable Long itemId, @RequestBody Map<String, Object> request) {
        return service.updateWorkItem(itemId, request);
    }

    @DeleteMapping("/work-items/{itemId}")
    public Map<String, Object> deleteWorkItem(@PathVariable Long itemId) {
        return service.deleteWorkItem(itemId);
    }
}
