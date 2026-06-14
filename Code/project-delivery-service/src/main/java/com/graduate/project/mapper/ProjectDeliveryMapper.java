package com.graduate.project.mapper;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class ProjectDeliveryMapper {

    private final JdbcTemplate jdbcTemplate;

    public ProjectDeliveryMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Integer countProjects() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM project WHERE COALESCE(visibility_scope, '') <> 'ARCHIVED'", Integer.class);
    }

    public Integer countHighRiskProjects() {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM (" +
                "SELECT p.id, " +
                "COALESCE(wi.overdue_count, 0) AS overdueCount, " +
                "COALESCE(ae.p1_count, 0) AS p1Count " +
                "FROM project p " +
                "LEFT JOIN (" +
                    "SELECT project_id, SUM(CASE WHEN status NOT IN ('DONE', 'CLOSED', 'RESOLVED') AND created_at_source < DATE_SUB(NOW(), INTERVAL 20 DAY) THEN 1 ELSE 0 END) AS overdue_count " +
                    "FROM work_item GROUP BY project_id" +
                ") wi ON wi.project_id = p.id " +
                "LEFT JOIN (" +
                    "SELECT project_id, SUM(CASE WHEN alert_status <> 'RECOVERED' AND alert_level = 'P1' THEN 1 ELSE 0 END) AS p1_count " +
                    "FROM alert_event GROUP BY project_id" +
                ") ae ON ae.project_id = p.id" +
            " WHERE COALESCE(p.visibility_scope, '') <> 'ARCHIVED') risk_summary WHERE p1Count > 0 OR overdueCount >= 2",
            Integer.class
        );
    }

    public Integer countCompletedItems() {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM work_item WHERE status IN ('DONE', 'CLOSED', 'RESOLVED')",
            Integer.class
        );
    }

    public Integer countWorkItems() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM work_item", Integer.class);
    }

    public Integer countOverdueItems() {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM work_item WHERE status NOT IN ('DONE', 'CLOSED', 'RESOLVED') " +
                "AND created_at_source < DATE_SUB(NOW(), INTERVAL 20 DAY)",
            Integer.class
        );
    }

    public List<Map<String, Object>> findProjectRows() {
        return jdbcTemplate.queryForList(
            "SELECT p.id AS projectId, p.project_code AS projectCode, p.project_status AS statusCode, p.project_name AS projectName, " +
                "COALESCE(manager.real_name, '待分配') AS managerName, " +
                "COALESCE(p.project_type, pl.line_name, '未分类') AS businessDomain, " +
                "CASE p.project_status " +
                    "WHEN 'PLANNING' THEN '规划中' " +
                    "WHEN 'RUNNING' THEN '执行中' " +
                    "WHEN 'ONLINE' THEN '稳定运行' " +
                    "ELSE '已关闭' END AS status, " +
                "CASE COALESCE(rv.version_status, 'PLANNED') " +
                    "WHEN 'PLANNED' THEN '版本规划' " +
                    "WHEN 'TESTING' THEN '测试验证' " +
                    "WHEN 'RELEASED' THEN '已发布' " +
                    "ELSE '归档完成' END AS deliveryStage, " +
                "COALESCE(wi.progress, 0) AS progress, " +
                "CASE " +
                    "WHEN COALESCE(ae.p1_count, 0) > 0 OR COALESCE(wi.overdue_count, 0) >= 2 THEN 'HIGH' " +
                    "WHEN COALESCE(ae.p2_count, 0) > 0 OR COALESCE(wi.overdue_count, 0) > 0 THEN 'MEDIUM' " +
                    "ELSE 'LOW' END AS riskLevel, " +
                "COALESCE(rv.version_name, '未发布') AS currentVersion, " +
                "COALESCE(DATE_FORMAT(rv.planned_release_date, '%Y-%m-%d'), '--') AS planGoLive " +
                "FROM project p " +
                "LEFT JOIN sys_user manager ON manager.id = p.manager_user_id " +
                "LEFT JOIN product_line pl ON pl.id = p.product_line_id " +
                "LEFT JOIN (" +
                    "SELECT rv.project_id, rv.version_name, rv.version_status, rv.planned_release_date " +
                    "FROM release_version rv " +
                    "JOIN (SELECT project_id, MAX(id) AS max_id FROM release_version GROUP BY project_id) latest ON latest.max_id = rv.id" +
                ") rv ON rv.project_id = p.id " +
                "LEFT JOIN (" +
                    "SELECT project_id, " +
                        "ROUND(IFNULL(SUM(CASE WHEN status IN ('DONE', 'CLOSED', 'RESOLVED') THEN 1 ELSE 0 END) / NULLIF(COUNT(*), 0) * 100, 0), 0) AS progress, " +
                        "SUM(CASE WHEN status NOT IN ('DONE', 'CLOSED', 'RESOLVED') AND created_at_source < DATE_SUB(NOW(), INTERVAL 20 DAY) THEN 1 ELSE 0 END) AS overdue_count " +
                    "FROM work_item GROUP BY project_id" +
                ") wi ON wi.project_id = p.id " +
                "LEFT JOIN (" +
                    "SELECT project_id, " +
                        "SUM(CASE WHEN alert_status <> 'RECOVERED' AND alert_level = 'P1' THEN 1 ELSE 0 END) AS p1_count, " +
                        "SUM(CASE WHEN alert_status <> 'RECOVERED' AND alert_level = 'P2' THEN 1 ELSE 0 END) AS p2_count " +
                    "FROM alert_event GROUP BY project_id" +
                ") ae ON ae.project_id = p.id " +
                "WHERE COALESCE(p.visibility_scope, '') <> 'ARCHIVED' " +
                "ORDER BY progress DESC, p.id ASC"
        );
    }

    public List<Map<String, Object>> findMilestones() {
        return jdbcTemplate.queryForList(
            "SELECT p.project_name AS projectName, " +
                "CASE COALESCE(rv.version_status, 'PLANNED') " +
                    "WHEN 'PLANNED' THEN '版本规划' " +
                    "WHEN 'TESTING' THEN '测试验证' " +
                    "WHEN 'RELEASED' THEN '已发布' " +
                    "ELSE '归档完成' END AS deliveryStage, " +
                "COALESCE(wi.progress, 0) AS progress, " +
                "COALESCE(DATE_FORMAT(rv.planned_release_date, '%Y-%m-%d'), '--') AS planGoLive " +
                "FROM project p " +
                "LEFT JOIN (" +
                    "SELECT rv.project_id, rv.version_status, rv.planned_release_date " +
                    "FROM release_version rv " +
                    "JOIN (SELECT project_id, MAX(id) AS max_id FROM release_version GROUP BY project_id) latest ON latest.max_id = rv.id" +
                ") rv ON rv.project_id = p.id " +
                "LEFT JOIN (" +
                    "SELECT project_id, " +
                        "ROUND(IFNULL(SUM(CASE WHEN status IN ('DONE', 'CLOSED', 'RESOLVED') THEN 1 ELSE 0 END) / NULLIF(COUNT(*), 0) * 100, 0), 0) AS progress " +
                    "FROM work_item GROUP BY project_id" +
                ") wi ON wi.project_id = p.id " +
                "WHERE COALESCE(p.visibility_scope, '') <> 'ARCHIVED' " +
                "ORDER BY rv.planned_release_date ASC, p.id ASC"
        );
    }

    public List<Map<String, Object>> findActivities() {
        return jdbcTemplate.queryForList(
            "SELECT activityTitle, activityType, projectName, ownerName, activityStatus, " +
                "DATE_FORMAT(occurredAt, '%Y-%m-%d %H:%i') AS occurredAt, detailText " +
                "FROM (" +
                    "SELECT CONCAT('版本 ', rv.version_name, ' 进入 ', rv.version_status) AS activityTitle, " +
                        "'版本推进' AS activityType, p.project_name AS projectName, COALESCE(manager.real_name, '系统管理员') AS ownerName, " +
                        "CASE rv.version_status WHEN 'RELEASED' THEN '已完成' WHEN 'TESTING' THEN '处理中' ELSE '待处理' END AS activityStatus, " +
                        "COALESCE(TIMESTAMP(rv.actual_release_date), TIMESTAMP(rv.planned_release_date)) AS occurredAt, " +
                        "CONCAT('计划发布时间 ', DATE_FORMAT(rv.planned_release_date, '%Y-%m-%d')) AS detailText " +
                    "FROM release_version rv " +
                    "JOIN project p ON p.id = rv.project_id " +
                    "LEFT JOIN sys_user manager ON manager.id = p.manager_user_id " +
                    "UNION ALL " +
                    "SELECT CONCAT('部署 ', COALESCE(rv.version_name, dr.external_id), ' 至 ', env.env_name) AS activityTitle, " +
                        "'部署动态' AS activityType, p.project_name AS projectName, COALESCE(dr.deployed_by, manager.real_name, '发布系统') AS ownerName, " +
                        "CASE dr.status WHEN 'SUCCESS' THEN '已完成' WHEN 'RUNNING' THEN '处理中' ELSE '异常' END AS activityStatus, " +
                        "COALESCE(dr.finished_at_source, dr.started_at_source) AS occurredAt, " +
                        "CONCAT('方式 ', dr.deploy_type, ' / ', IF(dr.is_rollback = 1, '回滚发布', '正常发布')) AS detailText " +
                    "FROM deployment_record dr " +
                    "JOIN project p ON p.id = dr.project_id " +
                    "JOIN deploy_environment env ON env.id = dr.environment_id " +
                    "LEFT JOIN release_version rv ON rv.id = dr.release_version_id " +
                    "LEFT JOIN sys_user manager ON manager.id = p.manager_user_id " +
                    "UNION ALL " +
                    "SELECT CONCAT('构建 ', br.pipeline_name, ' #', COALESCE(br.build_number, br.external_id)) AS activityTitle, " +
                        "'构建结果' AS activityType, p.project_name AS projectName, COALESCE(br.triggered_by, manager.real_name, '流水线') AS ownerName, " +
                        "CASE br.status WHEN 'SUCCESS' THEN '已完成' WHEN 'RUNNING' THEN '处理中' ELSE '异常' END AS activityStatus, " +
                        "COALESCE(br.finished_at_source, br.started_at_source) AS occurredAt, " +
                        "CONCAT('耗时 ', COALESCE(br.duration_seconds, 0), ' 秒') AS detailText " +
                    "FROM build_record br " +
                    "JOIN project p ON p.id = br.project_id " +
                    "LEFT JOIN sys_user manager ON manager.id = p.manager_user_id " +
                    "UNION ALL " +
                    "SELECT CONCAT('工作项 ', wi.title) AS activityTitle, " +
                        "'工作项更新' AS activityType, p.project_name AS projectName, COALESCE(wi.owner_name, wi.creator_name, manager.real_name, '未分配') AS ownerName, " +
                        "CASE WHEN wi.status IN ('DONE', 'CLOSED', 'RESOLVED') THEN '已完成' ELSE '处理中' END AS activityStatus, " +
                        "COALESCE(wi.delivered_at_source, wi.resolved_at_source, wi.started_at_source, wi.created_at_source) AS occurredAt, " +
                        "CONCAT(wi.work_item_type, ' / 状态 ', wi.status) AS detailText " +
                    "FROM work_item wi " +
                    "JOIN project p ON p.id = wi.project_id " +
                    "LEFT JOIN sys_user manager ON manager.id = p.manager_user_id " +
                ") activity_feed " +
                "WHERE occurredAt IS NOT NULL " +
                "ORDER BY occurredAt DESC " +
                "LIMIT 10"
        );
    }

    public List<Map<String, Object>> findWorkItems() {
        return jdbcTemplate.queryForList(
            "SELECT wi.id AS itemId, wi.project_id AS projectId, wi.status AS statusCode, wi.work_item_type AS itemTypeCode, p.project_name AS projectName, wi.title AS itemTitle, " +
                "CASE wi.work_item_type " +
                    "WHEN 'REQUIREMENT' THEN '需求' " +
                    "WHEN 'TASK' THEN '任务' " +
                    "ELSE '缺陷' END AS itemType, " +
                "CASE " +
                    "WHEN wi.status IN ('DONE', 'CLOSED', 'RESOLVED') THEN '已完成' " +
                    "WHEN wi.status IN ('TESTING', 'VERIFYING') THEN '测试中' " +
                    "WHEN wi.status IN ('IN_PROGRESS', 'DOING', 'RUNNING') THEN '进行中' " +
                    "WHEN wi.status IN ('OPEN', 'TODO', 'NEW') THEN '待处理' " +
                    "ELSE '风险中' END AS itemStatus, " +
                "CASE UPPER(COALESCE(wi.priority, 'MEDIUM')) " +
                    "WHEN 'P0' THEN '高' " +
                    "WHEN 'P1' THEN '高' " +
                    "WHEN 'HIGH' THEN '高' " +
                    "WHEN 'MEDIUM' THEN '中' " +
                    "WHEN 'P2' THEN '中' " +
                    "ELSE '低' END AS priorityLevel, " +
                "COALESCE(wi.owner_name, wi.creator_name, '未分配') AS ownerName, " +
                "COALESCE(rv.version_name, CONCAT('批次-', COALESCE(wi.etl_batch_no, 'N/A'))) AS sprintName, " +
                "CASE " +
                    "WHEN wi.status IN ('DONE', 'CLOSED', 'RESOLVED') THEN 100 " +
                    "WHEN wi.status IN ('TESTING', 'VERIFYING') THEN 85 " +
                    "WHEN wi.status IN ('IN_PROGRESS', 'DOING', 'RUNNING') THEN 65 " +
                    "WHEN wi.status IN ('OPEN', 'TODO', 'NEW') THEN 30 " +
                    "ELSE 45 END AS progress, " +
                "DATE_FORMAT(COALESCE(wi.delivered_at_source, wi.resolved_at_source, wi.started_at_source, wi.created_at_source), '%Y-%m-%d') AS dueDate " +
                "FROM work_item wi " +
                "JOIN project p ON p.id = wi.project_id " +
                "LEFT JOIN release_version rv ON rv.id = wi.release_version_id " +
                "ORDER BY CASE UPPER(COALESCE(wi.priority, 'MEDIUM')) " +
                    "WHEN 'P0' THEN 1 WHEN 'P1' THEN 1 WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 WHEN 'P2' THEN 2 ELSE 3 END, " +
                    "COALESCE(wi.delivered_at_source, wi.created_at_source) DESC"
        );
    }

    public List<Map<String, Object>> countProjectStatus() {
        return jdbcTemplate.queryForList(
            "SELECT CASE project_status " +
                "WHEN 'PLANNING' THEN '规划中' " +
                "WHEN 'RUNNING' THEN '执行中' " +
                "WHEN 'ONLINE' THEN '稳定运行' " +
                "ELSE '已关闭' END AS itemName, " +
                "COUNT(*) AS totalCount " +
                "FROM project WHERE COALESCE(visibility_scope, '') <> 'ARCHIVED' GROUP BY itemName ORDER BY totalCount DESC"
        );
    }

    public List<Map<String, Object>> countWorkItemStatus() {
        return jdbcTemplate.queryForList(
            "SELECT CASE " +
                "WHEN status IN ('DONE', 'CLOSED', 'RESOLVED') THEN '已完成' " +
                "WHEN status IN ('TESTING', 'VERIFYING') THEN '测试中' " +
                "WHEN status IN ('IN_PROGRESS', 'DOING', 'RUNNING') THEN '进行中' " +
                "WHEN status IN ('OPEN', 'TODO', 'NEW') THEN '待处理' " +
                "ELSE '风险中' END AS itemName, " +
                "COUNT(*) AS totalCount " +
                "FROM work_item GROUP BY itemName ORDER BY totalCount DESC"
        );
    }
    public void insertProject(Map<String, Object> request) {
        jdbcTemplate.update(
            "INSERT INTO project (dept_id, team_id, product_line_id, project_code, project_name, manager_user_id, project_type, project_status, description, planned_start_date, planned_end_date, visibility_scope, created_at, updated_at) " +
                "SELECT COALESCE(MIN(t.dept_id), 1), COALESCE(MIN(t.id), 1), (SELECT MIN(id) FROM product_line), ?, ?, (SELECT MIN(id) FROM sys_user WHERE status = 'ACTIVE'), ?, ?, ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 60 DAY), 'ENTERPRISE', NOW(), NOW() FROM team t",
            value(request, "projectCode", "PRJ" + System.currentTimeMillis()),
            value(request, "projectName", "New Project"),
            value(request, "businessDomain", "ENTERPRISE"),
            value(request, "statusCode", "PLANNING"),
            value(request, "description", "Created from portal")
        );
    }

    public void updateProject(Long projectId, Map<String, Object> request) {
        jdbcTemplate.update(
            "UPDATE project SET project_code = ?, project_name = ?, project_type = ?, project_status = ?, description = ?, updated_at = NOW() WHERE id = ?",
            value(request, "projectCode", "PRJ" + projectId),
            value(request, "projectName", "Project " + projectId),
            value(request, "businessDomain", "ENTERPRISE"),
            value(request, "statusCode", "RUNNING"),
            value(request, "description", "Updated from portal"),
            projectId
        );
    }

    public int deleteProject(Long projectId) {
        return jdbcTemplate.update("UPDATE project SET project_status = 'CLOSED', visibility_scope = 'ARCHIVED', updated_at = NOW() WHERE id = ?", projectId);
    }

    public void insertWorkItem(Map<String, Object> request) {
        String externalId = "MANUAL-" + System.currentTimeMillis();
        jdbcTemplate.update(
            "INSERT INTO work_item (data_source_id, project_id, team_id, external_id, external_key, work_item_type, title, priority, status, owner_name, creator_name, created_at_source, etl_batch_no, raw_payload, created_at, updated_at) " +
                "SELECT (SELECT MIN(id) FROM data_source), ?, p.team_id, ?, ?, ?, ?, ?, ?, ?, 'portal', NOW(), 'MANUAL', JSON_OBJECT('source','portal'), NOW(), NOW() FROM project p WHERE p.id = ?",
            longValue(request.get("projectId"), 1L),
            externalId,
            externalId,
            value(request, "itemTypeCode", "TASK"),
            value(request, "itemTitle", "New Work Item"),
            value(request, "priority", "MEDIUM"),
            value(request, "statusCode", "TODO"),
            value(request, "ownerName", "Unassigned"),
            longValue(request.get("projectId"), 1L)
        );
    }

    public void updateWorkItem(Long itemId, Map<String, Object> request) {
        jdbcTemplate.update(
            "UPDATE work_item SET project_id = ?, work_item_type = ?, title = ?, priority = ?, status = ?, owner_name = ?, updated_at = NOW() WHERE id = ?",
            longValue(request.get("projectId"), 1L),
            value(request, "itemTypeCode", "TASK"),
            value(request, "itemTitle", "Work Item " + itemId),
            value(request, "priority", "MEDIUM"),
            value(request, "statusCode", "TODO"),
            value(request, "ownerName", "Unassigned"),
            itemId
        );
    }

    public int deleteWorkItem(Long itemId) {
        return jdbcTemplate.update("DELETE FROM work_item WHERE id = ?", itemId);
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
