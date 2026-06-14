package com.graduate.delivery.controller;

import com.graduate.delivery.entity.SessionUser;
import com.graduate.delivery.service.ManagementService;
import com.graduate.delivery.service.SessionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/api/management")
public class ManagementController {

    private final ManagementService managementService;
    private final SessionService sessionService;

    public ManagementController(ManagementService managementService, SessionService sessionService) {
        this.managementService = managementService;
        this.sessionService = sessionService;
    }

    @GetMapping("/accounts")
    public Map<String, Object> getAccounts(HttpSession session) {
        SessionUser user = sessionService.requireSystemAdmin(session);
        return managementService.getAccountsPage(user);
    }

    @GetMapping("/database")
    public Map<String, Object> getDatabase(HttpSession session) {
        SessionUser user = sessionService.requireSystemAdmin(session);
        return managementService.getDatabasePage(user);
    }

    @PostMapping("/database/query")
    public Map<String, Object> runQuery(@RequestBody Map<String, String> request, HttpSession session) {
        SessionUser user = sessionService.requireSystemAdmin(session);
        return managementService.runQuery(user, request);
    }
}
