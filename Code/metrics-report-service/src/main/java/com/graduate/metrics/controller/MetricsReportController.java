package com.graduate.metrics.controller;

import com.graduate.metrics.service.MetricsReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/metrics-domain")
public class MetricsReportController {

    private final MetricsReportService service;

    public MetricsReportController(MetricsReportService service) {
        this.service = service;
    }

    @GetMapping("/metrics-overview")
    public Map<String, Object> metricsOverview() {
        return service.getMetricsOverviewPage();
    }

    @GetMapping("/report-center")
    public Map<String, Object> reportCenter() {
        return service.getReportCenterPage();
    }
}
