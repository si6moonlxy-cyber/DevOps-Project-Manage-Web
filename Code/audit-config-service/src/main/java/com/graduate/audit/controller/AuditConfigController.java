package com.graduate.audit.controller;

import com.graduate.audit.service.AuditConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/audit-domain")
public class AuditConfigController {

    private final AuditConfigService auditConfigService;

    public AuditConfigController(AuditConfigService auditConfigService) {
        this.auditConfigService = auditConfigService;
    }

    @GetMapping("/audit-events")
    public Map<String, Object> auditEvents() {
        return auditConfigService.getAuditEventsPage();
    }

    @GetMapping("/config-console")
    public Map<String, Object> configConsole() {
        return auditConfigService.getConfigConsolePage();
    }

    @PostMapping("/config-console/query")
    public Map<String, Object> query(@RequestBody Map<String, String> request) {
        return auditConfigService.runQuery(request);
    }
}
