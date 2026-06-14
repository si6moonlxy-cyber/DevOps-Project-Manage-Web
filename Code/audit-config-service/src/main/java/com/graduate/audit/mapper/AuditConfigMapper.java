package com.graduate.audit.mapper;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class AuditConfigMapper {

    private final JdbcTemplate jdbcTemplate;

    public AuditConfigMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Integer countAlerts() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM alert_event", Integer.class);
    }

    public Integer countActiveAlerts() {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM alert_event WHERE alert_status <> 'RECOVERED'",
            Integer.class
        );
    }

    public Integer countConfigItems() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_config", Integer.class);
    }

    public Integer countLoginLogs() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM login_log", Integer.class);
    }

    public List<Map<String, Object>> findAlerts() {
        return jdbcTemplate.queryForList(
            "SELECT COALESCE(p.project_name, '企业级平台') AS domainName, ae.alert_name AS alertName, ae.alert_level AS alertLevel, " +
                "CASE " +
                    "WHEN ae.alert_status = 'RECOVERED' THEN '已恢复' " +
                    "WHEN ae.alert_status = 'ACKED' THEN '处理中' " +
                    "ELSE '处理中' END AS eventStatus, " +
                "CONCAT(COALESCE(env.env_name, '全链路'), ' / ', COALESCE(ds.source_name, ae.alert_source, '统一监控')) AS impactScope, " +
                "DATE_FORMAT(ae.triggered_at_source, '%Y-%m-%d %H:%i') AS triggeredAt, " +
                "COALESCE(DATE_FORMAT(ae.recovered_at_source, '%Y-%m-%d %H:%i'), '未恢复') AS recoveredAt, " +
                "COALESCE(manager.real_name, '平台值班经理') AS ownerName, " +
                "COALESCE(ds.source_name, ae.alert_source, 'Prometheus') AS sourceService " +
                "FROM alert_event ae " +
                "LEFT JOIN project p ON p.id = ae.project_id " +
                "LEFT JOIN sys_user manager ON manager.id = p.manager_user_id " +
                "LEFT JOIN deploy_environment env ON env.id = ae.environment_id " +
                "LEFT JOIN data_source ds ON ds.id = ae.data_source_id " +
                "ORDER BY ae.triggered_at_source DESC"
        );
    }

    public List<Map<String, Object>> countAlertLevel() {
        return jdbcTemplate.queryForList(
            "SELECT alert_level AS itemName, COUNT(*) AS totalCount " +
                "FROM alert_event GROUP BY alert_level ORDER BY alert_level"
        );
    }

    public List<Map<String, Object>> findOperationLogs() {
        return jdbcTemplate.queryForList(
            "SELECT username AS operatorName, module_name AS operatorRole, operation_type AS operationType, " +
                "request_uri AS targetName, result_code AS operationResult, result_message AS operationDetail, " +
                "DATE_FORMAT(operated_at, '%Y-%m-%d %H:%i') AS occurredAt " +
                "FROM operation_log ORDER BY operated_at DESC"
        );
    }

    public List<Map<String, Object>> findLoginLogs() {
        return jdbcTemplate.queryForList(
            "SELECT ll.username AS username, COALESCE(r.role_name, '未分配角色') AS roleName, ll.login_status AS loginStatus, " +
                "ll.ip_address AS ipAddress, COALESCE(ll.user_agent, '浏览器登录') AS userAgent, " +
                "COALESCE(ll.failure_reason, '登录成功') AS failureReason, " +
                "DATE_FORMAT(ll.login_at, '%Y-%m-%d %H:%i') AS loginAt " +
                "FROM login_log ll " +
                "LEFT JOIN sys_user_role ur ON ur.user_id = ll.user_id " +
                "LEFT JOIN sys_role r ON r.id = ur.role_id " +
                "ORDER BY ll.login_at DESC"
        );
    }

    public List<Map<String, Object>> findConfigItems() {
        return jdbcTemplate.queryForList(
            "SELECT config_group AS configGroup, config_key AS configKey, config_value AS configValue, " +
                "CASE WHEN is_builtin = 1 THEN '系统内置' ELSE '人工维护' END AS configState, " +
                "COALESCE(remark, config_name) AS description, " +
                "DATE_FORMAT(updated_at, '%Y-%m-%d %H:%i') AS updatedAt " +
                "FROM sys_config ORDER BY id"
        );
    }

    public Integer countRows(String tableName) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class);
    }

    public Integer countColumns(String tableName) {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?",
            Integer.class,
            tableName
        );
    }

    public List<Map<String, Object>> executeReadOnlyQuery(String sql) {
        return jdbcTemplate.queryForList(sql);
    }

    public String findLatestUpdateTime() {
        return jdbcTemplate.queryForObject(
            "SELECT DATE_FORMAT(MAX(updated_at), '%Y-%m-%d %H:%i') FROM sys_config",
            String.class
        );
    }
}
