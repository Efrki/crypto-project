package com.example.crypto_project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage implements Serializable {
    private Long chatRoomId;
    private String senderUsername;
    private String ciphertext; // Зашифрованное сообщение
}