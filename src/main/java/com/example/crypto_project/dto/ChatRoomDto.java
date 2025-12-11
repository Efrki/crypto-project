package com.example.crypto_project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ChatRoomDto {
    private Long id;
    private String status;
    private String encryptionAlgorithm;
    private List<String> participants;
}