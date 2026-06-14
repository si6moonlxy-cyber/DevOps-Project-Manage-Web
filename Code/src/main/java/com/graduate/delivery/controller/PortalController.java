package com.graduate.delivery.controller;

import com.graduate.delivery.entity.SessionUser;
import com.graduate.delivery.service.PortalService;
import com.graduate.delivery.service.SessionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/api/portal")
public class PortalController {

    private final PortalService portalService;
    private final SessionService sessionService;

    public PortalController(PortalService portalService, SessionService sessionService) {
        this.portalService = portalService;
        this.sessionService = sessionService;
    }

    @GetMapping("/navigation")
    public Map<String, Object> getNavigation(HttpSession session) {
        SessionUser user = sessionService.requireLogin(session);
        return portalService.getNavigation(user);
    }

    @GetMapping("/home")
    public Map<String, Object> getHome(HttpSession session) {
        SessionUser user = sessionService.requireLogin(session);
        return portalService.getHomePage(user);
    }

    @GetMapping("/metrics")
    public Map<String, Object> getMetrics(HttpSession session) {
        SessionUser user = sessionService.requireLogin(session);
        return portalService.getMetricsPage(user);
    }

    @GetMapping("/sources")
    public Map<String, Object> getSources(HttpSession session) {
        SessionUser user = sessionService.requireLogin(session);
        return portalService.getSourcesPage(user);
    }
}
