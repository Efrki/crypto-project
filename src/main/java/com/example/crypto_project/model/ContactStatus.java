package com.example.crypto_project.model;

/**
 * Перечисление, представляющее статус запроса на добавление в контакты.
 */
public enum ContactStatus {
    PENDING,  // Запрос отправлен, ожидает подтверждения
    ACCEPTED, // Запрос принят, пользователи в контактах
    DECLINED  // Запрос отклонен
}