package com.example.crypto_project.controller;

import com.example.crypto_project.dto.ChatRoomDto;
import com.example.crypto_project.dto.CreateChatRequest;
import com.example.crypto_project.dto.JoinChatRequest;
import com.example.crypto_project.model.ChatRoom;
import com.example.crypto_project.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/create")
    public ResponseEntity<?> createChat(@RequestBody CreateChatRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String initiatorUsername = authentication.getName();

        ChatRoom chatRoom = chatService.createChatRoom(request, initiatorUsername);

        // Возвращаем ID созданной комнаты
        return ResponseEntity.ok().body("Chat room created with ID: " + chatRoom.getId());
    }

    @PostMapping("/join/{roomId}")
    public ResponseEntity<?> joinChat(@PathVariable Long roomId, @RequestBody JoinChatRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String joiningUsername = authentication.getName();

        ChatRoom chatRoom = chatService.joinChatRoom(roomId, request, joiningUsername);

        // После присоединения возвращаем публичный ключ другого участника,
        // чтобы клиент мог сгенерировать общий секрет.
        String otherPartyPublicKey = chatRoom.getParticipants().stream()
                .filter(p -> !p.getUser().getLogin().equals(joiningUsername))
                .findFirst().get().getDhPublicKey();

        return ResponseEntity.ok().body(otherPartyPublicKey);
    }

    @GetMapping
    public ResponseEntity<List<ChatRoomDto>> getUserChats() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<ChatRoomDto> chats = chatService.getChatsForUser(authentication.getName());
        return ResponseEntity.ok(chats);
    }

    @PostMapping("/close/{roomId}")
    public ResponseEntity<String> closeChat(@PathVariable Long roomId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        chatService.closeChatRoom(roomId, authentication.getName());
        return ResponseEntity.ok("Chat room closed successfully.");
    }

    @GetMapping("/{roomId}/other-public-key")
    public ResponseEntity<String> getOtherPartyPublicKey(@PathVariable Long roomId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String publicKey = chatService.getOtherPartyPublicKey(roomId, authentication.getName());
        return ResponseEntity.ok(publicKey);
    }
}