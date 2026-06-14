package com.graduate.organization.controller;

import com.graduate.organization.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> request,
                                     HttpSession session,
                                     HttpServletRequest servletRequest) {
        return authService.login(request, session, servletRequest);
    }

    @GetMapping("/me")
    public Map<String, Object> me(HttpSession session) {
        return authService.me(session);
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpSession session) {
        return authService.logout(session);
    }
}
