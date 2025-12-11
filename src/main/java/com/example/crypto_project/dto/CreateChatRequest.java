package com.example.crypto_project.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateChatRequest {
    private String contactUsername;
    private String encryptionAlgorithm; // "RC6" или "Twofish"
    private String cipherMode;
    private String paddingMode;
    private String dhPublicKey; // Публичный ключ в HEX-формате
}