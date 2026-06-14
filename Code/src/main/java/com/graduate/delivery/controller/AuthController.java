package com.graduate.delivery.controller;

import com.graduate.delivery.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public Map<String, Object> login(@RequestBody Map<String, String> request, HttpSession session) {
        return authService.login(request, session);
    }

    @GetMapping("/me")
    public Map<String, Object> current(HttpSession session) {
        return authService.current(session);
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpSession session) {
        return authService.logout(session);
    }
}
