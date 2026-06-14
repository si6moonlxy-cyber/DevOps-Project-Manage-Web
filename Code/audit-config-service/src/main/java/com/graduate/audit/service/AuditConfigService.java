package com.graduate.audit.service;

import com.graduate.audit.mapper.AuditConfigMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuditConfigService {

    private static final List<String> TABLES = Arrays.asList(
        "sys_user",
        "project",
        "work_item",
        "data_source",
        "collect_job",
        "metric_definition",
        "metric_result",
        "report_record",
        "alert_event",
        "sys_config"
    );

    private final AuditConfigMapper mapper;
    private final String mysqlUrl;
    private final String mysqlUsername;
    private final String mysqlPassword;
    private final String organizationBaseUrl;
    private final String projectBaseUrl;
    private final String devopsBaseUrl;
    private final String metricsBaseUrl;
    private final String auditBaseUrl;

    public AuditConfigService(AuditConfigMapper mapper,
                              @Value("${platform.target-mysql.url}") String mysqlUrl,
                              @Value("${platform.target-mysql.username}") String mysqlUsername,
                              @Value("${platform.target-mysql.password}") String mysqlPassword,
                              @Value("${platform.service.organization.base-url}") String organizationBaseUrl,
                              @Value("${platform.service.project.base-url}") String projectBaseUrl,
                              @Value("${platform.service.devops.base-url}") String devopsBaseUrl,
                              @Value("${platform.service.metrics.base-url}") String metricsBaseUrl,
                              @Value("${platform.service.audit.base-url}") String auditBaseUrl) {
        this.mapper = mapper;
        this.mysqlUrl = mysqlUrl;
        this.mysqlUsername = mysqlUsername;
        this.mysqlPassword = mysqlPassword;
        this.organizationBaseUrl = organizationBaseUrl;
        this.projectBaseUrl = projectBaseUrl;
        this.devopsBaseUrl = devopsBaseUrl;
        this.metricsBaseUrl = metricsBaseUrl;
        this.auditBaseUrl = auditBaseUrl;
    }

    public Map<String, Object> getAuditEventsPage() {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("summaryCards", buildAuditCards());
        payload.put("alerts", mapper.findAlerts());
        payload.put("levelDistribution", toDistribution(mapper.countAlertLevel()));
        payload.put("loginLogs", mapper.findLoginLogs());
        payload.put("operationLogs", mapper.findOperationLogs());
        return payload;
    }

    public Map<String, Object> getConfigConsolePage() {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("summaryCards", buildConfigCards());
        payload.put("engineInfo", buildEngineInfo());
        payload.put("targetDatabase", buildTargetDatabase());
        payload.put("serviceRegistry", buildServiceRegistry());
        payload.put("configItems", mapper.findConfigItems());
        payload.put("tableProfiles", buildTableProfiles());
        payload.put("operationLogs", mapper.findOperationLogs());
        payload.put("sampleSqls", buildSampleSqls());
        payload.put("readOnlyTip", "配置控制台仅支持 SELECT 或 WITH 查询，用于演示共享数据库下的只读巡检能力。");
        return payload;
    }

    public Map<String, Object> runQuery(Map<String, String> request) {
        String sql = request == null ? "" : request.get("sql");
        String normalized = sql == null ? "" : sql.trim();
        validateReadOnlyQuery(normalized);

        List<Map<String, Object>> rows = mapper.executeReadOnlyQuery(normalized);
        List<String> columns = new ArrayList<String>();
        if (!rows.isEmpty()) {
            columns.addAll(rows.get(0).keySet());
        }

        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("columns", columns);
        payload.put("rows", rows);
        payload.put("rowCount", rows.size());
        return payload;
    }

    private List<Map<String, Object>> buildAuditCards() {
        List<Map<String, Object>> cards = new ArrayList<Map<String, Object>>();
        cards.add(createCard("告警事件", valueOf(mapper.countAlerts()), "条", "审计域集中查看环境告警、恢复状态与项目责任链路。", "brand"));
        cards.add(createCard("处理中告警", valueOf(mapper.countActiveAlerts()), "条", "突出当前仍需跟踪的告警事件与恢复进度。", "danger"));
        cards.add(createCard("登录审计", valueOf(mapper.countLoginLogs()), "条", "登录成功、失败与锁定记录统一写入 login_log。", "accent"));
        cards.add(createCard("配置项", valueOf(mapper.countConfigItems()), "项", "审计与配置域统一维护平台配置台账。", "success"));
        return cards;
    }

    private List<Map<String, Object>> buildConfigCards() {
        List<Map<String, Object>> cards = new ArrayList<Map<String, Object>>();
        cards.add(createCard("微服务节点", String.valueOf(buildServiceRegistry().size()), "个", "组织与权限、项目交付、DevOps 数据、指标报告、审计配置分开运行。", "brand"));
        cards.add(createCard("配置项数量", valueOf(mapper.countConfigItems()), "项", "系统配置读取 sys_config 表，用于答辩展示。", "success"));
        cards.add(createCard("只读样例 SQL", String.valueOf(buildSampleSqls().size()), "条", "用于展示共享 MySQL 总库的查询巡检能力。", "warning"));
        return cards;
    }

    private Map<String, Object> buildEngineInfo() {
        Map<String, Object> info = new LinkedHashMap<String, Object>();
        info.put("engineName", "MySQL 8.0");
        info.put("mode", "Shared Database / Five Microservices");
        info.put("jdbcUrl", mysqlUrl);
        info.put("schema", extractSchemaName(mysqlUrl));
        info.put("lastRefreshTime", mapper.findLatestUpdateTime());
        return info;
    }

    private Map<String, Object> buildTargetDatabase() {
        Map<String, Object> info = new LinkedHashMap<String, Object>();
        info.put("databaseType", "MySQL 8.0");
        info.put("jdbcUrl", mysqlUrl);
        info.put("username", mysqlUsername);
        info.put("passwordMask", mask(mysqlPassword));
        info.put("description", "当前五个业务域服务共享同一演示数据库，便于答辩时展示主链路数据。");
        return info;
    }

    private List<Map<String, Object>> buildServiceRegistry() {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        rows.add(createService("组织与权限域服务", "organization-permission-service", organizationBaseUrl, "登录鉴权、角色导航与前端承载"));
        rows.add(createService("项目交付域服务", "project-delivery-service", projectBaseUrl, "项目、工作项、里程碑与交付动态"));
        rows.add(createService("DevOps 数据域服务", "devops-data-service", devopsBaseUrl, "数据源、采集任务、采集日志与质量巡检"));
        rows.add(createService("指标与报告域服务", "metrics-report-service", metricsBaseUrl, "指标定义、指标结果、报表历史与预警"));
        rows.add(createService("审计与配置域服务", "audit-config-service", auditBaseUrl, "告警审计、配置台账与只读 SQL 控制台"));
        return rows;
    }

    private List<Map<String, Object>> buildTableProfiles() {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        for (String tableName : TABLES) {
            Map<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("tableName", tableName);
            row.put("rowCount", valueOf(mapper.countRows(tableName)));
            row.put("columnCount", valueOf(mapper.countColumns(tableName)));
            row.put("description", describeTable(tableName));
            rows.add(row);
        }
        return rows;
    }

    private List<String> buildSampleSqls() {
        List<String> sqls = new ArrayList<String>();
        sqls.add("SELECT username, login_status, login_at FROM login_log ORDER BY login_at DESC LIMIT 10");
        sqls.add("SELECT project_name, project_status, visibility_scope FROM project ORDER BY id");
        sqls.add("SELECT metric_code, metric_name, current_version_no FROM metric_definition ORDER BY id");
        sqls.add("SELECT source_name, source_type, status FROM data_source ORDER BY id");
        return sqls;
    }

    private List<Map<String, Object>> toDistribution(List<Map<String, Object>> rows) {
        List<Map<String, Object>> distribution = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("name", row.get("itemName"));
            item.put("value", row.get("totalCount"));
            distribution.add(item);
        }
        return distribution;
    }

    private Map<String, Object> createCard(String title, String value, String unit, String description, String tone) {
        Map<String, Object> card = new LinkedHashMap<String, Object>();
        card.put("title", title);
        card.put("value", value);
        card.put("unit", unit);
        card.put("description", description);
        card.put("tone", tone);
        return card;
    }

    private Map<String, Object> createService(String serviceName, String serviceCode, String baseUrl, String responsibility) {
        Map<String, Object> row = new LinkedHashMap<String, Object>();
        row.put("serviceName", serviceName);
        row.put("serviceCode", serviceCode);
        row.put("baseUrl", baseUrl);
        row.put("runStatus", "RUNNING");
        row.put("portNo", extractPort(baseUrl));
        row.put("responsibility", responsibility);
        return row;
    }

    private String valueOf(Integer value) {
        return value == null ? "0" : String.valueOf(value);
    }

    private String describeTable(String tableName) {
        if ("sys_user".equals(tableName)) {
            return "统一存放系统与企业管理账号、角色归属和登录状态。";
        }
        if ("project".equals(tableName)) {
            return "项目主数据表，串联部门、团队、版本与负责人。";
        }
        if ("work_item".equals(tableName)) {
            return "需求、任务和缺陷工作项，是交付效能统计的核心明细。";
        }
        if ("data_source".equals(tableName)) {
            return "外部系统接入配置表，描述需求系统、代码平台和监控源。";
        }
        if ("collect_job".equals(tableName)) {
            return "采集任务定义表，用于说明定时同步链路和调度策略。";
        }
        if ("metric_definition".equals(tableName)) {
            return "统一存放指标定义、公式口径和版本号。";
        }
        if ("metric_result".equals(tableName)) {
            return "指标计算结果表，保存不同周期和维度的统计值。";
        }
        if ("report_record".equals(tableName)) {
            return "报表历史记录表，关联模板、周期和留痕状态。";
        }
        if ("alert_event".equals(tableName)) {
            return "告警事件表，跟踪触发、恢复、根因与项目环境。";
        }
        if ("sys_config".equals(tableName)) {
            return "系统配置台账，记录 JDBC、开关、调度与界面配置。";
        }
        return "共享总库业务表。";
    }

    private String mask(String value) {
        if (value == null || value.isEmpty()) {
            return "未设置";
        }
        if (value.length() <= 2) {
            return "**";
        }
        StringBuilder builder = new StringBuilder();
        builder.append(value.charAt(0));
        for (int i = 0; i < value.length() - 2; i++) {
            builder.append('*');
        }
        builder.append(value.charAt(value.length() - 1));
        return builder.toString();
    }

    private String extractPort(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            return "--";
        }
        int colonIndex = baseUrl.lastIndexOf(':');
        if (colonIndex < 0 || colonIndex == baseUrl.length() - 1) {
            return "--";
        }
        return baseUrl.substring(colonIndex + 1).trim();
    }

    private String extractSchemaName(String jdbcUrl) {
        if (jdbcUrl == null || jdbcUrl.trim().isEmpty()) {
            return "db_projectmanage";
        }
        int slashIndex = jdbcUrl.lastIndexOf('/');
        if (slashIndex < 0 || slashIndex == jdbcUrl.length() - 1) {
            return "db_projectmanage";
        }
        String tail = jdbcUrl.substring(slashIndex + 1);
        int questionIndex = tail.indexOf('?');
        return questionIndex >= 0 ? tail.substring(0, questionIndex) : tail;
    }

    private void validateReadOnlyQuery(String sql) {
        String normalized = sql == null ? "" : sql.trim().toLowerCase();
        if (normalized.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请输入需要执行的查询语句。");
        }
        if (!(normalized.startsWith("select") || normalized.startsWith("with"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "配置控制台仅支持 SELECT / WITH 查询。");
        }
        if (normalized.contains(";") || normalized.contains("--")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "为保证答辩演示稳定性，当前控制台不支持分号和注释语法。");
        }

        List<String> bannedKeywords = Arrays.asList(
            "insert ",
            "update ",
            "delete ",
            "drop ",
            "alter ",
            "truncate ",
            "merge ",
            "create ",
            "grant ",
            "revoke ",
            "into outfile"
        );
        for (String keyword : bannedKeywords) {
            if (normalized.contains(keyword)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "检测到写操作关键字，当前控制台已禁止执行。");
            }
        }
    }
}
