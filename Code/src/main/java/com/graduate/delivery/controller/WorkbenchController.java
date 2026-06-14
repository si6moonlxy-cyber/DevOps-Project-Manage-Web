package com.graduate.delivery.controller;

import com.graduate.delivery.entity.SessionUser;
import com.graduate.delivery.service.SessionService;
import com.graduate.delivery.service.WorkbenchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/api/workbench")
public class WorkbenchController {

    private final WorkbenchService workbenchService;
    private final SessionService sessionService;

    public WorkbenchController(WorkbenchService workbenchService, SessionService sessionService) {
        this.workbenchService = workbenchService;
        this.sessionService = sessionService;
    }

    @GetMapping("/items")
    public Map<String, Object> getItems(HttpSession session) {
        SessionUser user = sessionService.requireLogin(session);
        return workbenchService.getWorkItemsPage(user);
    }

    @GetMapping("/projects")
    public Map<String, Object> getProjects(HttpSession session) {
        SessionUser user = sessionService.requireLogin(session);
        return workbenchService.getProjectsPage(user);
    }

    @GetMapping("/reports")
    public Map<String, Object> getReports(HttpSession session) {
        SessionUser user = sessionService.requireLogin(session);
        return workbenchService.getReportsPage(user);
    }

    @GetMapping("/alerts")
    public Map<String, Object> getAlerts(HttpSession session) {
        SessionUser user = sessionService.requireLogin(session);
        return workbenchService.getAlertsPage(user);
    }
}
