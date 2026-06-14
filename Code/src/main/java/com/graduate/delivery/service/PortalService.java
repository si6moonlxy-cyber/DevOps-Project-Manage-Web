package com.graduate.delivery.service;

import com.graduate.delivery.entity.DeliveryActivity;
import com.graduate.delivery.entity.SessionUser;
import com.graduate.delivery.mapper.PortalMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class PortalService {

    private static final DateTimeFormatter CHINESE_DATE = DateTimeFormatter.ofPattern("M月d日，EEEE", Locale.CHINA);

    private final PortalMapper portalMapper;

    public PortalService(PortalMapper portalMapper) {
        this.portalMapper = portalMapper;
    }

    public Map<String, Object> getNavigation(SessionUser user) {
        List<Map<String, Object>> modules = buildModules(user.getRoleCode());
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("appName", "基于 DevOps 的通讯企业 IT 项目交付效能度量平台");
        payload.put("subtitle", "本科毕设答辩演示版管理端");
        payload.put("todayLabel", CHINESE_DATE.format(LocalDate.now()));
        payload.put("visiblePageCount", getVisiblePageCount(user.getRoleCode()));
        payload.put("modules", modules);
        return payload;
    }

    public int getVisiblePageCount(String roleCode) {
        int pageCount = 0;
        for (Map<String, Object> module : buildModules(roleCode)) {
            List<?> pages = (List<?>) module.get("pages");
            pageCount += pages == null ? 0 : pages.size();
        }
        return pageCount;
    }

    public Map<String, Object> getHomePage(SessionUser user) {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("welcomeTitle", "嗨，" + user.getDisplayName());
        payload.put("roleName", user.getRoleName());
        payload.put("todayLabel", CHINESE_DATE.format(LocalDate.now()));
        payload.put("summaryCards", buildHomeCards(user));
        payload.put("recentActivities", buildActivityRows(portalMapper.findRecentActivities()));
        payload.put("milestones", portalMapper.findUpcomingMilestones());
        payload.put("modulePanels", buildModulePanels(user.getRoleCode()));
        return payload;
    }

    public Map<String, Object> getMetricsPage(SessionUser user) {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("pageTitle", "企业交付仪表盘");
        payload.put("summaryCards", buildMetricCards());
        payload.put("metricTrend", buildMetricTrend());
        payload.put("metricCatalog", portalMapper.findMetricCatalog());
        payload.put("projectStatus", convertDistribution(portalMapper.findProjectStatusDistribution(), "statusName"));
        return payload;
    }

    public Map<String, Object> getSourcesPage(SessionUser user) {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("pageTitle", "数据源接入展示");
        payload.put("summaryCards", buildSourceCards());
        payload.put("statusDistribution", convertDistribution(portalMapper.findSourceStatusDistribution(), "statusName"));
        payload.put("sources", portalMapper.findSources());
        payload.put("pipelineSteps", buildPipelineSteps());
        return payload;
    }

    private List<Map<String, Object>> buildHomeCards(SessionUser user) {
        List<Map<String, Object>> cards = new ArrayList<Map<String, Object>>();
        cards.add(createCard("可见页面", getVisiblePageCount(user.getRoleCode()), "页", "根据角色自动收敛菜单层级", "brand"));
        cards.add(createCard("在研项目", safeInt(portalMapper.countProjects()), "个", "当前纳入交付度量的项目总数", "success"));
        cards.add(createCard("工作项", safeInt(portalMapper.countWorkItems()), "条", "覆盖需求、任务、缺陷与评审事项", "accent"));
        cards.add(createCard("逾期待办", safeInt(portalMapper.countOverdueItems()), "条", "用于答辩演示工作量跟踪能力", "warning"));
        cards.add(createCard("高风险项目", safeInt(portalMapper.countHighRiskProjects()), "个", "高风险项目会在项目页置顶显示", "danger"));
        cards.add(createCard("开放告警", safeInt(portalMapper.countOpenAlerts()), "条", "当前仍处于处理中状态的事件", "brand"));
        return cards;
    }

    private List<Map<String, Object>> buildMetricCards() {
        List<Map<String, Object>> cards = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> rows = portalMapper.findMetricOverviewRows();
        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("title", row.get("metricName"));
            item.put("value", row.get("metricValue"));
            item.put("unit", row.get("unitName"));
            item.put("target", row.get("targetValue"));
            item.put("delta", row.get("trendRate"));
            item.put("warningLevel", row.get("warningLevel"));
            item.put("tone", toneOfLevel(String.valueOf(row.get("warningLevel"))));
            cards.add(item);
        }
        return cards;
    }

    private Map<String, Object> buildMetricTrend() {
        List<Map<String, Object>> rows = portalMapper.findMetricTrendRows();
        Set<String> labels = new LinkedHashSet<String>();
        Map<String, List<BigDecimal>> seriesData = new LinkedHashMap<String, List<BigDecimal>>();
        List<String> metricOrder = Arrays.asList("REQ_DELIVERY_CYCLE", "DEPLOY_FREQUENCY", "CHANGE_FAILURE_RATE", "MTTR");
        for (String metricCode : metricOrder) {
            seriesData.put(metricCode, new ArrayList<BigDecimal>());
        }
        for (Map<String, Object> row : rows) {
            labels.add(String.valueOf(row.get("snapshotMonth")));
        }
        for (String metricCode : metricOrder) {
            for (String label : labels) {
                seriesData.get(metricCode).add(findMetricValue(rows, metricCode, label));
            }
        }

        List<Map<String, Object>> definitions = portalMapper.findMetricOverviewRows();
        Map<String, Map<String, Object>> definitionMap = new LinkedHashMap<String, Map<String, Object>>();
        for (Map<String, Object> definition : definitions) {
            definitionMap.put(String.valueOf(definition.get("metricCode")), definition);
        }

        List<Map<String, Object>> series = new ArrayList<Map<String, Object>>();
        for (String metricCode : metricOrder) {
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("code", metricCode);
            item.put("name", definitionMap.get(metricCode).get("metricName"));
            item.put("unit", definitionMap.get(metricCode).get("unitName"));
            item.put("data", seriesData.get(metricCode));
            series.add(item);
        }

        Map<String, Object> trend = new LinkedHashMap<String, Object>();
        trend.put("labels", new ArrayList<String>(labels));
        trend.put("series", series);
        return trend;
    }

    private List<Map<String, Object>> buildSourceCards() {
        int total = safeInt(portalMapper.countAllSources());
        int online = safeInt(portalMapper.countOnlineSources());
        BigDecimal onlineRate = total == 0
            ? BigDecimal.ZERO
            : BigDecimal.valueOf(online * 100.0 / total).setScale(1, RoundingMode.HALF_UP);

        List<Map<String, Object>> cards = new ArrayList<Map<String, Object>>();
        cards.add(createCard("在线数据源", online, "个", "用于演示 DevOps 工具链接入状态", "success"));
        cards.add(createCard("数据源总数", total, "个", "Jira / GitLab / Jenkins / Prometheus", "brand"));
        cards.add(createCard("展示健康度", onlineRate, "%", "样例数据下的接入链路健康度", "accent"));
        cards.add(createCard("异常告警", safeInt(portalMapper.countOpenAlerts()), "条", "异常事件会联动到告警中心", "warning"));
        return cards;
    }

    private List<Map<String, Object>> buildPipelineSteps() {
        List<Map<String, Object>> steps = new ArrayList<Map<String, Object>>();
        steps.add(createPipelineStep("需求采集", "Jira 同步需求、缺陷与任务数据。"));
        steps.add(createPipelineStep("代码分析", "GitLab 汇聚提交、MR 与分支信息。"));
        steps.add(createPipelineStep("发布监测", "Jenkins 提供构建与部署成功率。"));
        steps.add(createPipelineStep("稳定性回流", "Prometheus 反馈恢复时长与告警事件。"));
        return steps;
    }

    private List<Map<String, Object>> buildModulePanels(String roleCode) {
        List<Map<String, Object>> panels = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> module : buildModules(roleCode)) {
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("name", module.get("name"));
            item.put("description", module.get("description"));
            item.put("pageCount", ((List<?>) module.get("pages")).size());
            panels.add(item);
        }
        return panels;
    }

    private List<Map<String, Object>> buildModules(String roleCode) {
        List<Map<String, Object>> modules = new ArrayList<Map<String, Object>>();
        modules.add(createModule("home", "首页", "⌂", "查看概况与近期动态",
            createPage("overview", "概况", "⌂", "/api/portal/home"),
            isSystem(roleCode) ? createPage("activity", "近期动态", "◎", "/api/portal/home") : null
        ));
        modules.add(createModule("dashboard", "仪表盘", "◫", "展示核心指标与数据源接入概览",
            createPage("metrics", "指标总览", "◫", "/api/portal/metrics"),
            createPage("sources", "数据源接入", "≋", "/api/portal/sources")
        ));
        modules.add(createModule("workbench", "工作量", "▤", "管理工作项、项目、报表和告警",
            createPage("workitems", "工作项", "▤", "/api/workbench/items"),
            createPage("projects", "项目交付", "▣", "/api/workbench/projects"),
            createPage("reports", "报表历史", "◧", "/api/workbench/reports"),
            createPage("alerts", "告警中心", "!", "/api/workbench/alerts")
        ));
        if (isSystem(roleCode)) {
            modules.add(createModule("management", "管理端", "⚙", "系统管理员查看账号与数据库控制台",
                createPage("accounts", "管理账号", "☺", "/api/management/accounts"),
                createPage("database", "数据库控制台", "⌘", "/api/management/database")
            ));
        }
        return modules;
    }

    private Map<String, Object> createCard(String title, Object value, String unit, String description, String tone) {
        Map<String, Object> card = new LinkedHashMap<String, Object>();
        card.put("title", title);
        card.put("value", value);
        card.put("unit", unit);
        card.put("description", description);
        card.put("tone", tone);
        return card;
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

    private Map<String, Object> createPipelineStep(String title, String description) {
        Map<String, Object> item = new LinkedHashMap<String, Object>();
        item.put("title", title);
        item.put("description", description);
        return item;
    }

    private List<Map<String, Object>> buildActivityRows(List<DeliveryActivity> activities) {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (DeliveryActivity activity : activities) {
            Map<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("activityTitle", activity.getActivityTitle());
            row.put("activityType", activity.getActivityType());
            row.put("projectName", activity.getProjectName());
            row.put("ownerName", activity.getOwnerName());
            row.put("activityStatus", activity.getActivityStatus());
            row.put("occurredAt", activity.getOccurredAt() == null ? "" : formatter.format(activity.getOccurredAt()));
            row.put("detailText", activity.getDetailText());
            rows.add(row);
        }
        return rows;
    }

    private List<Map<String, Object>> convertDistribution(List<Map<String, Object>> rows, String nameField) {
        List<Map<String, Object>> distribution = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("name", row.get(nameField));
            item.put("value", row.get("totalCount"));
            distribution.add(item);
        }
        return distribution;
    }

    private BigDecimal findMetricValue(List<Map<String, Object>> rows, String metricCode, String label) {
        for (Map<String, Object> row : rows) {
            if (metricCode.equals(String.valueOf(row.get("metricCode"))) && label.equals(String.valueOf(row.get("snapshotMonth")))) {
                Object value = row.get("metricValue");
                return value instanceof BigDecimal ? (BigDecimal) value : BigDecimal.valueOf(Double.parseDouble(String.valueOf(value)));
            }
        }
        return BigDecimal.ZERO;
    }

    private String toneOfLevel(String warningLevel) {
        if ("CRITICAL".equalsIgnoreCase(warningLevel)) {
            return "danger";
        }
        if ("WARNING".equalsIgnoreCase(warningLevel)) {
            return "warning";
        }
        return "success";
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value.intValue();
    }

    private boolean isSystem(String roleCode) {
        return SessionService.ROLE_SYSTEM_ADMIN.equals(roleCode);
    }
}
