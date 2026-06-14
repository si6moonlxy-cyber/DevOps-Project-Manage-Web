package com.graduate.delivery.service;

import com.graduate.delivery.entity.SessionUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpSession;

@Service
public class SessionService {

    public static final String LOGIN_USER_ATTRIBUTE = "LOGIN_USER";
    public static final String ROLE_SYSTEM_ADMIN = "SYSTEM_ADMIN";

    public SessionUser getOptionalUser(HttpSession session) {
        Object value = session == null ? null : session.getAttribute(LOGIN_USER_ATTRIBUTE);
        return value instanceof SessionUser ? (SessionUser) value : null;
    }

    public SessionUser requireLogin(HttpSession session) {
        SessionUser user = getOptionalUser(session);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录管理端账号。");
        }
        return user;
    }

    public SessionUser requireSystemAdmin(HttpSession session) {
        SessionUser user = requireLogin(session);
        if (!ROLE_SYSTEM_ADMIN.equals(user.getRoleCode())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "当前账号无权访问系统管理页面。");
        }
        return user;
    }
}
