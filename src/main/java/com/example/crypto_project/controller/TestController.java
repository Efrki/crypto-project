package com.example.crypto_project.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    // Этот эндпоинт доступен всем, так как мы разрешили его в SecurityConfig
    @GetMapping("/hello")
    public String hello() {
        return "Привет, сервер работает!";
    }

    // Этот эндпоинт требует аутентификации
    @GetMapping("/api/protected")
    public String protectedEndpoint() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return "Hello, " + authentication.getName() + "! You have access to a protected resource.";
    }
}
