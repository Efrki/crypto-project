package com.example.crypto_project.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "chat_participants")
@Getter
@Setter
public class ChatParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    // Публичный ключ Диффи-Хеллмана этого участника для этого чата
    // Хранится как строка в формате HEX
    @Column(columnDefinition = "TEXT")
    private String dhPublicKey;
}