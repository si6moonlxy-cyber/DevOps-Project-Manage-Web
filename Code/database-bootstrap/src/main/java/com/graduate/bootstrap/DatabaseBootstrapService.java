package com.graduate.bootstrap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DatabaseBootstrapService {

    private enum SeedMode {
        SKIP,
        INJECT,
        RESET_AND_INJECT
    }

    private final boolean enabled;
    private final String databaseName;
    private final String serverUrl;
    private final String appUrl;
    private final String username;
    private final String password;
    private final String sqlRoot;
    private final String schemaFile;
    private final String seedFile;
    private final PasswordEncoder passwordEncoder;

    public DatabaseBootstrapService(@Value("${platform.bootstrap.enabled:true}") boolean enabled,
                                    @Value("${platform.bootstrap.database-name}") String databaseName,
                                    @Value("${platform.bootstrap.server-url}") String serverUrl,
                                    @Value("${platform.bootstrap.app-url}") String appUrl,
                                    @Value("${platform.bootstrap.username}") String username,
                                    @Value("${platform.bootstrap.password}") String password,
                                    @Value("${platform.bootstrap.sql-root:}") String sqlRoot,
                                    @Value("${platform.bootstrap.schema-file}") String schemaFile,
                                    @Value("${platform.bootstrap.seed-file}") String seedFile) {
        this.enabled = enabled;
        this.databaseName = databaseName;
        this.serverUrl = serverUrl;
        this.appUrl = appUrl;
        this.username = username;
        this.password = password;
        this.sqlRoot = sqlRoot == null ? "" : sqlRoot.trim();
        this.schemaFile = schemaFile;
        this.seedFile = seedFile;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public void bootstrap() throws Exception {
        if (!enabled) {
            System.out.println("[bootstrap] Database bootstrap is disabled.");
            return;
        }

        Path root = resolveSqlRoot();
        Path schemaPath = root.resolve(schemaFile).normalize();
        Path seedPath = root.resolve(seedFile).normalize();
        assertFileExists(schemaPath);
        assertFileExists(seedPath);

        System.out.println("[bootstrap] Using SQL root: " + root.toAbsolutePath());
        runSchema(schemaPath);
        runSeedIfRequired(seedPath);
        syncPasswordHashes();
        System.out.println("[bootstrap] Database bootstrap completed for " + databaseName + ".");
    }

    private void runSchema(Path schemaPath) throws Exception {
        try (Connection connection = DriverManager.getConnection(serverUrl, username, password)) {
            ScriptUtils.executeSqlScript(connection, new EncodedResource(new FileSystemResource(schemaPath.toFile()), StandardCharsets.UTF_8));
        }
    }

    private void runSeedIfRequired(Path seedPath) throws Exception {
        try (Connection connection = DriverManager.getConnection(appUrl, username, password)) {
            SeedMode seedMode = resolveSeedMode(connection);
            if (seedMode == SeedMode.SKIP) {
                System.out.println("[bootstrap] Seed data already exists, skipping SQL injection.");
                return;
            }
            if (seedMode == SeedMode.RESET_AND_INJECT) {
                System.out.println("[bootstrap] Legacy demo data detected, resetting platform tables before seed injection.");
                resetDemoData(connection);
            }
            ScriptUtils.executeSqlScript(connection, new EncodedResource(new FileSystemResource(seedPath.toFile()), StandardCharsets.UTF_8));
            System.out.println("[bootstrap] Seed data injected from " + seedPath.getFileName() + ".");
        }
    }

    private void syncPasswordHashes() throws Exception {
        Map<String, String> demoPasswords = buildDemoPasswords();
        try (Connection connection = DriverManager.getConnection(appUrl, username, password)) {
            for (Map.Entry<String, String> entry : demoPasswords.entrySet()) {
                updatePasswordHashIfRequired(connection, entry.getKey(), entry.getValue());
            }
        }
    }

    private void updatePasswordHashIfRequired(Connection connection, String usernameValue, String rawPassword) throws Exception {
        String selectSql = "SELECT password_hash FROM sys_user WHERE username = ?";
        try (PreparedStatement select = connection.prepareStatement(selectSql)) {
            select.setString(1, usernameValue);
            try (ResultSet resultSet = select.executeQuery()) {
                if (!resultSet.next()) {
                    return;
                }
                String currentValue = resultSet.getString(1);
                if (currentValue != null && currentValue.startsWith("$2")) {
                    return;
                }
            }
        }

        String updateSql = "UPDATE sys_user SET password_hash = ? WHERE username = ?";
        try (PreparedStatement update = connection.prepareStatement(updateSql)) {
            update.setString(1, passwordEncoder.encode(rawPassword));
            update.setString(2, usernameValue);
            update.executeUpdate();
        }
    }

    private int countRows(Connection connection, String tableName) throws Exception {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    private SeedMode resolveSeedMode(Connection connection) throws Exception {
        if (countRows(connection, "sys_user") == 0) {
            return SeedMode.INJECT;
        }
        if (existsUsername(connection, "sys_root")) {
            return SeedMode.SKIP;
        }
        if (existsUsername(connection, "user01") || existsRoleCode(connection, "SYS_ADMIN")) {
            return SeedMode.RESET_AND_INJECT;
        }
        return SeedMode.SKIP;
    }

    private boolean existsUsername(Connection connection, String usernameValue) throws Exception {
        String sql = "SELECT 1 FROM sys_user WHERE username = ? LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, usernameValue);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private boolean existsRoleCode(Connection connection, String roleCode) throws Exception {
        String sql = "SELECT 1 FROM sys_role WHERE role_code = ? LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, roleCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private void resetDemoData(Connection connection) throws Exception {
        List<String> tables = Arrays.asList(
            "api_access_log",
            "login_log",
            "operation_log",
            "report_record",
            "dashboard_config",
            "metric_alert_record",
            "metric_result",
            "metric_calc_job",
            "metric_definition_version",
            "alert_event",
            "deployment_record",
            "build_record",
            "merge_request",
            "code_commit",
            "work_item",
            "data_quality_log",
            "collect_log",
            "collect_job",
            "data_source_project",
            "deploy_environment",
            "release_version",
            "project_member",
            "user_data_scope",
            "sys_role_permission",
            "sys_user_role",
            "project",
            "data_quality_rule",
            "report_template",
            "open_api_client",
            "metric_definition",
            "metric_category",
            "sys_config",
            "data_source",
            "sys_permission",
            "sys_role",
            "sys_user",
            "team",
            "product_line",
            "org_department"
        );

        try (Statement statement = connection.createStatement()) {
            statement.execute("SET FOREIGN_KEY_CHECKS = 0");
            for (String table : tables) {
                statement.executeUpdate("DELETE FROM " + table);
                statement.execute("ALTER TABLE " + table + " AUTO_INCREMENT = 1");
            }
            statement.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

    private Map<String, String> buildDemoPasswords() {
        Map<String, String> passwords = new LinkedHashMap<String, String>();
        passwords.put("sys_root", "Sys@2026");
        passwords.put("sys_audit", "Audit@2026");
        passwords.put("sys_ops", "Ops@2026");
        passwords.put("sys_config", "Cfg@2026");
        passwords.put("enterprise_admin", "Corp@2026");
        passwords.put("enterprise_pm", "Pm@2026");
        passwords.put("enterprise_quality", "Qa@2026");
        passwords.put("enterprise_arch", "Arch@2026");
        passwords.put("enterprise_delivery", "Del@2026");
        passwords.put("enterprise_test", "Test@2026");
        return passwords;
    }

    private Path resolveSqlRoot() {
        List<Path> candidates = Arrays.asList(
            pathOf(sqlRoot),
            Paths.get(System.getProperty("user.dir")).resolve("..").resolve("sql"),
            Paths.get(System.getProperty("user.dir")).resolve("sql"),
            Paths.get(System.getProperty("user.dir")).resolve("..").resolve("..").resolve("sql")
        );

        for (Path candidate : candidates) {
            if (candidate == null) {
                continue;
            }
            Path normalized = candidate.toAbsolutePath().normalize();
            if (Files.exists(normalized.resolve(schemaFile))) {
                return normalized;
            }
        }
        throw new IllegalStateException("Unable to locate SQL root for bootstrap. Checked user.dir based candidates near: " + System.getProperty("user.dir"));
    }

    private Path pathOf(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return Paths.get(value.trim());
    }

    private void assertFileExists(Path file) {
        if (!Files.exists(file)) {
            throw new IllegalStateException("Missing required SQL file: " + file.toAbsolutePath());
        }
    }
}
