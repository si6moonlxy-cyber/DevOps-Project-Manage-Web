package com.graduate.delivery.mapper;

import com.graduate.delivery.entity.DeliveryActivity;
import com.graduate.delivery.entity.WorkItem;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class PortalMapper {

    private final JdbcTemplate jdbcTemplate;

    public PortalMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Integer countProjects() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM project", Integer.class);
    }

    public Integer countWorkItems() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM work_item", Integer.class);
    }

    public Integer countOverdueItems() {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM work_item WHERE due_date < CURRENT_DATE AND item_status <> '已完成'",
            Integer.class
        );
    }

    public Integer countReports() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM report_record", Integer.class);
    }

    public Integer countHighRiskProjects() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM project WHERE risk_level = 'HIGH'", Integer.class);
    }

    public Integer countOpenAlerts() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM alert_event WHERE status = '处理中'", Integer.class);
    }

    public Integer countOnlineSources() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM data_source WHERE sync_status = 'ONLINE'", Integer.class);
    }

    public Integer countAllSources() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM data_source", Integer.class);
    }

    public List<DeliveryActivity> findRecentActivities() {
        return jdbcTemplate.query(
            "SELECT id, activity_title AS activityTitle, activity_type AS activityType, project_name AS projectName, " +
                "owner_name AS ownerName, activity_status AS activityStatus, occurred_at AS occurredAt, detail_text AS detailText " +
                "FROM delivery_activity ORDER BY occurred_at DESC",
            new BeanPropertyRowMapper<DeliveryActivity>(DeliveryActivity.class)
        );
    }

    public List<Map<String, Object>> findUpcomingMilestones() {
        return jdbcTemplate.queryForList(
            "SELECT p.project_name AS projectName, t.team_name AS teamName, p.delivery_stage AS deliveryStage, " +
                "FORMATDATETIME(p.plan_go_live, 'yyyy-MM-dd') AS planGoLive, p.progress, p.risk_level AS riskLevel " +
                "FROM project p JOIN team t ON p.team_id = t.id ORDER BY p.plan_go_live ASC"
        );
    }

    public List<Map<String, Object>> findMetricOverviewRows() {
        return jdbcTemplate.queryForList(
            "SELECT md.metric_code AS metricCode, md.metric_name AS metricName, md.unit_name AS unitName, " +
                "md.target_value AS targetValue, ms.metric_value AS metricValue, ms.trend_rate AS trendRate, " +
                "ms.warning_level AS warningLevel " +
                "FROM metric_definition md " +
                "JOIN metric_snapshot ms ON md.metric_code = ms.metric_code " +
                "WHERE ms.scope_type = 'ENTERPRISE' " +
                "AND ms.snapshot_month = (SELECT MAX(snapshot_month) FROM metric_snapshot WHERE scope_type = 'ENTERPRISE') " +
                "ORDER BY md.id"
        );
    }

    public List<Map<String, Object>> findMetricTrendRows() {
        return jdbcTemplate.queryForList(
            "SELECT FORMATDATETIME(snapshot_month, 'yyyy-MM') AS snapshotMonth, metric_code AS metricCode, metric_value AS metricValue " +
                "FROM metric_snapshot WHERE scope_type = 'ENTERPRISE' ORDER BY snapshot_month ASC, id ASC"
        );
    }

    public List<Map<String, Object>> findMetricCatalog() {
        return jdbcTemplate.queryForList(
            "SELECT md.metric_code AS metricCode, md.metric_name AS metricName, md.category_name AS categoryName, " +
                "md.unit_name AS unitName, md.target_value AS targetValue, ms.metric_value AS latestValue, " +
                "ms.warning_level AS warningLevel, ms.trend_rate AS trendRate " +
                "FROM metric_definition md " +
                "JOIN metric_snapshot ms ON md.metric_code = ms.metric_code " +
                "WHERE ms.scope_type = 'ENTERPRISE' " +
                "AND ms.snapshot_month = (SELECT MAX(snapshot_month) FROM metric_snapshot WHERE scope_type = 'ENTERPRISE') " +
                "ORDER BY md.id"
        );
    }

    public List<Map<String, Object>> findProjectStatusDistribution() {
        return jdbcTemplate.queryForList(
            "SELECT status AS statusName, COUNT(*) AS totalCount FROM project GROUP BY status ORDER BY totalCount DESC"
        );
    }

    public List<Map<String, Object>> findSourceStatusDistribution() {
        return jdbcTemplate.queryForList(
            "SELECT sync_status AS statusName, COUNT(*) AS totalCount FROM data_source GROUP BY sync_status ORDER BY totalCount DESC"
        );
    }

    public List<Map<String, Object>> findSources() {
        return jdbcTemplate.queryForList(
            "SELECT source_name AS sourceName, source_type AS sourceType, sync_status AS syncStatus, sync_owner AS syncOwner, " +
                "FORMATDATETIME(last_sync_time, 'yyyy-MM-dd HH:mm') AS lastSyncTime, api_health AS apiHealth, " +
                "today_increment AS todayIncrement, remark AS remark " +
                "FROM data_source ORDER BY CASE sync_status WHEN 'ONLINE' THEN 1 WHEN 'WARNING' THEN 2 ELSE 3 END, id"
        );
    }

    public List<WorkItem> findWorkItems() {
        return jdbcTemplate.query(
            "SELECT id, project_code AS projectCode, project_name AS projectName, item_title AS itemTitle, " +
                "item_type AS itemType, item_status AS itemStatus, priority_level AS priorityLevel, owner_name AS ownerName, " +
                "sprint_name AS sprintName, progress, due_date AS dueDate " +
                "FROM work_item ORDER BY CASE priority_level WHEN '高' THEN 1 WHEN '中' THEN 2 ELSE 3 END, due_date ASC",
            new BeanPropertyRowMapper<WorkItem>(WorkItem.class)
        );
    }

    public List<Map<String, Object>> countWorkItemsByStatus() {
        return jdbcTemplate.queryForList(
            "SELECT item_status AS itemName, COUNT(*) AS totalCount FROM work_item GROUP BY item_status ORDER BY totalCount DESC"
        );
    }

    public List<Map<String, Object>> countWorkItemsByPriority() {
        return jdbcTemplate.queryForList(
            "SELECT priority_level AS itemName, COUNT(*) AS totalCount FROM work_item GROUP BY priority_level ORDER BY totalCount DESC"
        );
    }

    public List<Map<String, Object>> findProjectBoard() {
        return jdbcTemplate.queryForList(
            "SELECT p.project_code AS projectCode, p.project_name AS projectName, t.team_name AS teamName, " +
                "p.manager_name AS managerName, p.business_domain AS businessDomain, p.status, p.delivery_stage AS deliveryStage, " +
                "p.progress, p.risk_level AS riskLevel, p.current_version AS currentVersion, " +
                "FORMATDATETIME(p.plan_go_live, 'yyyy-MM-dd') AS planGoLive, " +
                "ROUND(MAX(CASE WHEN ms.metric_code = 'REQ_DELIVERY_CYCLE' THEN ms.metric_value END), 1) AS deliveryCycle, " +
                "ROUND(MAX(CASE WHEN ms.metric_code = 'DEPLOY_FREQUENCY' THEN ms.metric_value END), 1) AS deployFrequency, " +
                "ROUND(MAX(CASE WHEN ms.metric_code = 'CHANGE_FAILURE_RATE' THEN ms.metric_value END), 1) AS changeFailureRate, " +
                "ROUND(MAX(CASE WHEN ms.metric_code = 'MTTR' THEN ms.metric_value END), 1) AS mttr " +
                "FROM project p " +
                "JOIN team t ON t.id = p.team_id " +
                "LEFT JOIN metric_snapshot ms ON ms.scope_type = 'PROJECT' AND ms.scope_name = p.project_code " +
                "AND ms.snapshot_month = (SELECT MAX(snapshot_month) FROM metric_snapshot WHERE scope_type = 'PROJECT') " +
                "GROUP BY p.project_code, p.project_name, t.team_name, p.manager_name, p.business_domain, p.status, " +
                "p.delivery_stage, p.progress, p.risk_level, p.current_version, p.plan_go_live " +
                "ORDER BY CASE p.risk_level WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 ELSE 3 END, p.progress DESC"
        );
    }

    public List<Map<String, Object>> findReports() {
        return jdbcTemplate.queryForList(
            "SELECT report_name AS reportName, report_cycle AS reportCycle, report_type AS reportType, scope_name AS scopeName, " +
                "owner_name AS ownerName, FORMATDATETIME(generated_at, 'yyyy-MM-dd HH:mm') AS generatedAt, " +
                "quality_score AS qualityScore, status FROM report_record ORDER BY generated_at DESC"
        );
    }

    public List<Map<String, Object>> findReportScoreTrend() {
        return jdbcTemplate.queryForList(
            "SELECT FORMATDATETIME(generated_at, 'MM-dd') AS reportDay, quality_score AS qualityScore " +
                "FROM report_record ORDER BY generated_at ASC"
        );
    }

    public List<Map<String, Object>> findAlerts() {
        return jdbcTemplate.queryForList(
            "SELECT project_name AS projectName, alert_name AS alertName, alert_level AS alertLevel, status, impact_scope AS impactScope, " +
                "FORMATDATETIME(triggered_at, 'yyyy-MM-dd HH:mm') AS triggeredAt, " +
                "COALESCE(FORMATDATETIME(recovered_at, 'yyyy-MM-dd HH:mm'), '处理中') AS recoveredAt, owner_name AS ownerName " +
                "FROM alert_event ORDER BY triggered_at DESC"
        );
    }

    public List<Map<String, Object>> countAlertsByLevel() {
        return jdbcTemplate.queryForList(
            "SELECT alert_level AS itemName, COUNT(*) AS totalCount FROM alert_event GROUP BY alert_level ORDER BY alert_level ASC"
        );
    }
}
