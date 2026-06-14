package com.graduate.organization.mapper;

import com.graduate.organization.entity.AdminAccount;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Map;

@Repository
public class OrganizationPermissionMapper {

    private final JdbcTemplate jdbcTemplate;

    public OrganizationPermissionMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public AdminAccount findByUsername(String username) {
        List<AdminAccount> accounts;
        if (hasTable("sys_user") && hasTable("sys_user_role") && hasTable("sys_role")) {
            accounts = jdbcTemplate.query(
                "SELECT u.id, u.username, u.password_hash AS passwordHash, u.real_name AS displayName, " +
                    "r.role_code AS roleCode, r.role_name AS roleName, u.status AS accountStatus, " +
                    "u.last_login_at AS lastLoginAt, COALESCE(u.job_title, u.remark, '管理账号') AS description " +
                    "FROM sys_user u " +
                    "JOIN sys_user_role ur ON ur.user_id = u.id " +
                    "JOIN sys_role r ON r.id = ur.role_id " +
                    "WHERE u.username = ? " +
                    "ORDER BY ur.id ASC LIMIT 1",
                new BeanPropertyRowMapper<AdminAccount>(AdminAccount.class),
                username
            );
        } else {
            accounts = jdbcTemplate.query(
                "SELECT id, username, login_password AS passwordHash, display_name AS displayName, " +
                    "role_code AS roleCode, role_name AS roleName, account_status AS accountStatus, " +
                    "last_login_at AS lastLoginAt, description " +
                    "FROM admin_account WHERE username = ?",
                new BeanPropertyRowMapper<AdminAccount>(AdminAccount.class),
                username
            );
        }
        return accounts.isEmpty() ? null : accounts.get(0);
    }

    public void updateLastLogin(Long id) {
        String tableName = hasTable("sys_user") ? "sys_user" : "admin_account";
        jdbcTemplate.update("UPDATE " + tableName + " SET last_login_at = ? WHERE id = ?", new Timestamp(System.currentTimeMillis()), id);
    }

    public void updatePasswordHash(Long id, String passwordHash) {
        if (!hasTable("sys_user")) {
            return;
        }
        jdbcTemplate.update("UPDATE sys_user SET password_hash = ?, updated_at = NOW() WHERE id = ?", passwordHash, id);
    }

    public void activateAccount(Long id) {
        if (!hasTable("sys_user")) {
            jdbcTemplate.update("UPDATE admin_account SET account_status = 'ACTIVE' WHERE id = ?", id);
            return;
        }
        jdbcTemplate.update("UPDATE sys_user SET status = 'ACTIVE', updated_at = NOW() WHERE id = ?", id);
    }

    public void insertLoginLog(Long userId, String username, String status, String ipAddress, String userAgent, String failureReason) {
        if (!hasTable("login_log")) {
            return;
        }
        jdbcTemplate.update(
            "INSERT INTO login_log (user_id, username, login_status, ip_address, user_agent, failure_reason, login_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, NOW())",
            new Object[]{userId, username, status, ipAddress, userAgent, failureReason},
            new int[]{Types.BIGINT, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR}
        );
    }

