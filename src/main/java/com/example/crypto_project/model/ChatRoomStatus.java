package com.example.crypto_project.model;

public enum ChatRoomStatus {
    PENDING, // Ожидание второго участника
    ACTIVE,  // Оба участника в чате, ключ согласован
    CLOSED   // Чат закрыт
}