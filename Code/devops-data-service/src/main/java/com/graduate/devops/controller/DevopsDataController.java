package com.graduate.devops.controller;

import com.graduate.devops.service.DevopsDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/devops-domain")
public class DevopsDataController {

    private final DevopsDataService service;

    public DevopsDataController(DevopsDataService service) {
        this.service = service;
    }

    @GetMapping("/devops-overview")
    public Map<String, Object> devopsOverview() {
        return service.getDevopsOverviewPage();
    }

    @GetMapping("/collection-pipeline")
    public Map<String, Object> collectionPipeline() {
        return service.getCollectionPipelinePage();
    }

    @PutMapping("/sources/{sourceId}")
    public Map<String, Object> updateSource(@PathVariable Long sourceId, @RequestBody Map<String, Object> request) {
        return service.updateSource(sourceId, request);
    }
}
