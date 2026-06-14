package com.graduate.delivery.service;

import com.graduate.delivery.entity.SessionUser;
import com.graduate.delivery.entity.WorkItem;
import com.graduate.delivery.mapper.PortalMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class WorkbenchService {

    private final PortalMapper portalMapper;

    public WorkbenchService(PortalMapper portalMapper) {
        this.portalMapper = portalMapper;
    }

    public Map<String, Object> getWorkItemsPage(SessionUser user) {
        List<WorkItem> items = portalMapper.findWorkItems();
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("summaryCards", buildWorkItemCards(items));
        payload.put("statusDistribution", convertDistribution(portalMapper.countWorkItemsByStatus()));
        payload.put("priorityDistribution", convertDistribution(portalMapper.countWorkItemsByPriority()));
        payload.put("items", buildWorkItemRows(items));
        return payload;
    }

    public Map<String, Object> getProjectsPage(SessionUser user) {
        List<Map<String, Object>> projects = portalMapper.findProjectBoard();
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("summaryCards", buildProjectCards(projects));
        payload.put("projectStatus", convertDistribution(portalMapper.findProjectStatusDistribution()));
        payload.put("projects", projects);
        return payload;
    }

    public Map<String, Object> getReportsPage(SessionUser user) {
        List<Map<String, Object>> reports = portalMapper.findReports();
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("summaryCards", buildReportCards(reports));
        payload.put("reports", reports);
        payload.put("scoreTrend", buildScoreTrend(portalMapper.findReportScoreTrend()));
        return payload;
    }

    public Map<String, Object> getAlertsPage(SessionUser user) {
        List<Map<String, Object>> alerts = portalMapper.findAlerts();
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("summaryCards", buildAlertCards(alerts));
        payload.put("alerts", alerts);
        payload.put("levelDistribution", convertDistribution(portalMapper.countAlertsByLevel()));
        return payload;
    }

    private List<Map<String, Object>> buildWorkItemRows(List<WorkItem> items) {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        for (WorkItem item : items) {
            Map<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("projectName", item.getProjectName());
            row.put("itemTitle", item.getItemTitle());
            row.put("itemType", item.getItemType());
            row.put("itemStatus", item.getItemStatus());
            row.put("priorityLevel", item.getPriorityLevel());
            row.put("ownerName", item.getOwnerName());
            row.put("sprintName", item.getSprintName());
            row.put("progress", item.getProgress());
            row.put("dueDate", item.getDueDate() == null ? "" : formatter.format(item.getDueDate()));
            rows.add(row);
        }
        return rows;
    }

    private List<Map<String, Object>> buildWorkItemCards(List<WorkItem> items) {
        int total = items.size();
        int inProgress = countByStatus(items, "进行中");
        int done = countByStatus(items, "已完成");
        int highPriority = countByPriority(items, "高");

        List<Map<String, Object>> cards = new ArrayList<Map<String, Object>>();
        cards.add(createCard("工作项总数", total, "条", "覆盖需求、测试、缺陷与评审", "brand"));
        cards.add(createCard("进行中", inProgress, "条", "当前需要持续推进的事项", "accent"));
        cards.add(createCard("已完成", done, "条", "可用于答辩演示闭环交付", "success"));
        cards.add(createCard("高优先级", highPriority, "条", "用于展示风险控制能力", "danger"));
        return cards;
    }

    private List<Map<String, Object>> buildProjectCards(List<Map<String, Object>> projects) {
        int total = projects.size();
        int highRisk = 0;
        int stable = 0;
        int avgProgress = 0;
        for (Map<String, Object> project : projects) {
            if ("HIGH".equals(String.valueOf(project.get("riskLevel")))) {
                highRisk++;
            }
            if ("稳定运行".equals(String.valueOf(project.get("status")))) {
                stable++;
            }
            avgProgress += Integer.parseInt(String.valueOf(project.get("progress")));
        }
        avgProgress = total == 0 ? 0 : avgProgress / total;

        List<Map<String, Object>> cards = new ArrayList<Map<String, Object>>();
        cards.add(createCard("项目总览", total, "个", "按风险等级和阶段排序展示", "brand"));
        cards.add(createCard("高风险项目", highRisk, "个", "高风险项目会在表格置顶", "danger"));
        cards.add(createCard("稳定运行", stable, "个", "已进入稳定运营阶段的项目", "success"));
        cards.add(createCard("平均进度", avgProgress, "%", "便于答辩说明项目交付成熟度", "accent"));
        return cards;
    }

    private List<Map<String, Object>> buildReportCards(List<Map<String, Object>> reports) {
        int total = reports.size();
        int completed = 0;
        BigDecimal qualityTotal = BigDecimal.ZERO;
        for (Map<String, Object> report : reports) {
            if ("已生成".equals(String.valueOf(report.get("status"))) || "已留档".equals(String.valueOf(report.get("status")))) {
                completed++;
            }
            qualityTotal = qualityTotal.add(new BigDecimal(String.valueOf(report.get("qualityScore"))));
        }
        BigDecimal average = total == 0 ? BigDecimal.ZERO : qualityTotal.divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);

        List<Map<String, Object>> cards = new ArrayList<Map<String, Object>>();
        cards.add(createCard("历史记录", total, "份", "展示管理报表、项目复盘和质量分析留痕", "brand"));
        cards.add(createCard("平均评分", average, "分", "用于辅助说明交付质量趋势", "success"));
        cards.add(createCard("已留档", completed, "份", "当前版本仅展示历史记录，不提供生成导出", "accent"));
        cards.add(createCard("待复盘", total - completed, "份", "仍需进一步复盘的记录数量", "warning"));
        return cards;
    }

    private Map<String, Object> buildScoreTrend(List<Map<String, Object>> rows) {
        List<String> labels = new ArrayList<String>();
        List<Object> values = new ArrayList<Object>();
        for (Map<String, Object> row : rows) {
            labels.add(String.valueOf(row.get("reportDay")));
            values.add(row.get("qualityScore"));
        }
        Map<String, Object> trend = new LinkedHashMap<String, Object>();
        trend.put("labels", labels);
        trend.put("values", values);
        return trend;
    }

    private List<Map<String, Object>> buildAlertCards(List<Map<String, Object>> alerts) {
        int total = alerts.size();
        int open = 0;
        int p1p2 = 0;
        for (Map<String, Object> alert : alerts) {
            if ("处理中".equals(String.valueOf(alert.get("status")))) {
                open++;
            }
            String level = String.valueOf(alert.get("alertLevel"));
            if ("P1".equals(level) || "P2".equals(level)) {
                p1p2++;
            }
        }

        List<Map<String, Object>> cards = new ArrayList<Map<String, Object>>();
        cards.add(createCard("告警总数", total, "条", "覆盖构建、交付与稳定性异常", "brand"));
        cards.add(createCard("处理中", open, "条", "仍需持续跟踪的告警事件", "danger"));
        cards.add(createCard("高优先级", p1p2, "条", "P1 和 P2 事件需要重点说明", "warning"));
        cards.add(createCard("已恢复", total - open, "条", "可用于展示恢复闭环能力", "success"));
        return cards;
    }

    private List<Map<String, Object>> convertDistribution(List<Map<String, Object>> rows) {
        List<Map<String, Object>> distribution = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("name", row.get("itemName") == null ? row.get("statusName") : row.get("itemName"));
            item.put("value", row.get("totalCount"));
            distribution.add(item);
        }
        return distribution;
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

    private int countByStatus(List<WorkItem> items, String status) {
        int count = 0;
        for (WorkItem item : items) {
            if (status.equals(item.getItemStatus())) {
                count++;
            }
        }
        return count;
    }

    private int countByPriority(List<WorkItem> items, String priority) {
        int count = 0;
        for (WorkItem item : items) {
            if (priority.equals(item.getPriorityLevel())) {
                count++;
            }
        }
        return count;
    }
}
