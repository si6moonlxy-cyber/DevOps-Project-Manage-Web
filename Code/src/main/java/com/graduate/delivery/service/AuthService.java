package com.graduate.delivery.service;

import com.graduate.delivery.entity.AdminAccount;
import com.graduate.delivery.entity.SessionUser;
import com.graduate.delivery.mapper.AuthMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AuthService {

    private final AuthMapper authMapper;
    private final SessionService sessionService;
    private final PortalService portalService;

    public AuthService(AuthMapper authMapper, SessionService sessionService, PortalService portalService) {
        this.authMapper = authMapper;
        this.sessionService = sessionService;
        this.portalService = portalService;
    }

    public Map<String, Object> login(Map<String, String> request, HttpSession session) {
        String username = trim(request.get("username"));
        String password = trim(request.get("password"));
        if (username.isEmpty() || password.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请输入用户名和密码。");
        }

        AdminAccount account = authMapper.findByUsername(username);
        if (account == null || !password.equals(account.getLoginPassword()) || !"ACTIVE".equals(account.getAccountStatus())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "账号或密码不正确。");
        }

        SessionUser user = SessionUser.fromAccount(account);
        session.setAttribute(SessionService.LOGIN_USER_ATTRIBUTE, user);
        authMapper.updateLastLogin(account.getId());
        return buildAuthPayload(user);
    }

    public Map<String, Object> current(HttpSession session) {
        return buildAuthPayload(sessionService.requireLogin(session));
    }

    public Map<String, Object> logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("message", "已退出当前管理端会话。");
        return payload;
    }

    private Map<String, Object> buildAuthPayload(SessionUser user) {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("user", buildUserProfile(user));
        payload.put("navigation", portalService.getNavigation(user));
        return payload;
    }

    private Map<String, Object> buildUserProfile(SessionUser user) {
        Map<String, Object> profile = new LinkedHashMap<String, Object>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("displayName", user.getDisplayName());
        profile.put("roleCode", user.getRoleCode());
        profile.put("roleName", user.getRoleName());
        profile.put("teamId", user.getTeamId());
        return profile;
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
