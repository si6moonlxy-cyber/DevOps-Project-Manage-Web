package com.graduate.delivery.mapper;

import com.graduate.delivery.entity.AdminAccount;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class AuthMapper {

    private final JdbcTemplate jdbcTemplate;

    public AuthMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public AdminAccount findByUsername(String username) {
        List<AdminAccount> accounts = jdbcTemplate.query(
            "SELECT id, username, login_password AS loginPassword, display_name AS displayName, " +
                "role_code AS roleCode, role_name AS roleName, team_id AS teamId, account_status AS accountStatus, " +
                "last_login_at AS lastLoginAt, description " +
                "FROM admin_account WHERE username = ?",
            new BeanPropertyRowMapper<AdminAccount>(AdminAccount.class),
            username
        );
        return accounts.isEmpty() ? null : accounts.get(0);
    }

    public void updateLastLogin(Long accountId) {
        jdbcTemplate.update("UPDATE admin_account SET last_login_at = ? WHERE id = ?", new Timestamp(System.currentTimeMillis()), accountId);
    }
}
