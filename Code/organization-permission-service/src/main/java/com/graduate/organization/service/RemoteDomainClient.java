package com.graduate.organization.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class RemoteDomainClient {

    private final RestTemplate restTemplate;
    private final String projectBaseUrl;
    private final String devopsBaseUrl;
    private final String metricsBaseUrl;
    private final String auditBaseUrl;

    public RemoteDomainClient(RestTemplate restTemplate,
                              @Value("${services.project-base-url}") String projectBaseUrl,
                              @Value("${services.devops-base-url}") String devopsBaseUrl,
                              @Value("${services.metrics-base-url}") String metricsBaseUrl,
                              @Value("${services.audit-base-url}") String auditBaseUrl) {
        this.restTemplate = restTemplate;
        this.projectBaseUrl = projectBaseUrl;
        this.devopsBaseUrl = devopsBaseUrl;
        this.metricsBaseUrl = metricsBaseUrl;
        this.auditBaseUrl = auditBaseUrl;
    }

    public Map<String, Object> fetchProjectDelivery() {
        return get(projectBaseUrl + "/api/project-domain/project-delivery");
    }

    public Map<String, Object> fetchWorkItemCenter() {
        return get(projectBaseUrl + "/api/project-domain/work-item-center");
    }

    public Map<String, Object> fetchDevopsOverview() {
        return get(devopsBaseUrl + "/api/devops-domain/devops-overview");
    }

    public Map<String, Object> fetchCollectionPipeline() {
        return get(devopsBaseUrl + "/api/devops-domain/collection-pipeline");
    }

    public Map<String, Object> fetchMetricsOverview() {
        return get(metricsBaseUrl + "/api/metrics-domain/metrics-overview");
    }

    public Map<String, Object> fetchReportCenter() {
        return get(metricsBaseUrl + "/api/metrics-domain/report-center");
    }

    public Map<String, Object> fetchAuditEvents() {
        return get(auditBaseUrl + "/api/audit-domain/audit-events");
    }

    public Map<String, Object> fetchConfigConsole() {
        return get(auditBaseUrl + "/api/audit-domain/config-console");
    }

    public Map<String, Object> postConfigConsoleQuery(Map<String, String> request) {
        RequestEntity<Map<String, String>> entity = new RequestEntity<Map<String, String>>(request, HttpMethod.POST, URI.create(auditBaseUrl + "/api/audit-domain/config-console/query"));
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(entity, new ParameterizedTypeReference<Map<String, Object>>() {
        });
        return response.getBody();
    }

    public Map<String, Object> createProject(Map<String, Object> request) {
        return exchange(projectBaseUrl + "/api/project-domain/projects", HttpMethod.POST, request);
    }

    public Map<String, Object> updateProject(Long projectId, Map<String, Object> request) {
        return exchange(projectBaseUrl + "/api/project-domain/projects/" + projectId, HttpMethod.PUT, request);
    }

    public Map<String, Object> deleteProject(Long projectId) {
        return exchange(projectBaseUrl + "/api/project-domain/projects/" + projectId, HttpMethod.DELETE, null);
    }

    public Map<String, Object> createWorkItem(Map<String, Object> request) {
        return exchange(projectBaseUrl + "/api/project-domain/work-items", HttpMethod.POST, request);
    }

    public Map<String, Object> updateWorkItem(Long itemId, Map<String, Object> request) {
        return exchange(projectBaseUrl + "/api/project-domain/work-items/" + itemId, HttpMethod.PUT, request);
    }

    public Map<String, Object> deleteWorkItem(Long itemId) {
        return exchange(projectBaseUrl + "/api/project-domain/work-items/" + itemId, HttpMethod.DELETE, null);
    }

    public Map<String, Object> updateSource(Long sourceId, Map<String, Object> request) {
        return exchange(devopsBaseUrl + "/api/devops-domain/sources/" + sourceId, HttpMethod.PUT, request);
    }

    private Map<String, Object> get(String url) {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<Map<String, Object>>() {
        });
        return response.getBody();
    }

    private Map<String, Object> exchange(String url, HttpMethod method, Map<String, Object> request) {
        RequestEntity<Map<String, Object>> entity = new RequestEntity<Map<String, Object>>(request, method, URI.create(url));
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(entity, new ParameterizedTypeReference<Map<String, Object>>() {
        });
        return response.getBody();
    }
}
