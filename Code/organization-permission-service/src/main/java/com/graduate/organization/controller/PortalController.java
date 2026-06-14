package com.graduate.organization.controller;

import com.graduate.organization.entity.SessionUser;
import com.graduate.organization.service.NavigationService;
import com.graduate.organization.service.PortalPageService;
import com.graduate.organization.service.SessionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/api/portal")
public class PortalController {

    private final SessionService sessionService;
    private final NavigationService navigationService;
    private final PortalPageService portalPageService;

    public PortalController(SessionService sessionService, NavigationService navigationService, PortalPageService portalPageService) {
        this.sessionService = sessionService;
        this.navigationService = navigationService;
        this.portalPageService = portalPageService;
    }

    @GetMapping("/navigation")
    public Map<String, Object> navigation(HttpSession session) {
        SessionUser user = sessionService.requireLogin(session);
        return navigationService.buildNavigation(user);
    }

    @GetMapping("/page/{pageCode}")
    public Map<String, Object> page(@PathVariable String pageCode, HttpSession session) {
        SessionUser user = sessionService.requireLogin(session);
        return portalPageService.getPage(pageCode, user);
    }

    @PostMapping("/page/config-console/query")
    public Map<String, Object> queryConfigConsole(@RequestBody Map<String, String> request, HttpSession session) {
        SessionUser user = sessionService.requireLogin(session);
        return portalPageService.runConfigConsoleQuery(request, user);
    }

    @PutMapping("/accounts/{userId}")
    public Map<String, Object> updateAccount(@PathVariable Long userId, @RequestBody Map<String, Object> request, HttpSession session) {
        SessionUser user = sessionService.requireLogin(session);
        return portalPageService.updateAccount(userId, request, user);
    }

    @PostMapping("/members")
    public Map<String, Object> createMember(@RequestBody Map<String, Object> request, HttpSession session) {
        SessionUser user = sessionService.requireLogin(session);
        return portalPageService.createMember(request, user);
    }

    @PutMapping("/members/{userId}")
    public Map<String, Object> updateMember(@PathVariable Long userId, @RequestBody Map<String, Object> request, HttpSession session) {
        SessionUser user = sessionService.requireLogin(session);
        return portalPageService.updateMember(userId, request, user);
    }

    @DeleteMapping("/members/{userId}")
    public Map<String, Object> deleteMember(@PathVariable Long userId, HttpSession session) {
        SessionUser user = sessionService.requireLogin(session);
        return portalPageService.deleteMember(userId, user);
    }

    @PostMapping("/projects")
    public Map<String, Object> createProject(@RequestBody Map<String, Object> request, HttpSession session) {
        SessionUser user = sessionService.requireLogin(session);
        return portalPageService.createProject(request, user);
    }

    @PutMapping("/projects/{projectId}")
    public Map<String, Object> updateProject(@PathVariable Long projectId, @RequestBody Map<String, Object> request, HttpSession session) {
        SessionUser user = sessionService.requireLogin(session);
        return portalPageService.updateProject(projectId, request, user);
    }

    @DeleteMapping("/projects/{projectId}")
    public Map<String, Object> deleteProject(@PathVariable Long projectId, HttpSession session) {
        SessionUser user = sessionService.requireLogin(session);
        return portalPageService.deleteProject(projectId, user);
    }

    @PostMapping("/work-items")
    public Map<String, Object> createWorkItem(@RequestBody Map<String, Object> request, HttpSession session) {
        SessionUser user = sessionService.requireLogin(session);
        return portalPageService.createWorkItem(request, user);
    }

    @PutMapping("/work-items/{itemId}")
    public Map<String, Object> updateWorkItem(@PathVariable Long itemId, @RequestBody Map<String, Object> request, HttpSession session) {
        SessionUser user = sessionService.requireLogin(session);
        return portalPageService.updateWorkItem(itemId, request, user);
    }

    @DeleteMapping("/work-items/{itemId}")
    public Map<String, Object> deleteWorkItem(@PathVariable Long itemId, HttpSession session) {
        SessionUser user = sessionService.requireLogin(session);
        return portalPageService.deleteWorkItem(itemId, user);
    }

    @PutMapping("/sources/{sourceId}")
    public Map<String, Object> updateSource(@PathVariable Long sourceId, @RequestBody Map<String, Object> request, HttpSession session) {
        SessionUser user = sessionService.requireLogin(session);
        return portalPageService.updateSource(sourceId, request, user);
    }
}
