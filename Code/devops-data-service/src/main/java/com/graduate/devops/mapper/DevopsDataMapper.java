package com.graduate.devops.mapper;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class DevopsDataMapper {

    private final JdbcTemplate jdbcTemplate;

    public DevopsDataMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Integer countSources() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM data_source", Integer.class);
    }

    public Integer countOnlineSources() {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM data_source WHERE status = 'ACTIVE'",
            Integer.class
        );
    }

    public Integer countCollectionTasks() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM collect_job", Integer.class);
    }

    public Integer countEnabledQualityRules() {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM data_quality_rule WHERE is_enabled = 1",
            Integer.class
        );
    }

    public Integer countOpenQualityIssues() {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM data_quality_log WHERE handle_status = 'OPEN'",
            Integer.class
        );
    }

    public List<Map<String, Object>> findSources() {
        return jdbcTemplate.queryForList(
            "SELECT ds.id AS sourceId, ds.endpoint_url AS endpointUrl, ds.status AS sourceStatus, ds.source_name AS sourceName, ds.source_type AS sourceType, " +
                "CASE " +
                    "WHEN ds.status <> 'ACTIVE' THEN 'OFFLINE' " +
                    "WHEN COALESCE(logs.failed_runs, 0) > 0 THEN 'WARNING' " +
                    "ELSE 'ONLINE' END AS syncStatus, " +
                "COALESCE(MAX(owner.real_name), '平台采集服务') AS syncOwner, " +
                "COALESCE(DATE_FORMAT(COALESCE(logs.latest_finished_at, ds.last_check_at), '%Y-%m-%d %H:%i'), '--') AS lastSyncTime, " +
                "COALESCE(logs.api_health, CASE WHEN ds.status = 'ACTIVE' THEN 98 ELSE 60 END) AS apiHealth, " +
                "COALESCE(logs.today_increment, 0) AS todayIncrement, " +
                "COALESCE(ds.remark, '已接入共享 MySQL 总库') AS remark " +
                "FROM data_source ds " +
                "LEFT JOIN data_source_project dsp ON dsp.data_source_id = ds.id AND dsp.status = 'ACTIVE' " +
                "LEFT JOIN project p ON p.id = dsp.project_id " +
                "LEFT JOIN sys_user owner ON owner.id = p.manager_user_id " +
                "LEFT JOIN (" +
                    "SELECT data_source_id, " +
                        "MAX(finished_at) AS latest_finished_at, " +
                        "SUM(CASE WHEN DATE(created_at) = CURDATE() THEN success_count ELSE 0 END) AS today_increment, " +
                        "SUM(CASE WHEN status = 'FAILED' AND created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) THEN 1 ELSE 0 END) AS failed_runs, " +
                        "ROUND(AVG(CASE status WHEN 'SUCCESS' THEN 98 WHEN 'RUNNING' THEN 85 ELSE 66 END), 0) AS api_health " +
                    "FROM collect_log GROUP BY data_source_id" +
                ") logs ON logs.data_source_id = ds.id " +
                "GROUP BY ds.id, ds.source_name, ds.source_type, ds.status, ds.last_check_at, ds.remark, " +
                    "logs.latest_finished_at, logs.today_increment, logs.failed_runs, logs.api_health " +
                "ORDER BY ds.id"
        );
    }

    public List<Map<String, Object>> findCollectionTasks() {
        return jdbcTemplate.queryForList(
            "SELECT cj.job_name AS taskName, ds.source_name AS sourceName, " +
                "COALESCE(cj.cron_expression, cj.schedule_type) AS scheduleExpr, " +
                "CASE " +
                    "WHEN last_log.status = 'FAILED' THEN 'WARNING' " +
                    "WHEN cj.status IN ('RUNNING', 'ACTIVE', 'ENABLED') THEN 'RUNNING' " +
                    "ELSE 'OFFLINE' END AS taskStatus, " +
                "COALESCE(DATE_FORMAT(cj.last_collect_at, '%Y-%m-%d %H:%i'), '--') AS lastRunTime, " +
                "COALESCE(DATE_FORMAT(cj.next_collect_at, '%Y-%m-%d %H:%i'), '--') AS nextRunTime " +
                "FROM collect_job cj " +
                "JOIN data_source ds ON ds.id = cj.data_source_id " +
                "LEFT JOIN collect_log last_log ON last_log.id = (" +
                    "SELECT MAX(cl.id) FROM collect_log cl WHERE cl.job_id = cj.id" +
                ") " +
                "ORDER BY cj.id"
        );
    }

    public List<Map<String, Object>> findQualityChecks() {
        return jdbcTemplate.queryForList(
            "SELECT ds.source_name AS sourceName, dqr.rule_name AS ruleName, dqr.target_object AS targetObject, " +
                "dqr.severity AS severity, COALESCE(issues.issue_count, 0) AS issueCount, " +
                "COALESCE(DATE_FORMAT(issues.latest_detected_at, '%Y-%m-%d %H:%i'), '--') AS latestDetectedAt, " +
                "CASE WHEN dqr.is_enabled = 1 THEN '启用' ELSE '停用' END AS enabledStatus " +
                "FROM data_quality_rule dqr " +
                "LEFT JOIN data_source ds ON ds.id = dqr.data_source_id " +
                "LEFT JOIN (" +
                    "SELECT rule_id, COUNT(*) AS issue_count, MAX(created_at) AS latest_detected_at " +
                    "FROM data_quality_log GROUP BY rule_id" +
                ") issues ON issues.rule_id = dqr.id " +
                "ORDER BY dqr.id"
        );
    }

    public List<Map<String, Object>> countSourceStatus() {
        return jdbcTemplate.queryForList(
            "SELECT CASE " +
                "WHEN ds.status <> 'ACTIVE' THEN 'OFFLINE' " +
                "WHEN COALESCE(logs.failed_runs, 0) > 0 THEN 'WARNING' " +
                "ELSE 'ONLINE' END AS itemName, " +
                "COUNT(*) AS totalCount " +
                "FROM data_source ds " +
                "LEFT JOIN (" +
                    "SELECT data_source_id, " +
                        "SUM(CASE WHEN status = 'FAILED' AND created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) THEN 1 ELSE 0 END) AS failed_runs " +
                    "FROM collect_log GROUP BY data_source_id" +
                ") logs ON logs.data_source_id = ds.id " +
                "GROUP BY itemName ORDER BY totalCount DESC"
        );
    }

    public void updateSource(Long sourceId, Map<String, Object> request) {
        jdbcTemplate.update(
            "UPDATE data_source SET endpoint_url = ?, status = ?, remark = ?, updated_at = NOW() WHERE id = ?",
            value(request, "endpointUrl", ""),
            booleanValue(request.get("visible")) ? "ACTIVE" : "INACTIVE",
            value(request, "remark", "Updated from portal"),
            sourceId
        );
    }

    private String value(Map<String, Object> request, String key, String defaultValue) {
        Object value = request.get(key);
        if (value == null || String.valueOf(value).trim().isEmpty()) {
            return defaultValue;
        }
        return String.valueOf(value).trim();
    }

    private boolean booleanValue(Object value) {
        if (value == null) {
            return false;
        }
        String text = String.valueOf(value).trim();
        return "true".equalsIgnoreCase(text) || "1".equals(text) || "ACTIVE".equalsIgnoreCase(text);
    }
}
