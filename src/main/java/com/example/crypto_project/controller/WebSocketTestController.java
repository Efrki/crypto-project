package com.example.crypto_project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class WebSocketTestController {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    @PostMapping("/websocket/{username}")
    public String testWebSocket(@PathVariable String username) {
        Map<String, String> testMessage = Map.of("type", "TEST", "message", "Hello " + username);
        messagingTemplate.convertAndSendToUser(username, "/topic/messages", testMessage);
        return "Test message sent to " + username;
    }
}