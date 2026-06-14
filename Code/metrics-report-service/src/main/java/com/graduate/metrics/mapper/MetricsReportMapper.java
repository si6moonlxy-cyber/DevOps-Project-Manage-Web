package com.graduate.metrics.mapper;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class MetricsReportMapper {

    private final JdbcTemplate jdbcTemplate;

    public MetricsReportMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> findMetricCards() {
        return jdbcTemplate.queryForList(
            "SELECT md.metric_code AS metricCode, md.metric_name AS metricName, md.unit_name AS unitName, " +
                "COALESCE(md.warning_threshold, 0) AS targetValue, mr.metric_value AS metricValue, " +
                "COALESCE(mr.trend_value, 0) AS trendRate, mr.warning_level AS warningLevel " +
                "FROM metric_definition md " +
                "JOIN metric_result mr ON mr.id = (" +
                    "SELECT MAX(mr2.id) FROM metric_result mr2 " +
                    "WHERE mr2.metric_id = md.id AND mr2.result_key = 'ENTERPRISE'" +
                ") " +
                "WHERE md.is_enabled = 1 AND md.is_core_metric = 1 " +
                "ORDER BY md.id"
        );
    }

    public List<Map<String, Object>> findMetricTrend() {
        return jdbcTemplate.queryForList(
            "SELECT DATE_FORMAT(mr.stat_period_end, '%Y-%m') AS snapshotMonth, " +
                "md.metric_code AS metricCode, mr.metric_value AS metricValue " +
                "FROM metric_result mr " +
                "JOIN metric_definition md ON md.id = mr.metric_id " +
                "WHERE md.is_enabled = 1 AND md.is_core_metric = 1 AND mr.result_key = 'ENTERPRISE' " +
                "ORDER BY mr.stat_period_end ASC, md.id ASC"
        );
    }

    public List<Map<String, Object>> findReports() {
        return jdbcTemplate.queryForList(
            "SELECT rr.report_name AS reportName, rt.report_cycle AS reportCycle, " +
                "COALESCE(rt.template_name, rt.template_code) AS reportType, " +
                "CASE rr.scope_type " +
                    "WHEN 'ENTERPRISE' THEN '企业级' " +
                    "WHEN 'DEPT' THEN CONCAT('部门 / ', COALESCE(d.dept_name, CAST(rr.scope_object_id AS CHAR))) " +
                    "WHEN 'TEAM' THEN CONCAT('团队 / ', COALESCE(t.team_name, CAST(rr.scope_object_id AS CHAR))) " +
                    "ELSE CONCAT('项目 / ', COALESCE(p.project_name, CAST(rr.scope_object_id AS CHAR))) END AS scopeName, " +
                "COALESCE(owner.real_name, rr.generated_by, '调度服务') AS ownerName, " +
                "DATE_FORMAT(rr.generated_at, '%Y-%m-%d %H:%i') AS generatedAt, " +
                "COALESCE(JSON_UNQUOTE(JSON_EXTRACT(rr.preview_json, '$.qualityScore')), '90') AS qualityScore, " +
                "rr.status AS status " +
                "FROM report_record rr " +
                "LEFT JOIN report_template rt ON rt.id = rr.template_id " +
                "LEFT JOIN project p ON p.id = COALESCE(rr.project_id, rr.scope_object_id) " +
                "LEFT JOIN org_department d ON d.id = rr.scope_object_id " +
                "LEFT JOIN team t ON t.id = rr.scope_object_id " +
                "LEFT JOIN sys_user owner ON owner.id = rr.generated_by " +
                "ORDER BY rr.generated_at DESC"
        );
    }

    public Integer countReports() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM report_record", Integer.class);
    }

    public Integer countMetricAlerts() {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM metric_alert_record WHERE status <> 'CLOSED'",
            Integer.class
        );
    }

    public List<Map<String, Object>> findReportScoreTrend() {
        return jdbcTemplate.queryForList(
            "SELECT DATE_FORMAT(generated_at, '%m-%d') AS reportDay, " +
                "COALESCE(JSON_UNQUOTE(JSON_EXTRACT(preview_json, '$.qualityScore')), '90') AS qualityScore " +
                "FROM report_record ORDER BY generated_at ASC"
        );
    }

    public List<Map<String, Object>> findMetricAlerts() {
        return jdbcTemplate.queryForList(
            "SELECT md.metric_name AS metricName, mar.warning_level AS warningLevel, mar.alert_message AS alertMessage, " +
                "COALESCE(p.project_name, '企业级汇总') AS scopeName, " +
                "DATE_FORMAT(mar.triggered_at, '%Y-%m-%d %H:%i') AS triggeredAt, mar.status AS status " +
                "FROM metric_alert_record mar " +
                "JOIN metric_definition md ON md.id = mar.metric_id " +
                "LEFT JOIN project p ON p.id = mar.project_id " +
                "ORDER BY mar.triggered_at DESC " +
                "LIMIT 10"
        );
    }
}
