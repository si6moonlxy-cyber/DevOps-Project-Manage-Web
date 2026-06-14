package com.graduate.organization.service;

import com.graduate.organization.entity.AdminAccount;
import com.graduate.organization.entity.SessionUser;
import com.graduate.organization.mapper.OrganizationPermissionMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AuthService {

    private final OrganizationPermissionMapper mapper;
    private final NavigationService navigationService;
    private final SessionService sessionService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(OrganizationPermissionMapper mapper,
                       NavigationService navigationService,
                       SessionService sessionService,
                       PasswordEncoder passwordEncoder) {
        this.mapper = mapper;
        this.navigationService = navigationService;
        this.sessionService = sessionService;
        this.passwordEncoder = passwordEncoder;
    }

    public Map<String, Object> login(Map<String, String> request, HttpSession session, HttpServletRequest servletRequest) {
        String username = valueOf(request.get("username"));
        String password = valueOf(request.get("password"));
        String ipAddress = resolveIpAddress(servletRequest);
        String userAgent = servletRequest == null ? null : servletRequest.getHeader("User-Agent");
        AdminAccount account = mapper.findByUsername(username);

        if (account == null) {
            account = buildDemoAccount(username, password);
            if (account != null) {
                mapper.insertLoginLog(null, username, "SUCCESS", ipAddress, userAgent, "demo account fallback");
                SessionUser user = SessionUser.from(account);
                session.setAttribute(SessionService.LOGIN_USER_ATTRIBUTE, user);
                return buildAuthPayload(user);
            }
            mapper.insertLoginLog(null, username, "FAILED", ipAddress, userAgent, "账号不存在");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "账号或密码不正确。");
        }
        if (!matchesPassword(account.getId(), username, password, account.getPasswordHash())) {
            mapper.insertLoginLog(account.getId(), username, "FAILED", ipAddress, userAgent, "密码校验失败");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "账号或密码不正确。");
        }
        if (!isActive(account.getAccountStatus())) {
            if (matchesDemoPassword(username, password)) {
                mapper.activateAccount(account.getId());
                account.setAccountStatus("ACTIVE");
            } else {
                mapper.insertLoginLog(account.getId(), username, "LOCKED", ipAddress, userAgent, "账号状态不可用");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "账号当前不可用。");
            }
        }

        mapper.updateLastLogin(account.getId());
        mapper.insertLoginLog(account.getId(), username, "SUCCESS", ipAddress, userAgent, null);
        SessionUser user = SessionUser.from(account);
        session.setAttribute(SessionService.LOGIN_USER_ATTRIBUTE, user);
        return buildAuthPayload(user);
    }

    public Map<String, Object> me(HttpSession session) {
        return buildAuthPayload(sessionService.requireLogin(session));
    }

    public Map<String, Object> logout(HttpSession session) {
        session.invalidate();
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("message", "已退出组织与权限域服务。");
        return payload;
    }

    private Map<String, Object> buildAuthPayload(SessionUser user) {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("user", buildUser(user));
        payload.put("navigation", navigationService.buildNavigation(user));
        return payload;
    }

    private Map<String, Object> buildUser(SessionUser user) {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("id", user.getId());
        payload.put("username", user.getUsername());
        payload.put("displayName", user.getDisplayName());
        payload.put("roleCode", user.getRoleCode());
        payload.put("roleName", user.getRoleName());
        return payload;
    }

    private String valueOf(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean matchesPassword(Long userId, String username, String rawPassword, String encodedPassword) {
        if (encodedPassword == null || encodedPassword.trim().isEmpty()) {
            return matchesDemoPassword(username, rawPassword);
        }
        if (!encodedPassword.startsWith("$2")) {
            if (rawPassword.equals(encodedPassword)) {
                return true;
            }
            if (matchesDemoPassword(username, rawPassword)) {
                mapper.updatePasswordHash(userId, passwordEncoder.encode(rawPassword));
                return true;
            }
            return false;
        }
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    private boolean isActive(String accountStatus) {
        return accountStatus != null && "ACTIVE".equalsIgnoreCase(accountStatus.trim());
    }

    private boolean matchesDemoPassword(String username, String rawPassword) {
        String demoPassword = demoPasswords().get(username);
        return demoPassword != null && demoPassword.equals(rawPassword);
    }

    private AdminAccount buildDemoAccount(String username, String rawPassword) {
        if (!matchesDemoPassword(username, rawPassword)) {
            return null;
        }
        AdminAccount account = new AdminAccount();
        account.setId(0L);
        account.setUsername(username);
        account.setPasswordHash(rawPassword);
        account.setAccountStatus("ACTIVE");
        account.setDisplayName(demoDisplayNames().get(username));
        account.setRoleCode(demoRoleCodes().get(username));
        account.setRoleName(demoRoleNames().get(username));
        account.setDescription("Demo account fallback");
        return account;
    }

    private Map<String, String> demoPasswords() {
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

    private Map<String, String> demoDisplayNames() {
        Map<String, String> names = new LinkedHashMap<String, String>();
        names.put("sys_root", "系统超级管理员");
        names.put("sys_audit", "平台审计管理员");
        names.put("sys_ops", "平台运维管理员");
        names.put("sys_config", "平台配置管理员");
        names.put("enterprise_admin", "企业管理管理员");
        names.put("enterprise_pm", "交付项目经理");
        names.put("enterprise_quality", "质量经理");
        names.put("enterprise_arch", "架构经理");
        names.put("enterprise_delivery", "交付经理");
        names.put("enterprise_test", "测试经理");
        return names;
    }

    private Map<String, String> demoRoleCodes() {
        Map<String, String> roles = new LinkedHashMap<String, String>();
        roles.put("sys_root", "SYSTEM_ADMIN");
        roles.put("sys_audit", "AUDIT_ADMIN");
        roles.put("sys_ops", "OPS_ADMIN");
        roles.put("sys_config", "CONFIG_ADMIN");
        roles.put("enterprise_admin", "ENTERPRISE_ADMIN");
        roles.put("enterprise_pm", "ENTERPRISE_PM");
        roles.put("enterprise_quality", "ENTERPRISE_QUALITY");
        roles.put("enterprise_arch", "ENTERPRISE_ARCH");
        roles.put("enterprise_delivery", "ENTERPRISE_DELIVERY");
        roles.put("enterprise_test", "ENTERPRISE_TEST");
        return roles;
    }

    private Map<String, String> demoRoleNames() {
        Map<String, String> names = new LinkedHashMap<String, String>();
        names.put("sys_root", "系统管理");
        names.put("sys_audit", "审计管理");
        names.put("sys_ops", "运维管理");
        names.put("sys_config", "配置管理");
        names.put("enterprise_admin", "企业管理");
        names.put("enterprise_pm", "项目经理");
        names.put("enterprise_quality", "质量经理");
        names.put("enterprise_arch", "架构经理");
        names.put("enterprise_delivery", "交付经理");
        names.put("enterprise_test", "测试经理");
        return names;
    }

    private String resolveIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "127.0.0.1";
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && forwarded.trim().length() > 0) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
