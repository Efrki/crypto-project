package com.example.crypto_project.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "contacts")
@Getter
@Setter
@NoArgsConstructor
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Пользователь, который отправил запрос
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Пользователь, которому отправили запрос
    @ManyToOne
    @JoinColumn(name = "contact_user_id", nullable = false)
    private User contactUser;

    // Статус запроса (PENDING, ACCEPTED, DECLINED)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContactStatus status;
}
