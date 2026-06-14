package com.graduate.delivery.service;

import com.graduate.delivery.entity.AdminAccount;
import com.graduate.delivery.entity.SessionUser;
import com.graduate.delivery.mapper.ManagementMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ManagementService {

    private static final List<String> TABLES = Arrays.asList(
        "department",
        "team",
        "sys_user",
        "admin_account",
        "project",
        "work_item",
        "data_source",
        "metric_definition",
        "metric_snapshot",
        "report_record",
        "alert_event",
        "delivery_activity"
    );

    private final ManagementMapper managementMapper;
    private final PortalService portalService;

    public ManagementService(ManagementMapper managementMapper, PortalService portalService) {
        this.managementMapper = managementMapper;
        this.portalService = portalService;
    }

    public Map<String, Object> getAccountsPage(SessionUser user) {
        List<AdminAccount> accounts = managementMapper.findAllAccounts();
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("summaryCards", buildAccountCards(accounts));
        payload.put("accounts", buildAccountRows(accounts));
        return payload;
    }

    public Map<String, Object> getDatabasePage(SessionUser user) {
        List<AdminAccount> accounts = managementMapper.findAllAccounts();
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("engineInfo", buildEngineInfo());
        payload.put("tableProfiles", buildTableProfiles());
        payload.put("loginProfiles", buildAccountRows(accounts));
        payload.put("sampleSqls", buildSampleSqls());
        payload.put("readOnlyTip", "数据库控制台仅开放 SELECT / WITH 查询，账号密码等敏感字段已在页面侧脱敏展示。");
        return payload;
    }

    public Map<String, Object> runQuery(SessionUser user, Map<String, String> request) {
        String sql = request == null ? "" : request.get("sql");
        String normalized = sql == null ? "" : sql.trim();
        validateReadOnlyQuery(normalized);

        List<Map<String, Object>> rows = managementMapper.executeReadOnlyQuery(normalized);
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

    private List<Map<String, Object>> buildAccountCards(List<AdminAccount> accounts) {
        int systemCount = 0;
        int enterpriseCount = 0;
        for (AdminAccount account : accounts) {
            if (SessionService.ROLE_SYSTEM_ADMIN.equals(account.getRoleCode())) {
                systemCount++;
            } else {
                enterpriseCount++;
            }
        }

        List<Map<String, Object>> cards = new ArrayList<Map<String, Object>>();
        cards.add(createCard("系统管理账号", systemCount, "个", "拥有管理端与数据库控制台权限", "brand"));
        cards.add(createCard("企业管理账号", enterpriseCount, "个", "仅可见企业级菜单与工作台页面", "success"));
        cards.add(createCard("系统页数", portalService.getVisiblePageCount(SessionService.ROLE_SYSTEM_ADMIN), "页", "系统管理角色可见的页面数量", "accent"));
        cards.add(createCard("企业页数", portalService.getVisiblePageCount("ENTERPRISE_ADMIN"), "页", "企业管理角色可见的页面数量", "warning"));
        return cards;
    }

    private List<Map<String, Object>> buildAccountRows(List<AdminAccount> accounts) {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (AdminAccount account : accounts) {
            Map<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("displayName", account.getDisplayName());
            row.put("username", account.getUsername());
            row.put("loginPassword", maskSecret(account.getLoginPassword()));
            row.put("roleName", account.getRoleName());
            row.put("accountStatus", account.getAccountStatus());
            row.put("visiblePageCount", portalService.getVisiblePageCount(account.getRoleCode()));
            row.put("lastLoginAt", formatTimestamp(account.getLastLoginAt(), formatter));
            row.put("description", account.getDescription());
            rows.add(row);
        }
        return rows;
    }

    private List<Map<String, Object>> buildTableProfiles() {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        for (String table : TABLES) {
            Map<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("tableName", table);
            row.put("rowCount", managementMapper.countRows(table));
            row.put("columnCount", managementMapper.countColumns(table));
            row.put("description", describeTable(table));
            rows.add(row);
        }
        return rows;
    }

    private Map<String, Object> buildEngineInfo() {
        Map<String, Object> info = new LinkedHashMap<String, Object>();
        info.put("engineName", "H2 Database Engine");
        info.put("mode", "In-Memory / MySQL Compatibility");
        info.put("jdbcUrl", "jdbc:h2:mem:delivery_metrics");
        info.put("schema", "PUBLIC");
        info.put("lastRefreshTime", managementMapper.findLatestRefreshTime());
        return info;
    }

    private List<String> buildSampleSqls() {
        List<String> sqls = new ArrayList<String>();
        sqls.add("SELECT username, role_name, account_status, last_login_at FROM admin_account ORDER BY id");
        sqls.add("SELECT project_name, status, risk_level, progress FROM project ORDER BY progress DESC");
        sqls.add("SELECT report_name, quality_score, status FROM report_record ORDER BY generated_at DESC");
        return sqls;
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

    private String formatTimestamp(Timestamp value, SimpleDateFormat formatter) {
        return value == null ? "首次登录" : formatter.format(value);
    }

    private String describeTable(String tableName) {
        if ("admin_account".equals(tableName)) {
            return "存放系统管理和企业管理登录账号。";
        }
        if ("project".equals(tableName)) {
            return "项目主数据与交付阶段信息。";
        }
        if ("work_item".equals(tableName)) {
            return "用于工作量页面的需求、任务和缺陷。";
        }
        if ("metric_snapshot".equals(tableName)) {
            return "核心度量结果快照，供图表和看板使用。";
        }
        if ("delivery_activity".equals(tableName)) {
            return "首页近期动态与交付轨迹记录。";
        }
        return "管理端演示数据表。";
    }

    private String maskSecret(String value) {
        if (value == null || value.length() == 0) {
            return "--";
        }
        if (value.length() <= 4) {
            return "****";
        }
        return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
    }

    private void validateReadOnlyQuery(String sql) {
        String normalized = sql == null ? "" : sql.trim().toLowerCase();
        if (normalized.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请输入需要执行的查询语句。");
        }

        // 管理端只开放只读查询，避免本地答辩演示误改数据。
        if (!(normalized.startsWith("select") || normalized.startsWith("with"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "数据库控制台仅支持 SELECT / WITH 查询。");
        }

        if (normalized.contains(";") || normalized.contains("--")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "为了避免多语句执行，当前控制台不支持分号和注释写法。");
        }

        List<String> bannedKeywords = Arrays.asList("insert ", "update ", "delete ", "drop ", "alter ", "truncate ", "merge ", "create ", "grant ", "revoke ");
        for (String keyword : bannedKeywords) {
            if (normalized.contains(keyword)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "检测到写操作关键字，当前控制台已禁止执行。");
            }
        }
    }
}
