package com.example.crypto_project.controller;

import com.example.crypto_project.model.LocalMessage;
import com.example.crypto_project.service.LocalMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/local-messages")
@RequiredArgsConstructor
public class LocalMessageController {
    
    private final LocalMessageService localMessageService;
    
    @GetMapping("/chat/{chatId}")
    public ResponseEntity<List<LocalMessage>> getChatHistory(@PathVariable Long chatId) {
        List<LocalMessage> messages = localMessageService.getChatHistory(chatId);
        return ResponseEntity.ok(messages);
    }
    
    @PostMapping("/save")
    public ResponseEntity<String> saveMessage(@RequestBody SaveMessageRequest request) {
        localMessageService.saveMessage(
            request.getChatRoomId(),
            request.getSenderUsername(),
            request.getContent(),
            request.isOutgoing()
        );
        return ResponseEntity.ok("Message saved");
    }
    
    public static class SaveMessageRequest {
        private Long chatRoomId;
        private String senderUsername;
        private String content;
        private boolean outgoing;
        
        // Getters and setters
        public Long getChatRoomId() { return chatRoomId; }
        public void setChatRoomId(Long chatRoomId) { this.chatRoomId = chatRoomId; }
        public String getSenderUsername() { return senderUsername; }
        public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public boolean isOutgoing() { return outgoing; }
        public void setOutgoing(boolean outgoing) { this.outgoing = outgoing; }
    }
}