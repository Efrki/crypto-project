package com.example.crypto_project.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinChatRequest {
    private String dhPublicKey; // Публичный ключ в HEX-формате
}