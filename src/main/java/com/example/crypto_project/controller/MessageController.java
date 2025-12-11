package com.example.crypto_project.controller;

import com.example.crypto_project.dto.ChatMessage;
import com.example.crypto_project.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestBody ChatMessage message) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        messageService.sendMessage(message, authentication.getName());
        return ResponseEntity.ok("Message sent to queue.");
    }
}