    public Integer countDepartments() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + organizationTable("org_department", "department"), Integer.class);
    }

    public Integer countTeams() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM team", Integer.class);
    }

    public Integer countAccounts() {
        if (!hasTable("sys_user")) {
            return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM admin_account WHERE account_status = 'ACTIVE'", Integer.class);
        }
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(DISTINCT u.id) FROM sys_user u " +
                "JOIN sys_user_role ur ON ur.user_id = u.id " +
                "JOIN sys_role r ON r.id = ur.role_id " +
                "WHERE u.status = 'ACTIVE' AND r.status = 'ACTIVE'",
            Integer.class
        );
    }

    public Integer countSystemAdmins() {
        if (!hasTable("sys_user")) {
            return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM admin_account WHERE account_status = 'ACTIVE' AND role_code = 'SYSTEM_ADMIN'", Integer.class);
        }
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(DISTINCT u.id) FROM sys_user u " +
                "JOIN sys_user_role ur ON ur.user_id = u.id " +
                "JOIN sys_role r ON r.id = ur.role_id " +
                "WHERE u.status = 'ACTIVE' AND r.role_code = 'SYSTEM_ADMIN'",
            Integer.class
        );
    }

    public List<Map<String, Object>> findTeams() {
        String departmentTable = organizationTable("org_department", "department");
        if (!hasTable("sys_user")) {
            return jdbcTemplate.queryForList(
                "SELECT t.id AS teamId, d.dept_name AS deptName, t.team_name AS teamName, " +
                    "COALESCE(t.focus_area, '交付协同与度量建设') AS focusArea, " +
                    "COALESCE(t.member_count, 0) AS memberCount " +
                    "FROM team t " +
                    "JOIN " + departmentTable + " d ON d.id = t.dept_id " +
                    "ORDER BY t.id"
            );
        }
        return jdbcTemplate.queryForList(
            "SELECT t.id AS teamId, d.dept_name AS deptName, t.team_name AS teamName, COALESCE(t.remark, '交付协同与度量建设') AS focusArea, " +
                "(SELECT COUNT(*) FROM sys_user su WHERE su.team_id = t.id AND su.status = 'ACTIVE') AS memberCount " +
                "FROM team t " +
                "JOIN " + departmentTable + " d ON d.id = t.dept_id " +
                "ORDER BY t.id"
        );
    }

    public List<Map<String, Object>> findAccounts() {
        if (!hasTable("sys_user")) {
            return jdbcTemplate.queryForList(
                "SELECT display_name AS displayName, username, role_name AS roleName, account_status AS accountStatus, " +
                    "description, COALESCE(DATE_FORMAT(last_login_at, '%Y-%m-%d %H:%i'), '首次登录') AS lastLoginAt " +
                    "FROM admin_account ORDER BY id"
            );
        }
        return jdbcTemplate.queryForList(
            "SELECT u.id AS userId, u.real_name AS displayName, u.username, r.id AS roleId, r.role_code AS roleCode, r.role_name AS roleName, u.status AS accountStatus, " +
                "COALESCE(u.job_title, u.remark, '管理端账号') AS description, " +
                "COALESCE(DATE_FORMAT(u.last_login_at, '%Y-%m-%d %H:%i'), '首次登录') AS lastLoginAt " +
                "FROM sys_user u " +
                "JOIN sys_user_role ur ON ur.user_id = u.id " +
                "JOIN sys_role r ON r.id = ur.role_id " +
                "WHERE r.status = 'ACTIVE' " +
                "ORDER BY u.id"
        );
    }

    public List<Map<String, Object>> findAssignableMembers() {
        if (!hasTable("sys_user")) {
            return jdbcTemplate.queryForList(
                "SELECT id AS userId, display_name AS displayName, username, NULL AS teamId, role_name AS roleName, account_status AS accountStatus " +
                "FROM admin_account WHERE role_code NOT IN ('SYSTEM_ADMIN', 'ENTERPRISE_ADMIN') ORDER BY id"
            );
        }
        return jdbcTemplate.queryForList(
            "SELECT u.id AS userId, u.real_name AS displayName, u.username, u.team_id AS teamId, " +
                "t.team_name AS teamName, r.role_name AS roleName, u.status AS accountStatus " +
                "FROM sys_user u " +
                "JOIN sys_user_role ur ON ur.user_id = u.id " +
                "JOIN sys_role r ON r.id = ur.role_id " +
                "LEFT JOIN team t ON t.id = u.team_id " +
                "WHERE r.role_code NOT IN ('SYSTEM_ADMIN', 'ENTERPRISE_ADMIN') AND u.status = 'ACTIVE' " +
                "ORDER BY u.id"
        );
    }

    public List<Map<String, Object>> findRoles() {
        if (!hasTable("sys_role")) {
            return jdbcTemplate.queryForList(
                "SELECT DISTINCT role_code AS roleCode, role_code AS roleId, role_name AS roleName, account_status AS status FROM admin_account ORDER BY role_code"
            );
        }
        return jdbcTemplate.queryForList(
            "SELECT id AS roleId, role_code AS roleCode, role_name AS roleName, status FROM sys_role ORDER BY id"
        );
    }

    public void updateAccount(Long userId, String username, String rawPassword, Long roleId, String status, String description, org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        if (rawPassword != null && rawPassword.trim().length() > 0) {
            jdbcTemplate.update(
                "UPDATE sys_user SET username = ?, password_hash = ?, status = ?, remark = ?, updated_at = NOW() WHERE id = ?",
                username, passwordEncoder.encode(rawPassword.trim()), status, description, userId
            );
        } else {
            jdbcTemplate.update(
                "UPDATE sys_user SET username = ?, status = ?, remark = ?, updated_at = NOW() WHERE id = ?",
                username, status, description, userId
            );
        }
        jdbcTemplate.update("DELETE FROM sys_user_role WHERE user_id = ?", userId);
        jdbcTemplate.update("INSERT INTO sys_user_role (user_id, role_id, created_at) VALUES (?, ?, NOW())", userId, roleId);
    }

    public void updateMemberTeam(Long userId, Long teamId) {
        jdbcTemplate.update("UPDATE sys_user SET team_id = ?, updated_at = NOW() WHERE id = ?", teamId, userId);
    }

    public void createMember(Map<String, Object> request, org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        Long teamId = longValue(request.get("teamId"), 1L);
        String username = value(request, "username", "member" + System.currentTimeMillis());
        String displayName = value(request, "displayName", username);
        String password = value(request, "password", "User@2026");
        Long roleId = jdbcTemplate.queryForObject(
            "SELECT COALESCE((SELECT id FROM sys_role WHERE role_code = 'ENTERPRISE_USER' LIMIT 1), (SELECT id FROM sys_role WHERE role_code = 'ENTERPRISE_PM' LIMIT 1), MIN(id)) FROM sys_role",
            Long.class
        );
        jdbcTemplate.update(
            "INSERT INTO sys_user (dept_id, team_id, username, real_name, password_hash, email, user_type, job_title, status, remark, created_at, updated_at) " +
                "SELECT t.dept_id, ?, ?, ?, ?, CONCAT(?, '@demo.local'), 'ENTERPRISE', 'Member', 'ACTIVE', 'Created from portal', NOW(), NOW() FROM team t WHERE t.id = ?",
            teamId, username, displayName, passwordEncoder.encode(password), username, teamId
        );
        Long userId = jdbcTemplate.queryForObject("SELECT id FROM sys_user WHERE username = ?", Long.class, username);
        jdbcTemplate.update("INSERT INTO sys_user_role (user_id, role_id, created_at) VALUES (?, ?, NOW())", userId, roleId);
    }

    public int deleteMember(Long userId) {
        return jdbcTemplate.update("UPDATE sys_user SET status = 'INACTIVE', updated_at = NOW() WHERE id = ?", userId);
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

    private String organizationTable(String preferredName, String fallbackName) {
        return hasTable(preferredName) ? preferredName : fallbackName;
    }

    private boolean hasTable(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
            Integer.class,
            tableName
        );
        return count != null && count.intValue() > 0;
    }
}
