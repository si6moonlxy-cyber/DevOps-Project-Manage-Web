package com.graduate.organization.service;

import com.graduate.organization.entity.SessionUser;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class NavigationService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("M月d日，EEEE", Locale.CHINA);

    public Map<String, Object> buildNavigation(SessionUser user) {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("appName", "DevOps 交付效能度量平台 v1.0.1");
        payload.put("subtitle", "按五个业务域拆分的本科毕设答辩演示版");
        payload.put("todayLabel", DATE_FORMATTER.format(LocalDate.now()));
        payload.put("visiblePageCount", countVisiblePages(user));
        payload.put("modules", buildModules(user));
        return payload;
    }

    public int countVisiblePages(SessionUser user) {
        int count = 0;
        for (Map<String, Object> module : buildModules(user)) {
            count += ((List<?>) module.get("pages")).size();
        }
        return count;
    }

    public List<Map<String, Object>> buildModules(SessionUser user) {
        String roleCode = user.getRoleCode();
        List<Map<String, Object>> modules = new ArrayList<Map<String, Object>>();
        modules.add(createModule("organization-domain", "组织与权限域", "组", "用户、角色、权限与组织信息管理",
            createPage("organization-overview", "组织概况", "概", "/api/portal/page/organization-overview"),
            isSystem(roleCode) ? createPage("permission-center", "权限管理", "权", "/api/portal/page/permission-center") : null
        ));
        modules.add(createModule("project-domain", "项目交付域", "项", "项目主数据、工作项和交付进展",
            createPage("project-delivery", "项目交付", "交", "/api/portal/page/project-delivery"),
            createPage("work-item-center", "工作项清单", "工", "/api/portal/page/work-item-center")
        ));
        modules.add(createModule("devops-domain", "DevOps 数据域", "数", "工具链接入展示、采集链路与同步状态",
            createPage("devops-overview", "数据源概览", "源", "/api/portal/page/devops-overview"),
            isSystem(roleCode) ? createPage("collection-pipeline", "采集链路", "链", "/api/portal/page/collection-pipeline") : null
        ));
        modules.add(createModule("metrics-domain", "指标与报告域", "指", "核心指标、趋势分析与报表历史",
            createPage("metrics-overview", "指标总览", "标", "/api/portal/page/metrics-overview"),
            createPage("report-center", "报表历史", "报", "/api/portal/page/report-center")
        ));
        modules.add(createModule("audit-domain", "审计与配置域", "审", "告警审计、配置台账与只读查询控制台",
            createPage("audit-events", "告警审计", "警", "/api/portal/page/audit-events"),
            isSystem(roleCode) ? createPage("config-console", "配置控制台", "控", "/api/portal/page/config-console") : null
        ));
        if (isPlatformAdmin(roleCode) && !isSuperAdmin(user)) {
            modules.removeIf(module -> "project-domain".equals(module.get("code")));
        }
        return modules;
    }

    private Map<String, Object> createModule(String code, String name, String icon, String description, Map<String, Object>... pages) {
        Map<String, Object> module = new LinkedHashMap<String, Object>();
        module.put("code", code);
        module.put("name", name);
        module.put("icon", icon);
        module.put("description", description);
        List<Map<String, Object>> pageList = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> page : pages) {
            if (page != null) {
                pageList.add(page);
            }
        }
        module.put("pages", pageList);
        return module;
    }

    private Map<String, Object> createPage(String code, String name, String icon, String endpoint) {
        Map<String, Object> page = new LinkedHashMap<String, Object>();
        page.put("code", code);
        page.put("name", name);
        page.put("icon", icon);
        page.put("endpoint", endpoint);
        return page;
    }

    private boolean isSystem(String roleCode) {
        return SessionService.ROLE_SYSTEM_ADMIN.equals(roleCode);
    }

    private boolean isPlatformAdmin(String roleCode) {
        return "SYSTEM_ADMIN".equals(roleCode)
            || "AUDIT_ADMIN".equals(roleCode)
            || "OPS_ADMIN".equals(roleCode)
            || "CONFIG_ADMIN".equals(roleCode);
    }

    private boolean isSuperAdmin(SessionUser user) {
        return isSystem(user.getRoleCode()) && "sys_root".equals(user.getUsername());
    }
}
