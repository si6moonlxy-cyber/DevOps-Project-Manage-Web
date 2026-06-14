package com.graduate.organization.service;

import com.graduate.organization.entity.SessionUser;
import com.graduate.organization.mapper.OrganizationPermissionMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PortalPageService {

    private final OrganizationPermissionMapper mapper;
    private final RemoteDomainClient remoteDomainClient;
    private final PasswordEncoder passwordEncoder;

    public PortalPageService(OrganizationPermissionMapper mapper, RemoteDomainClient remoteDomainClient, PasswordEncoder passwordEncoder) {
        this.mapper = mapper;
        this.remoteDomainClient = remoteDomainClient;
        this.passwordEncoder = passwordEncoder;
    }

    public Map<String, Object> getPage(String pageCode, SessionUser user) {
        if ("organization-overview".equals(pageCode)) {
            return buildOrganizationOverview(user);
        }
        if ("permission-center".equals(pageCode)) {
            requireSystem(user);
            return buildPermissionCenter();
        }
        if ("project-delivery".equals(pageCode)) {
            requireProjectDomainAccess(user);
            return remoteDomainClient.fetchProjectDelivery();
        }
        if ("work-item-center".equals(pageCode)) {
            requireProjectDomainAccess(user);
            return remoteDomainClient.fetchWorkItemCenter();
        }
        if ("devops-overview".equals(pageCode)) {
            return remoteDomainClient.fetchDevopsOverview();
        }
        if ("collection-pipeline".equals(pageCode)) {
            requireSystem(user);
            return remoteDomainClient.fetchCollectionPipeline();
        }
        if ("metrics-overview".equals(pageCode)) {
            return remoteDomainClient.fetchMetricsOverview();
        }
        if ("report-center".equals(pageCode)) {
            return remoteDomainClient.fetchReportCenter();
        }
        if ("audit-events".equals(pageCode)) {
            return remoteDomainClient.fetchAuditEvents();
        }
        if ("config-console".equals(pageCode)) {
            requireSystem(user);
            return remoteDomainClient.fetchConfigConsole();
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "未找到页面：" + pageCode);
    }

    public Map<String, Object> runConfigConsoleQuery(Map<String, String> request, SessionUser user) {
        requireSystem(user);
        return remoteDomainClient.postConfigConsoleQuery(request);
    }

    public Map<String, Object> updateAccount(Long userId, Map<String, Object> request, SessionUser user) {
        requireSystem(user);
        mapper.updateAccount(
            userId,
            value(request, "username", "user" + userId),
            value(request, "password", ""),
            longValue(request.get("roleId"), 1L),
            value(request, "accountStatus", "ACTIVE"),
            value(request, "description", ""),
            passwordEncoder
        );
        return buildPermissionCenter();
    }

    public Map<String, Object> createMember(Map<String, Object> request, SessionUser user) {
        mapper.createMember(request, passwordEncoder);
        return buildOrganizationOverview(user);
    }

    public Map<String, Object> updateMember(Long userId, Map<String, Object> request, SessionUser user) {
        mapper.updateMemberTeam(userId, longValue(request.get("teamId"), 1L));
        return buildOrganizationOverview(user);
    }

    public Map<String, Object> deleteMember(Long userId, SessionUser user) {
        mapper.deleteMember(userId);
        return buildOrganizationOverview(user);
    }

    public Map<String, Object> createProject(Map<String, Object> request, SessionUser user) {
        requireSuperProjectAdmin(user);
        return remoteDomainClient.createProject(request);
    }

    public Map<String, Object> updateProject(Long projectId, Map<String, Object> request, SessionUser user) {
        requireSuperProjectAdmin(user);
        return remoteDomainClient.updateProject(projectId, request);
    }

    public Map<String, Object> deleteProject(Long projectId, SessionUser user) {
        requireSuperProjectAdmin(user);
        return remoteDomainClient.deleteProject(projectId);
    }

    public Map<String, Object> createWorkItem(Map<String, Object> request, SessionUser user) {
        requireProjectDomainAccess(user);
        return remoteDomainClient.createWorkItem(request);
    }

    public Map<String, Object> updateWorkItem(Long itemId, Map<String, Object> request, SessionUser user) {
        requireProjectDomainAccess(user);
        return remoteDomainClient.updateWorkItem(itemId, request);
    }

    public Map<String, Object> deleteWorkItem(Long itemId, SessionUser user) {
        requireProjectDomainAccess(user);
        return remoteDomainClient.deleteWorkItem(itemId);
    }

    public Map<String, Object> updateSource(Long sourceId, Map<String, Object> request, SessionUser user) {
        if (!"ENTERPRISE_ADMIN".equals(user.getRoleCode()) && !isSuperAdmin(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "当前账号无权修改数据源。");
        }
        return remoteDomainClient.updateSource(sourceId, request);
    }

    private Map<String, Object> buildOrganizationOverview(SessionUser user) {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("summaryCards", buildOrganizationCards(user));
        payload.put("teams", mapper.findTeams());
        payload.put("members", mapper.findAssignableMembers());
        payload.put("roleGuide", buildRoleGuide());
        payload.put("versionNotes", buildVersionNotes());
        return payload;
    }

    private Map<String, Object> buildPermissionCenter() {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("summaryCards", buildPermissionCards());
        payload.put("accounts", mapper.findAccounts());
        payload.put("roles", mapper.findRoles());
        payload.put("permissionNotice", "v1.0.1 版本先完成业务域拆分和角色层级命名同步，后续再扩展更细粒度授权能力。");
        return payload;
    }

    private List<Map<String, Object>> buildOrganizationCards(SessionUser user) {
        List<Map<String, Object>> cards = new ArrayList<Map<String, Object>>();
        cards.add(createCard("当前角色", user.getRoleName(), "", "需求 v1.0.1 对齐后的业务域导航入口", "brand"));
        cards.add(createCard("组织部门", valueOf(mapper.countDepartments()), "个", "组织与权限域负责统一维护部门结构", "success"));
        cards.add(createCard("交付团队", valueOf(mapper.countTeams()), "支", "页面文案已按五个业务域重新编排", "accent"));
        cards.add(createCard("管理账号", valueOf(mapper.countAccounts()), "个", "系统管理与企业管理账号继续保留", "warning"));
        return cards;
    }

    private List<Map<String, Object>> buildPermissionCards() {
        List<Map<String, Object>> cards = new ArrayList<Map<String, Object>>();
        cards.add(createCard("系统管理账号", valueOf(mapper.countSystemAdmins()), "个", "拥有五个业务域的全部系统级页面", "brand"));
        cards.add(createCard("全部管理账号", valueOf(mapper.countAccounts()), "个", "登录账号来源于组织与权限域数据库", "accent"));
        return cards;
    }

    private List<Map<String, Object>> buildRoleGuide() {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        rows.add(createNote("企业管理入口", "默认聚焦项目交付、工作项、数据源概览、指标总览、报表历史和告警审计。"));
        rows.add(createNote("系统管理入口", "额外开放权限管理、采集链路和配置控制台。"));
        rows.add(createNote("答辩演示口径", "数据源与报表页面为展示版能力，不讲成真实 API 联调或自动导出。"));
        return rows;
    }

    private List<Map<String, Object>> buildVersionNotes() {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        rows.add(createNote("业务域组织", "组织与权限域 / 项目交付域 / DevOps 数据域 / 指标与报告域 / 审计与配置域"));
        rows.add(createNote("运行方式", "五个 Spring Boot 微服务可分别启动，前端由组织与权限域服务承载。"));
        rows.add(createNote("界面策略", "沿用现有管理端颜色和布局，仅收口模块文案与领域描述。"));
        return rows;
    }

    private Map<String, Object> createCard(Object title, Object value, String unit, String description, String tone) {
        Map<String, Object> row = new LinkedHashMap<String, Object>();
        row.put("title", title);
        row.put("value", value);
        row.put("unit", unit);
        row.put("description", description);
        row.put("tone", tone);
        return row;
    }

    private Map<String, Object> createNote(String title, String description) {
        Map<String, Object> row = new LinkedHashMap<String, Object>();
        row.put("title", title);
        row.put("description", description);
        return row;
    }

    private String valueOf(Integer value) {
        return value == null ? "0" : String.valueOf(value);
    }

    private void requireSystem(SessionUser user) {
        if (!SessionService.ROLE_SYSTEM_ADMIN.equals(user.getRoleCode())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "当前账号无权访问系统级业务域页面。");
        }
    }
    private void requireSuperProjectAdmin(SessionUser user) {
        if (!isSuperAdmin(user) && !"ENTERPRISE_ADMIN".equals(user.getRoleCode())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "当前账号无权干涉项目。");
        }
    }

    private void requireProjectDomainAccess(SessionUser user) {
        if (SessionService.ROLE_SYSTEM_ADMIN.equals(user.getRoleCode()) && !isSuperAdmin(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "普通系统管理员无权访问项目交付域。");
        }
    }

    private boolean isSuperAdmin(SessionUser user) {
        return SessionService.ROLE_SYSTEM_ADMIN.equals(user.getRoleCode()) && "sys_root".equals(user.getUsername());
    }

    private String value(Map<String, Object> request, String key, String defaultValue) {
        Object value = request.get(key);
        if (value == null || String.valueOf(value).trim().isEmpty()) {
            return defaultValue;
        }
        return String.valueOf(value).trim();
    }

    private Long longValue(Object value, Long defaultValue) {
        if (value == null || String.valueOf(value).trim().isEmpty()) {
            return defaultValue;
        }
        return Long.valueOf(String.valueOf(value).trim());
    }
}
