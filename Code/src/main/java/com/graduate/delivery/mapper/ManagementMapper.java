package com.graduate.delivery.mapper;

import com.graduate.delivery.entity.AdminAccount;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class ManagementMapper {

    private final JdbcTemplate jdbcTemplate;

    public ManagementMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AdminAccount> findAllAccounts() {
        return jdbcTemplate.query(
            "SELECT id, username, login_password AS loginPassword, display_name AS displayName, " +
                "role_code AS roleCode, role_name AS roleName, team_id AS teamId, account_status AS accountStatus, " +
                "last_login_at AS lastLoginAt, description FROM admin_account ORDER BY id",
            new BeanPropertyRowMapper<AdminAccount>(AdminAccount.class)
        );
    }

    public Integer countRows(String tableName) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class);
    }

    public Integer countColumns(String tableName) {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ? AND TABLE_SCHEMA = 'PUBLIC'",
            Integer.class,
            tableName.toUpperCase()
        );
    }

    public String findLatestRefreshTime() {
        return jdbcTemplate.queryForObject(
            "SELECT FORMATDATETIME(MAX(last_sync_time), 'yyyy-MM-dd HH:mm') FROM data_source",
            String.class
        );
    }

    public List<Map<String, Object>> executeReadOnlyQuery(String sql) {
        return jdbcTemplate.queryForList(sql);
    }
}
