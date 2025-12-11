package com.example.crypto_project.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "local_messages")
@Getter
@Setter
@NoArgsConstructor
public class LocalMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long chatRoomId;
    
    @Column(nullable = false)
    private String senderUsername;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(nullable = false)
    private boolean isOutgoing; // true если отправлено нами, false если получено
    
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}