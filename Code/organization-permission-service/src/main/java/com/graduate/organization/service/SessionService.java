package com.graduate.organization.service;

import com.graduate.organization.entity.SessionUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpSession;

@Service
public class SessionService {

    public static final String LOGIN_USER_ATTRIBUTE = "LOGIN_USER";
    public static final String ROLE_SYSTEM_ADMIN = "SYSTEM_ADMIN";

    public SessionUser requireLogin(HttpSession session) {
        Object value = session.getAttribute(LOGIN_USER_ATTRIBUTE);
        if (value instanceof SessionUser) {
            return (SessionUser) value;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录组织与权限域服务。");
    }

    public SessionUser requireSystemAdmin(HttpSession session) {
        SessionUser user = requireLogin(session);
        if (!ROLE_SYSTEM_ADMIN.equals(user.getRoleCode())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "当前账号无权访问系统级页面。");
        }
        return user;
    }
}
